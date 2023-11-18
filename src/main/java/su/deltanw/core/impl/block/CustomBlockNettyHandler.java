package su.deltanw.core.impl.block;

import com.jeff_media.customblockdata.CustomBlockData;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import su.deltanw.core.Core;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CustomBlockNettyHandler extends ChannelDuplexHandler {

  private static final MethodHandle SECTION_POS_GETTER;
  private static final MethodHandle POSITIONS_GETTER;
  private static final MethodHandle BLOCK_STATES_GETTER;

  static {
    try {
      Field[] fields = ClientboundSectionBlocksUpdatePacket.class.getDeclaredFields();

      String sectionPosFieldName =
          Arrays.stream(fields)
              .dropWhile(field -> field.getType() != SectionPos.class)
              .findFirst().orElseThrow().getName();

      String positionsFieldName =
          Arrays.stream(fields)
              .dropWhile(field -> field.getType() != short[].class)
              .findFirst().orElseThrow().getName();

      String blockStatesFieldName =
          Arrays.stream(fields)
              .dropWhile(field -> field.getType() != BlockState[].class)
              .findFirst().orElseThrow().getName();

      SECTION_POS_GETTER = MethodHandles.privateLookupIn(ClientboundSectionBlocksUpdatePacket.class, MethodHandles.lookup())
          .findGetter(ClientboundSectionBlocksUpdatePacket.class, sectionPosFieldName, SectionPos.class);

      POSITIONS_GETTER = MethodHandles.privateLookupIn(ClientboundSectionBlocksUpdatePacket.class, MethodHandles.lookup())
          .findGetter(ClientboundSectionBlocksUpdatePacket.class, positionsFieldName, short[].class);

      BLOCK_STATES_GETTER = MethodHandles.privateLookupIn(ClientboundSectionBlocksUpdatePacket.class, MethodHandles.lookup())
          .findGetter(ClientboundSectionBlocksUpdatePacket.class, blockStatesFieldName, BlockState[].class);
    } catch (Throwable e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private final Map<Channel, String> channelToNameMap = new HashMap<>();

  private final Core plugin;

  public CustomBlockNettyHandler(Core plugin) {
    this.plugin = plugin;
  }

  @Override
  public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
    if (msg instanceof ServerboundHelloPacket packet) {
      channelToNameMap.put(ctx.channel(), packet.name());
    }
    super.channelRead(ctx, msg);
  }

  private CustomBlock getCustomBlock(Block block) {
    CustomBlockData blockData = new CustomBlockData(block, plugin);
    String key = blockData.get(Objects.requireNonNull(NamespacedKey.fromString("deltanw:custom_block")), PersistentDataType.STRING);
    if (key != null) {
      NamespacedKey namespacedKey = NamespacedKey.fromString(key);
      return CustomBlock.get(namespacedKey);
    }
    return null;
  }

  private CustomBlock getCustomBlock(World world, BlockPos blockPos) {
    Location location = new Location(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    return getCustomBlock(location.getBlock());
  }

  private Player getPlayer(Channel channel) {
    String name = channelToNameMap.get(channel);
    if (name != null) {
      return Bukkit.getPlayerExact(name);
    }
    return null;
  }

  private void forceUpdateLight(ChannelHandlerContext ctx, World world, ChunkPos pos) {
    ServerLevel level = ((CraftWorld) world).getHandle();

    for (int chunkX = pos.x - 1; chunkX <= pos.x + 1; chunkX++) {
      for (int chunkZ = pos.z - 1; chunkZ <= pos.z + 1; chunkZ++) {
        ctx.write(new ClientboundLightUpdatePacket(new ChunkPos(chunkX, chunkZ), level.getLightEngine(), null, null));
      }
    }
  }

  private void forceUpdateNeighboursLight(ChannelHandlerContext ctx, World world, ChunkPos pos) {
    ServerLevel level = ((CraftWorld) world).getHandle();

    for (int chunkX = pos.x - 1; chunkX <= pos.x + 1; chunkX++) {
      for (int chunkZ = pos.z - 1; chunkZ <= pos.z + 1; chunkZ++) {
        if (chunkZ == pos.x && chunkX == pos.z) {
          continue;
        }

        ctx.write(new ClientboundLightUpdatePacket(new ChunkPos(chunkX, chunkZ), level.getLightEngine(), null, null));
      }
    }
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (msg instanceof ClientboundBlockUpdatePacket packet) {
      Player player = getPlayer(ctx.channel());
      if (player != null) {
        CustomBlock customBlock = getCustomBlock(player.getWorld(), packet.getPos());
        if (customBlock != null) {
          ctx.write(new ClientboundBlockUpdatePacket(packet.getPos(), customBlock.clientsideBlock()));
          if (customBlock.serversideBlock().getLightEmission() > 0) {
            forceUpdateLight(ctx, player.getWorld(), new ChunkPos(packet.getPos()));
          }
          return;
        }
      }
    } else if (msg instanceof ClientboundLevelChunkWithLightPacket packet) {
      super.write(ctx, msg, promise);

      Player player = getPlayer(ctx.channel());
      if (player == null) {
        return;
      }

      Chunk chunk = player.getWorld().getChunkAt(packet.getX(), packet.getZ());
      Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        Set<Block> blocks = CustomBlockData.getBlocksWithCustomData(plugin, chunk);

        boolean needLightUpdate = false;
        for (Block block : blocks) {
          CustomBlock customBlock = getCustomBlock(block);
          if (customBlock == null) {
            return;
          }

          // TODO: Update sections
          ctx.write(new ClientboundBlockUpdatePacket(new BlockPos(block.getX(), block.getY(), block.getZ()), customBlock.clientsideBlock()));
          if (customBlock.serversideBlock().getLightEmission() > 0) {
            needLightUpdate = true;
          }
        }

        if (needLightUpdate) {
          Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
              forceUpdateNeighboursLight(ctx, player.getWorld(), new ChunkPos(packet.getX(), packet.getZ())));
          FriendlyByteBuf buf = new FriendlyByteBuf(ctx.alloc().ioBuffer());
          buf.writeVarInt(0x27); // 1.20.1 - Update Light
          buf.writeVarInt(packet.getX());
          buf.writeVarInt(packet.getZ());
          packet.getLightData().write(buf);
          ctx.write(buf);
        }
      });

      return;
    } else if (msg instanceof ClientboundSectionBlocksUpdatePacket packet) {
      Player player = getPlayer(ctx.channel());
      if (player == null) {
        super.write(ctx, msg, promise);
        return;
      }

      try {
        SectionPos sectionPos = (SectionPos) SECTION_POS_GETTER.invokeExact(packet);
        short[] positions = (short[]) POSITIONS_GETTER.invokeExact(packet);
        BlockState[] blockStates = (BlockState[]) BLOCK_STATES_GETTER.invokeExact(packet);

        boolean needLightUpdate = false;
        Chunk chunk = player.getWorld().getChunkAt(sectionPos.x(), sectionPos.z());
        for (int i = 0; i < positions.length; i++) {
          short position = positions[i];
          int x = SectionPos.sectionRelativeX(position);
          int y = sectionPos.relativeToBlockY(position);
          int z = SectionPos.sectionRelativeZ(position);
          Block block = chunk.getBlock(x, y, z);
          CustomBlock customBlock = getCustomBlock(block);
          if (customBlock == null) {
            continue;
          }
          blockStates[i] = customBlock.clientsideBlock();
          if (customBlock.serversideBlock().getLightEmission() > 0) {
            needLightUpdate = true;
          }
        }

        super.write(ctx, msg, promise);
        if (needLightUpdate) {
          forceUpdateLight(ctx, player.getWorld(), sectionPos.chunk());
        }

        return;
      } catch (Throwable e) {
        plugin.getLogger().warning("Unable to update custom blocks section.");
      }
    }

    super.write(ctx, msg, promise);
  }

  @Override
  public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    channelToNameMap.remove(ctx.channel());
    super.close(ctx, promise);
  }
}
