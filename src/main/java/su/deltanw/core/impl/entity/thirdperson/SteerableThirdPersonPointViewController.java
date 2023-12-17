package su.deltanw.core.impl.entity.thirdperson;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import su.deltanw.core.api.entity.model.factory.EntityModelFactory;

public class SteerableThirdPersonPointViewController extends ThirdPersonViewController {

  private static final float MAX_ROTATION_ANGLE = 0.5F;
  private static final float SENSITIVITY = 0.5F;

  private final Vector direction = new Vector();
  private final Location target;

  public SteerableThirdPersonPointViewController(EntityModelFactory<?, ?> factory, ThirdPersonNettyHandler handler,
                                                 Player player, Location viewPoint, Location target) {
    super(factory, handler, player, viewPoint);
    this.target = target.clone();
    setTarget(target);
  }

  @Override
  protected void inject(Channel channel) {
    channel.pipeline().addBefore("packet_handler", "thirdperson_view_control",
        new ChannelInboundHandlerAdapter() {

          private Float previousYaw = null;

          @Override
          public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
            if (msg instanceof ServerboundMovePlayerPacket packet) {
              if (packet.hasRotation()) {
                if (previousYaw != null) {
                  Vector relative = viewPoint.clone().subtract(target).toVector();
                  float diff = (packet.xRot - previousYaw) * SENSITIVITY;
                  diff = Math.min(Math.max(diff, -MAX_ROTATION_ANGLE), MAX_ROTATION_ANGLE);
                  relative.rotateAroundY(Location.normalizeYaw(diff));
                  Location newViewPoint = relative.toLocation(viewPoint.getWorld()).add(target);
                  moveTo(newViewPoint);
                }
                previousYaw = packet.xRot;
              }
            }
            super.channelRead(ctx, msg);
          }
        });
    super.inject(channel);
  }

  @Override
  protected void deject(Channel channel) {
    super.deject(channel);
    if (channel.isActive()) {
      channel.pipeline().remove("thirdperson_view_control");
    }
  }

  public void calculateDirection() {
    this.direction.copy(target.clone().subtract(viewPoint).toVector().normalize());
    this.viewPoint.setDirection(direction);
    updateViewerPosition();
  }

  public void setTarget(Location target) {
    this.target.set(target.x(), target.y(), target.z());
    calculateDirection();
  }

  @Override
  public void rotate(float yaw, float pitch) {

  }

  @Override
  public void move(double deltaX, double deltaY, double deltaZ, float yaw, float pitch) {
    move(deltaX, deltaY, deltaZ);
  }

  @Override
  public void move(double deltaX, double deltaY, double deltaZ) {
    viewPoint.add(deltaX, deltaY, deltaZ);
    calculateDirection();

    if (!isActive()) {
      return;
    }

    moveClientside(deltaX, deltaY, deltaZ, viewPoint.getYaw(), viewPoint.getPitch());
  }
}
