package su.deltanw.core.api.entity.model;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import su.deltanw.core.api.entity.model.animation.AnimationDirection;
import su.deltanw.core.api.entity.model.bone.ModelBone;

import java.util.List;
import java.util.Set;

public interface EntityModel {

  String getId();

  Vector getPivot();

  double getGlobalRotation();

  void setGlobalRotation(double rotation);

  Vector getGlobalOffset();

  Location getTrackingPosition();

  Location getPosition();

  void setPosition(Location pos);

  void setState(String state);

  void destroy();

  void despawn(Player player);

  void despawn();

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

  void setNameTag(Component nameTag);

  Component getNameTag();

  <E> E getNameTagBone();

  Vector getOffset(String bone);

  Vector getDiff(String bone);

  void triggerAnimationEnd(String animation, AnimationDirection direction);

  default void spawn(Location location) {
    spawn(location, 1);
  }

  void spawn(Location location, float scale);

  void spawn(Player player);

  void setScale(float scale);

  <I> ModelEngine<I> getEngine();

  Set<Player> getViewers();

  void addViewer(Player player);

  void removeViewer(Player player);
}
