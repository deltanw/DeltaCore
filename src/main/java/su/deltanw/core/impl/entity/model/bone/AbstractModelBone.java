package su.deltanw.core.impl.entity.model.bone;

import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.joml.Quaterniond;
import su.deltanw.core.api.entity.model.EntityModel;
import su.deltanw.core.api.entity.model.animation.AnimationType;
import su.deltanw.core.api.entity.model.animation.ModelAnimation;
import su.deltanw.core.api.entity.model.bone.ModelBone;
import su.deltanw.core.impl.entity.model.ModelMath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractModelBone implements ModelBone {

  protected final List<ModelBone> children = new ArrayList<>();
  protected final List<ModelAnimation> animations = new ArrayList<>();
  protected final Map<String, ItemStack> items;

  protected final EntityModel model;
  private final Vector originalPivot;
  protected final Vector diff;
  protected final Vector pivot;
  protected final String name;
  protected float scale;
  protected Vector offset;
  protected Vector rotation;
  protected ModelBone parent;
  protected BoneEntity entity;

  public AbstractModelBone(Vector pivot, String name, Vector rotation, EntityModel model, float scale) {
    this.name = name;
    this.rotation = rotation;
    this.model = model;
    this.diff = model.getDiff(name);
    this.offset = model.getOffset(name);
    this.originalPivot = pivot;
    this.pivot = pivot.clone();
    if (this.diff != null) {
      this.pivot.add(this.diff);
    }
    this.items = model.<ItemStack>getEngine().getItems(model.getId(), name);
    this.scale = scale;
  }

  @Override
  @SuppressWarnings("unchecked")
  public BoneEntity getEntity() {
    return entity;
  }

  @Override
  public ModelBone getParent() {
    return parent;
  }

  @Override
  public void setParent(ModelBone parent) {
    this.parent = parent;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setScale(float scale) {
    this.scale = scale;
  }

  public Vector calculateGlobalRotation(Vector endPos) {
    return calculateRotation(endPos, new Vector(0, 180 - model.getGlobalRotation(), 0), model.getPivot());
  }

  @Override
  public Vector calculateRotation(Vector v, Vector rotation, Vector pivot) {
    Vector position = v.clone().subtract(pivot);
    return ModelMath.rotate(position, rotation).add(pivot);
  }

  @Override
  public Vector simulateTransform(Vector v, String animation, int time) {
    Vector endPos = v.clone();

    if (diff != null) {
      endPos = calculateRotation(endPos, simulateRotation(animation, time), pivot.clone().subtract(this.diff));
    } else {
      endPos = calculateRotation(endPos, simulateRotation(animation, time), pivot);
    }

    for (ModelAnimation currentAnimation : animations) {
      if (currentAnimation == null || !currentAnimation.name().equals(animation)) {
        continue;
      }

      if (currentAnimation.getType() == AnimationType.TRANSLATION) {
        var calculatedTransform = currentAnimation.getTransformAtTime(time);
        endPos.add(calculatedTransform);
      }
    }

    if (parent != null) {
      endPos = parent.simulateTransform(endPos, animation, time);
    }

    return endPos;
  }

  @Override
  public Vector applyTransform(Vector v) {
    Vector endPos = v.clone();
    if (diff != null) {
      endPos = calculateRotation(endPos, getPropogatedRotation(), pivot.clone().subtract(diff));
    } else {
      endPos = calculateRotation(endPos, getPropogatedRotation(), pivot);
    }

    for (ModelAnimation currentAnimation : animations) {
      if (currentAnimation != null && currentAnimation.isPlaying()) {
        if (currentAnimation.getType() == AnimationType.TRANSLATION) {
          var calculatedTransform = currentAnimation.getTransform();
          endPos.add(calculatedTransform);
        }
      }
    }

    if (parent != null) {
      endPos = parent.applyTransform(endPos);
    }

    return endPos;
  }

  @Override
  public Vector getPropogatedRotation() {
    Vector netTransform = new Vector();

    for (ModelAnimation currentAnimation : animations) {
      if (currentAnimation != null && currentAnimation.isPlaying()) {
        if (currentAnimation.getType() == AnimationType.ROTATION) {
          Vector calculatedTransform = currentAnimation.getTransform();
          netTransform.add(calculatedTransform);
        }
      }
    }

    return netTransform.add(rotation);
  }

  @Override
  public Vector simulateRotation(String animation, int time) {
    Vector netTransform = new Vector();

    for (ModelAnimation currentAnimation : animations) {
      if (currentAnimation == null || !currentAnimation.name().equals(animation)) {
        continue;
      }

      if (currentAnimation.getType() == AnimationType.ROTATION) {
        Vector calculatedTransform = currentAnimation.getTransformAtTime(time);
        netTransform.add(calculatedTransform);
      }
    }

    return netTransform.add(rotation);
  }

  @Override
  public Quaterniond calculateFinalAngle(Quaterniond q) {
    if (parent != null) {
      Quaterniond pq = parent.calculateFinalAngle(ModelMath.quaternion(parent.getPropogatedRotation()));
      q = pq.mul(q);
    }

    return q;
  }

  @Override
  public void addAnimation(ModelAnimation animation) {
    animations.add(animation);
  }

  @Override
  public void addChild(ModelBone child) {
    children.add(child);
  }

  @Override
  public void destroy() {
    children.forEach(ModelBone::destroy);
    children.clear();

    if (entity != null) {
      entity.remove();
    }
  }

  @Override
  public void spawn(Location location) {
    if (offset != null && entity != null) {
      entity.getEntity().setNoGravity(true);
      entity.getEntity().setSilent(true);
      entity.spawn(location);
    }
  }

  @Override
  public Vector getOffset() {
    return offset.clone();
  }

  @Override
  public Vector getPivot() {
    return originalPivot.clone();
  }

  public EntityModel getModel() {
    return model;
  }
}
