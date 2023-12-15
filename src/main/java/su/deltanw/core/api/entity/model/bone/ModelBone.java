package su.deltanw.core.api.entity.model.bone;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.joml.Quaterniond;
import su.deltanw.core.api.entity.model.animation.ModelAnimation;

public interface ModelBone {

  void spawn(Location location);

  Vector applyTransform(Vector v);

  void display();

  void destroy();

  Vector simulateTransform(Vector v, String animation, int time);

  Vector simulateRotation(String animation, int time);

  void setState(String state);

  void setParent(ModelBone parent);

  String getName();

  <E> E getEntity();

  Vector getOffset();

  ModelBone getParent();

  Vector getPropogatedRotation();

  Vector calculateRotation(Vector v, Vector rotation, Vector pivot);

  Quaterniond calculateFinalAngle(Quaterniond q);

  Location calculatePosition();

  Vector calculateRotation();

  void addChild(ModelBone child);

  void setScale(float scale);

  void addAnimation(ModelAnimation animation);
}