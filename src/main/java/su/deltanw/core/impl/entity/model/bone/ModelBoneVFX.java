package su.deltanw.core.impl.entity.model.bone;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import su.deltanw.core.api.entity.model.EntityModel;
import su.deltanw.core.api.entity.model.bone.ModelBone;

public class ModelBoneVFX extends AbstractModelBone {

  private Location position = new Location(null, 0, 0, 0);

  public ModelBoneVFX(Vector pivot, String name, Vector rotation, EntityModel model, float scale) {
    super(pivot, name, rotation, model, scale);
    entity = null;
  }

  public Location getPosition() {
    return position;
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
    if (offset == null) {
      return new Location(null, 0, 0, 0);
    }

    Vector p = offset.clone();
    p = applyTransform(p);
    p = calculateGlobalRotation(p);

    return p.multiply(0.25)
        .multiply(scale)
        .toLocation(position.getWorld())
        .add(model.getPosition());
  }

  @Override
  public Vector calculateRotation() {
    return new Vector();
  }

  @Override
  public void destroy() {
  }
}
