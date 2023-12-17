package su.deltanw.core.impl.entity.thirdperson;

import io.netty.channel.*;
import io.papermc.paper.event.player.PlayerTrackEntityEvent;
import io.papermc.paper.event.player.PlayerUntrackEntityEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import su.deltanw.core.Core;
import su.deltanw.core.api.entity.model.EntityModel;

@ChannelHandler.Sharable
public class ThirdPersonNettyHandler extends ChannelOutboundHandlerAdapter implements Listener {

  private final Int2ObjectMap<ThirdPersonViewController> thirdPersonControllers = new Int2ObjectOpenHashMap<>();
  private final Core core;

  public ThirdPersonNettyHandler(Core core) {
    this.core = core;
  }

  public void lockPlayer(ThirdPersonViewController controller) {
    thirdPersonControllers.put(controller.getPlayer().getEntityId(), controller);
  }

  public void unlockPlayer(Player player) {
    thirdPersonControllers.remove(player.getEntityId());
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (msg instanceof ClientboundSetEntityDataPacket packet) {
      if (thirdPersonControllers.containsKey(packet.id())) {
        return;
      }
    } else if (msg instanceof ClientboundSetEquipmentPacket packet) {
      if (thirdPersonControllers.containsKey(packet.getEntity())) {
        return;
      }
    }

    super.write(ctx, msg, promise);
  }

  @EventHandler
  public void onTrack(PlayerTrackEntityEvent event) {
    int id = event.getEntity().getEntityId();
    ThirdPersonViewController controller = thirdPersonControllers.get(id);
    if (controller != null) {
      EntityModel model = controller.getModel();
      model.spawn(event.getPlayer());
      model.addViewer(event.getPlayer());
      ServerGamePacketListenerImpl connection = ((CraftPlayer) event.getPlayer()).getHandle().connection;
      ChannelHandlerContext packetEncoder = connection.connection.channel.pipeline().context("thirdperson_handler");
      Bukkit.getScheduler().runTask(core, () -> {
        packetEncoder.write(new ClientboundSetEntityDataPacket(id, controller.getMetadata()));
        packetEncoder.write(new ClientboundSetEquipmentPacket(id, controller.getEquipment()));
      });
    }
  }

  @EventHandler
  public void onUntrack(PlayerUntrackEntityEvent event) {
    int id = event.getEntity().getEntityId();
    ThirdPersonViewController controller = thirdPersonControllers.get(id);
    if (controller != null) {
      EntityModel model = controller.getModel();
      model.removeViewer(event.getPlayer());
      model.despawn(event.getPlayer());
    }
  }
}
