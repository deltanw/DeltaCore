package su.deltanw.core.impl.entity.model.bone;

import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.util.Vector;
import su.deltanw.core.api.entity.model.EntityModel;

public class ModelBoneNameTag extends AbstractModelBone {

  public ModelBoneNameTag(Vector pivot, String name, Vector rotation, EntityModel model, BoneEntity tagEntity, float scale) {
    super(pivot, name, rotation, model, scale);
    if (offset != null && tagEntity != null) {
      entity = tagEntity;
    }
  }

  public void linkEntity(BoneEntity entity) {
    this.entity = entity;
  }

  @Override
  public void setState(String state) {
  }

  @Override
  public void display() {
    if (offset == null || entity == null) {
      return;
    }

    Location position = calculatePosition();
    entity.getEntity().teleportTo(position.getX(), position.getY(), position.getZ());
    entity.getEntity().setRot(position.getYaw(), position.getPitch());
    ClientboundTeleportEntityPacket packet = new ClientboundTeleportEntityPacket(entity.getEntity());
    model.getViewers().forEach(p -> ((CraftPlayer) p).getHandle().connection.send(packet));
  }

  @Override
  public Location calculatePosition() {
    World world = model.getPosition().getWorld();
    if (offset == null) {
      return new Location(world, 0, 0, 0);
    }

    Vector p = offset.clone();
    p = applyTransform(p);
    p = calculateGlobalRotation(p);

    return p.multiply(0.25)
        .multiply(scale)
        .toLocation(world)
        .add(model.getPosition())
        .add(model.getGlobalOffset());
  }

  @Override
  public Vector calculateRotation() {
    return new Vector();
  }
}
