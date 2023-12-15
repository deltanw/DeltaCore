package su.deltanw.core.impl.entity.model.bone;

import net.minecraft.core.Rotations;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.util.Vector;
import org.joml.Quaterniond;
import su.deltanw.core.api.entity.model.EntityModel;
import su.deltanw.core.api.entity.model.bone.ModelBone;
import su.deltanw.core.api.entity.model.bone.ModelBoneViewable;
import su.deltanw.core.impl.entity.model.ModelMath;

public class ArmorStandModelBone extends AbstractModelBone implements ModelBoneViewable {

  private static final Vector NORMAL_SUB = new Vector(0, 1.377, 0);

  private final World world;
  private Vector lastRotation = new Vector();
  private Vector halfRotation = new Vector();
  private boolean update = true;

  public ArmorStandModelBone(World world, Vector pivot, String name, Vector rotation, EntityModel model, float scale) {
    super(pivot, name, rotation, model, scale);
    this.world = world;

    if (offset != null) {
      ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, ((CraftWorld) world).getHandle());
      entity = new BoneEntity(armorStand, model);
      armorStand.setInvisible(true);
      armorStand.setNoBasePlate(true);
    }
  }

  protected void setBoneRotation(Vector rotation) {
    ArmorStand armorStand = (ArmorStand) entity.getEntity();
    armorStand.setRightArmPose(Rotations.createWithoutValidityChecks(
        (float) rotation.getX(),
        -(float) rotation.getY(),
        -(float) rotation.getZ()
    ));
    entity.updateMeta();
  }

  @Override
  public Location calculatePosition() {
    if (offset == null) {
      return new Location(world, 0, 0, 0);
    }

    Vector p = applyTransform(offset);
    p = calculateGlobalRotation(p);

    return p.multiply(0.15625)
        .multiply(1.0 / 0.624)
        .subtract(NORMAL_SUB)
        .multiply(scale)
        .toLocation(world)
        .add(model.getPosition())
        .add(model.getGlobalOffset());
  }

  @Override
  public Vector calculateRotation() {
    Quaterniond q = calculateFinalAngle(ModelMath.quaternion(getPropogatedRotation()));
    Quaterniond pq = ModelMath.quaternion(new Vector(0, 180 - model.getGlobalRotation(), 0));
    q = pq.mul(q);

    return ModelMath.toEuler(q);
  }

  private double fixAngle(double angle) {
    angle %= 360;
    if (angle > 180) {
      angle -= 360;
    }
    if (angle < -180) {
      angle += 360;
    }
    return angle;
  }

  @Override
  public void display() {
    children.forEach(ModelBone::display);
    if (offset == null) {
      return;
    }

    if (update) {
      Vector rotation = calculateRotation();
      Vector step = rotation.clone().subtract(lastRotation);

      double stepX = fixAngle(step.getX());
      double stepY = fixAngle(step.getY());
      double stepZ = fixAngle(step.getZ());

      halfRotation = lastRotation.clone().add(new Vector(stepX / 2, stepY / 2, stepZ / 2));

      Location pos = calculatePosition();
      entity.getEntity().teleportTo(pos.getX(), pos.getY(), pos.getZ());
      ClientboundTeleportEntityPacket packet = new ClientboundTeleportEntityPacket(entity.getEntity());
      model.getViewers().forEach(p -> ((CraftPlayer) p).getHandle().connection.send(packet));

      setBoneRotation(lastRotation);
      lastRotation = rotation;
    } else {
      setBoneRotation(halfRotation);
    }

    update ^= true;
  }

  @Override
  public void setState(String state) {
    if (entity != null) {
      if (state.equals("invisible")) {
        ((ArmorStand) entity.getEntity()).setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        entity.updateEquipment();
      }

      ItemStack item = items.get(state);
      if (item != null) {
        ((ArmorStand) entity.getEntity()).setItemInHand(InteractionHand.MAIN_HAND, item);
        entity.updateEquipment();
      }
    }
  }
}
