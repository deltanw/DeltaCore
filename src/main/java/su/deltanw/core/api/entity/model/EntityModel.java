package su.deltanw.core.api.entity.model;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import su.deltanw.core.api.entity.model.animation.AnimationDirection;
import su.deltanw.core.api.entity.model.bone.ModelBone;
import su.deltanw.core.impl.entity.model.bone.BoneEntity;

import java.util.List;
import java.util.Set;

public interface EntityModel {

  String getId();

  Vector getPivot();

  double getGlobalRotation();

  void setGlobalRotation(double rotation);

  Vector getGlobalOffset();

  Location getPosition();

  void setPosition(Location pos);

  void setState(String state);

  void destroy();

  void removeHitboxes();

  void mountEntity(Entity entity);

  void dismountEntity(Entity entity);

  List<Entity> getPassengers();

  Vector getVfx(String name);

  ModelBone getPart(String boneName);

  void display();

  void setHeadRotation(double rotation);

  List<ModelBone> getParts();

  ModelBone getSeat();

  Vector getBoneAtTime(String animation, String bone, int time);

  void setNameTagEntity(BoneEntity entity);

  BoneEntity getNameTagEntity();

  Vector getOffset(String bone);

  Vector getDiff(String bone);

  void triggerAnimationEnd(String animation, AnimationDirection direction);

  void setScale(float scale);

  <I> ModelEngine<I> getEngine();

  Set<Player> getViewers();

  void addViewer(Player player);

  void removeViewer(Player player);
}