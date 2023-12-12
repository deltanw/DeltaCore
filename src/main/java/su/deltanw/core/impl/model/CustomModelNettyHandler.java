package su.deltanw.core.impl.model;

import com.jeff_media.customblockdata.CustomBlockData;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import su.deltanw.core.Core;
import su.deltanw.core.impl.util.Vector2DataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomModelNettyHandler extends ChannelDuplexHandler {

  private final Map<Channel, String> channelToNameMap = new HashMap<>();

  private final Core plugin;

  public CustomModelNettyHandler(Core plugin) {
    this.plugin = plugin;
  }

  private Player getPlayer(Channel channel) {
    String name = channelToNameMap.get(channel);
    if (name != null) {
      return Bukkit.getPlayerExact(name);
    }
    return null;
  }

  @Override
  public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
    if (msg instanceof ServerboundHelloPacket packet) {
      channelToNameMap.put(ctx.channel(), packet.name());
    }
    super.channelRead(ctx, msg);
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (msg instanceof ClientboundLevelChunkWithLightPacket || msg instanceof ClientboundForgetLevelChunkPacket) {
      Player player = getPlayer(ctx.channel());
      if (player == null) {
        return;
      }

      Chunk chunk;
      if (msg instanceof ClientboundLevelChunkWithLightPacket packet) {
        chunk = player.getWorld().getChunkAt(packet.getX(), packet.getZ());
      } else {
        ClientboundForgetLevelChunkPacket packet = (ClientboundForgetLevelChunkPacket) msg;
        chunk = player.getWorld().getChunkAt(packet.getX(), packet.getZ());
      }
      for (Block block : CustomBlockData.getBlocksWithCustomData(plugin, chunk)) {
        CustomBlockData blockData = new CustomBlockData(block, plugin);
        String modelKeyString = blockData.get(CustomModel.MODEL_PDC_KEY, PersistentDataType.STRING);
        if (modelKeyString == null) {
          continue;
        }

        NamespacedKey modelKey = NamespacedKey.fromString(modelKeyString);
        CustomModel model = CustomModel.get(modelKey);
        int entityId = Objects.requireNonNull(blockData.get(CustomModel.MODEL_PDC_EID_KEY, PersistentDataType.INTEGER));
        Vector2f rotation = Objects.requireNonNull(blockData.get(CustomModel.MODEL_PDC_ROTATION_KEY, Vector2DataType.INSTANCE));
        List<Packet<?>> packets;
        if (msg instanceof ClientboundLevelChunkWithLightPacket) {
          packets = model.createSpawnPackets(entityId, rotation, block.getLocation().toVector().toBlockVector());
        } else {
          packets = model.createDestroyPackets(entityId);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> packets.forEach(ctx::write));
      }
    }

    super.write(ctx, msg, promise);
  }
}
