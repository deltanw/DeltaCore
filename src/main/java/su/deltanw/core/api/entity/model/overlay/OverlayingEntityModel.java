package su.deltanw.core.api.entity.model.overlay;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;
import su.deltanw.core.api.entity.model.EntityModel;
import su.deltanw.core.api.entity.model.ModelEngine;
import su.deltanw.core.api.entity.model.animation.AnimationDirection;
import su.deltanw.core.api.entity.model.bone.ModelBone;
import su.deltanw.core.api.entity.model.factory.EntityModelFactory;

import java.util.List;
import java.util.Set;

public class OverlayingEntityModel<T extends EntityModel> implements EntityModel {

  protected T underlyingModel;

  public OverlayingEntityModel(@NonNull T underlyingModel) {
    this.underlyingModel = underlyingModel;
  }

  public OverlayingEntityModel(EntityModelFactory<T, ?> factory) {
    this(factory.createEntityModelBase());
  }

  @Override
  public String getId() {
    return underlyingModel.getId();
  }

  @Override
  public Vector getPivot() {
    return underlyingModel.getPivot();
  }

  @Override
  public double getGlobalRotation() {
    return underlyingModel.getGlobalRotation();
  }

  @Override
  public void setGlobalRotation(double rotation) {
    underlyingModel.setGlobalRotation(rotation);
  }

  @Override
  public Vector getGlobalOffset() {
    return underlyingModel.getGlobalOffset();
  }

  @Override
  public Location getTrackingPosition() {
    return underlyingModel.getTrackingPosition();
  }

  @Override
  public Location getPosition() {
    return underlyingModel.getPosition();
  }

  @Override
  public void setPosition(Location pos) {
    underlyingModel.setPosition(pos);
  }

  @Override
  public void setState(String state) {
    underlyingModel.setState(state);
  }

  @Override
  public void destroy() {
    underlyingModel.destroy();
  }

  @Override
  public void despawn(Player player) {
    underlyingModel.despawn(player);
  }

  @Override
  public void despawn() {
    underlyingModel.despawn();
  }

  @Override
  public void removeHitboxes() {
    underlyingModel.removeHitboxes();
  }

  @Override
  public void mountEntity(Entity entity) {
    underlyingModel.mountEntity(entity);
  }

  @Override
  public void dismountEntity(Entity entity) {
    underlyingModel.dismountEntity(entity);
  }

  @Override
  public List<Entity> getPassengers() {
    return underlyingModel.getPassengers();
  }

  @Override
  public Vector getVfx(String name) {
    return underlyingModel.getVfx(name);
  }

  @Override
  public ModelBone getPart(String boneName) {
    return underlyingModel.getPart(boneName);
  }

  @Override
  public void display() {
    underlyingModel.display();
  }

  @Override
  public void setHeadRotation(double rotation) {
    underlyingModel.setHeadRotation(rotation);
  }

  @Override
  public List<ModelBone> getParts() {
    return underlyingModel.getParts();
  }

  @Override
  public ModelBone getSeat() {
    return underlyingModel.getSeat();
  }

  @Override
  public Vector getBoneAtTime(String animation, String bone, int time) {
    return underlyingModel.getBoneAtTime(animation, bone, time);
  }

  @Override
  public void setNameTag(Component nameTag) {
    underlyingModel.setNameTag(nameTag);
  }

  @Override
  public Component getNameTag() {
    return underlyingModel.getNameTag();
  }

  @Override
  public <E> E getNameTagBone() {
    return underlyingModel.getNameTagBone();
  }

  @Override
  public Vector getOffset(String bone) {
    return underlyingModel.getOffset(bone);
  }

  @Override
  public Vector getDiff(String bone) {
    return underlyingModel.getDiff(bone);
  }

  @Override
  public void triggerAnimationEnd(String animation, AnimationDirection direction) {
    underlyingModel.triggerAnimationEnd(animation, direction);
  }

  @Override
  public void spawn(Location location) {
    underlyingModel.spawn(location);
  }

  @Override
  public void spawn(Location location, float scale) {
    underlyingModel.spawn(location, scale);
  }

  @Override
  public void spawn(Player player) {
    underlyingModel.spawn(player);
  }

  @Override
  public void setScale(float scale) {
    underlyingModel.setScale(scale);
  }

  @Override
  public <I> ModelEngine<I> getEngine() {
    return underlyingModel.getEngine();
  }

  @Override
  public Set<Player> getViewers() {
    return underlyingModel.getViewers();
  }

  @Override
  public void addViewer(Player player) {
    underlyingModel.addViewer(player);
  }

  @Override
  public void removeViewer(Player player) {
    underlyingModel.removeViewer(player);
  }

  public void setUnderlyingModel(T underlyingModel) {
    this.underlyingModel = underlyingModel;
  }

  public T getUnderlyingModel() {
    return underlyingModel;
  }
}
