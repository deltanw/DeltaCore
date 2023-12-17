package su.deltanw.core.impl.entity.model.bone;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import su.deltanw.core.api.entity.model.EntityModel;
import su.deltanw.core.api.entity.model.bone.ModelBone;

public class ModelBoneVFX extends AbstractModelBone {

  private Location position = null;

  public ModelBoneVFX(Vector pivot, String name, Vector rotation, EntityModel model, float scale) {
    super(pivot, name, rotation, model, scale);
    entity = null;
  }

  public Location getPosition() {
    if (position == null) {
      return new Location(model.getPosition().getWorld(), 0, 0, 0);
    } else {
      return position;
    }
  }

  @Override
  public void display() {
    children.forEach(ModelBone::display);
    if (offset == null) {
      return;
    }
    position = calculatePosition();
  }

  @Override
  public void setState(String state) {
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
        .add(model.getPosition());
  }

  @Override
  public Vector calculateRotation() {
    return new Vector();
  }

  @Override
  public void destroy() {
  }

  @Override
  public void despawn(Player player) {
  }
}
