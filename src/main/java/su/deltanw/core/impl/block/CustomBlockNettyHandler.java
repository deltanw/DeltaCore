package su.deltanw.core.impl.block;

import com.jeff_media.customblockdata.CustomBlockData;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import su.deltanw.core.Core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CustomBlockNettyHandler extends ChannelDuplexHandler {

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

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (msg instanceof ClientboundBlockUpdatePacket packet) {
      Player player = getPlayer(ctx.channel());
      if (player != null) {
        CustomBlock customBlock = getCustomBlock(player.getWorld(), packet.getPos());
        if (customBlock != null) {
          msg = new ClientboundBlockUpdatePacket(packet.getPos(), customBlock.clientsideBlock());
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
        blocks.forEach(block -> {
          CustomBlock customBlock = getCustomBlock(block);
          if (customBlock == null) {
            return;
          }

          // TODO: Update sections
          ctx.write(new ClientboundBlockUpdatePacket(new BlockPos(block.getX(), block.getY(), block.getZ()), customBlock.clientsideBlock()));
        });
      });

      return;
    }
    // TODO: Update Section packet
    super.write(ctx, msg, promise);
  }

  @Override
  public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    channelToNameMap.remove(ctx.channel());
    super.close(ctx, promise);
  }
}
