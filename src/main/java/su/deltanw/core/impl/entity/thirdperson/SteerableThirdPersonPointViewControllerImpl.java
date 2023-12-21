package su.deltanw.core.impl.entity.thirdperson;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.RelativeMovement;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import su.deltanw.core.api.entity.model.factory.EntityModelFactory;
import su.deltanw.core.api.entity.thirdperson.ThirdPersonPointViewController;

import java.util.Set;

public class SteerableThirdPersonPointViewControllerImpl extends ThirdPersonViewControllerImpl implements ThirdPersonPointViewController {

  // TODO: Make this configurable?
  private static final float MAX_YAW_ROTATION_DIFF = 28.5F;
  private static final float MAX_PITCH_ROTATION_DIFF = 15.0F;
  private static final float MIN_PITCH_ROTATION = -45.0F;
  private static final float MAX_PITCH_ROTATION = 80.0F;
  private static final float SENSITIVITY = 2.85F;

  private final Vector direction = new Vector();
  private final Location target;
  private double distance;

  public SteerableThirdPersonPointViewControllerImpl(EntityModelFactory<?, ?> factory, ThirdPersonViewHandler handler,
                                                     Player player, Location viewPoint, Location target) {
    super(factory, handler, player, viewPoint);
    this.target = target.clone();
    setTarget(target);
  }

  @Override
  public boolean enterView() {
    if (super.enterView()) {
      ((CraftPlayer) getPlayer()).getHandle().connection.send(
          new ClientboundPlayerPositionPacket(0, 0, 0, 0, 0, Set.of(RelativeMovement.X, RelativeMovement.Y, RelativeMovement.Z), -1));
      return true;
    }

    return false;
  }

  @Override
  protected void inject(Channel channel) {
    channel.pipeline().addBefore("packet_handler", "thirdperson_view_control",
        new ChannelInboundHandlerAdapter() {

          private Vector2f rotation = null;

          @Override
          public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
            if (msg instanceof ServerboundMovePlayerPacket packet) {
              if (packet.hasRotation()) {
                if (rotation != null) {
                  Vector relative = direction.clone().multiply(-distance);

                  float yawDiff = (rotation.y() - packet.yRot) * SENSITIVITY;
                  yawDiff = Math.min(Math.max(yawDiff, -MAX_YAW_ROTATION_DIFF), MAX_YAW_ROTATION_DIFF);
                  relative.rotateAroundY(Math.toRadians(yawDiff));

                  float pitchDiff = (packet.xRot - rotation.x()) * SENSITIVITY;
                  pitchDiff = Math.min(Math.max(pitchDiff, -MAX_PITCH_ROTATION_DIFF), MAX_PITCH_ROTATION_DIFF);
                  float absolutePitch = viewPoint.getPitch() + pitchDiff;
                  absolutePitch = Math.max(Math.min(absolutePitch, MAX_PITCH_ROTATION), MIN_PITCH_ROTATION);
                  pitchDiff = absolutePitch - viewPoint.getPitch();
                  Vector axis = relative.clone().normalize().crossProduct(new Vector(0, 1, 0));
                  relative.rotateAroundNonUnitAxis(axis.normalize(), Math.toRadians(pitchDiff));

                  Location targetLocation;
                  Vector direction = relative.clone().normalize();
                  RayTraceResult result = viewPoint.getWorld().rayTraceBlocks(target, direction, distance + 1.0, FluidCollisionMode.NEVER, true);
                  if (result != null) {
                    targetLocation = result.getHitPosition().toLocation(viewPoint.getWorld());
                    targetLocation.add(direction.clone().multiply(new Vector(-0.6, -1.6, -0.6)));
                  } else {
                    targetLocation = relative.toLocation(viewPoint.getWorld()).add(target);
                  }

                  moveTo(targetLocation);
                } else {
                  rotation = new Vector2f();
                }

                float clampedPitch = Math.max(Math.min(packet.xRot, MAX_PITCH_ROTATION), MIN_PITCH_ROTATION);
                rotation.set(clampedPitch, packet.yRot);
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

  @Override
  public void calculateDirection() {
    this.direction.copy(target.clone().subtract(viewPoint).toVector().normalize());
    this.viewPoint.setDirection(direction);
    updateViewerPosition();
  }

  public void calculateDistance() {
    distance = viewPoint.distance(target);
  }

  @Override
  public void setTarget(Location target) {
    this.target.set(target.x(), target.y(), target.z());
    calculateDirection();
    calculateDistance();
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
