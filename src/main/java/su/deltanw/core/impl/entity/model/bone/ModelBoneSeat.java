package su.deltanw.core.impl.entity.model.bone;

import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.util.Vector;
import org.joml.Quaterniond;
import su.deltanw.core.api.entity.model.EntityModel;
import su.deltanw.core.api.entity.model.bone.ModelBone;
import su.deltanw.core.impl.entity.model.ModelMath;

public class ModelBoneSeat extends AbstractModelBone {

  public ModelBoneSeat(World world, Vector pivot, String name, Vector rotation, EntityModel model, float scale) {
    super(pivot, name, rotation, model, scale);
    if (offset != null) {
      Zombie zombie = new Zombie(EntityType.ZOMBIE, ((CraftWorld) world).getHandle());
      zombie.setNoAi(true);
      zombie.setSilent(true);
      zombie.setNoGravity(true);
      zombie.setInvisible(true);
      entity = new BoneEntity(zombie, model);
    }
  }

  @Override
  public void setState(String state) {
  }

  @Override
  public Location calculatePosition() {
    if (offset == null) {
      return new Location(null, 0, 0, 0);
    }

    Vector rotation = calculateRotation();
    Vector p = applyTransform(offset.clone());
    p = calculateGlobalRotation(p);

    return p.multiply(0.25)
        .multiply(scale)
        .add(model.getPosition().toVector())
        .add(model.getGlobalOffset())
        .toLocation(null, (float) -rotation.getY(), (float) rotation.getX());
  }

  @Override
  public Vector calculateRotation() {
    Quaterniond q = ModelMath.quaternion(new Vector(0, 180 - model.getGlobalRotation(), 0));
    return ModelMath.toEulerYZX(q);
  }

  @Override
  public void display() {
    children.forEach(ModelBone::display);
    if (offset == null) {
      return;
    }

    Location position = calculatePosition();
    entity.getEntity().teleportTo(position.getX(), position.getY(), position.getZ());
    entity.getEntity().setRot(position.getYaw(), position.getPitch());
    ClientboundTeleportEntityPacket packet = new ClientboundTeleportEntityPacket(entity.getEntity());
    model.getViewers().forEach(p -> ((CraftPlayer) p).getHandle().connection.send(packet));
  }
}
