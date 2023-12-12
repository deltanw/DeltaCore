package su.deltanw.core.impl.entity.model.bone;

import org.bukkit.World;
import org.bukkit.util.Vector;
import su.deltanw.core.api.entity.model.EntityModel;
import su.deltanw.core.api.entity.model.bone.ModelBoneHead;

public class DisplayEntityModelBoneHead extends DisplayEntityModelBone implements ModelBoneHead {

  private double headRotation;

  public DisplayEntityModelBoneHead(World world, Vector pivot, String name, Vector rotation, EntityModel model, float scale) {
    super(world, pivot, name, rotation, model, scale);
  }

  @Override
  public Vector getPropogatedRotation() {
    return super.getPropogatedRotation().add(new Vector(0, headRotation, 0));
  }

  @Override
  public void setRotation(double rotation) {
    headRotation = rotation;
  }
}
