package su.deltanw.core.impl.entity.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import su.deltanw.core.api.entity.model.EntityModel;
import su.deltanw.core.api.entity.model.ModelEngine;
import su.deltanw.core.api.entity.model.PlayerModel;
import su.deltanw.core.api.entity.model.animation.AnimationDirection;
import su.deltanw.core.api.entity.model.bone.ModelBone;
import su.deltanw.core.api.entity.model.bone.ModelBoneHead;
import su.deltanw.core.api.entity.model.bone.ModelBoneViewable;
import su.deltanw.core.api.entity.model.event.AnimationCompleteEvent;
import su.deltanw.core.impl.entity.model.bone.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractEntityModel implements EntityModel {

  protected final LinkedHashMap<String, ModelBone> parts = new LinkedHashMap<>();
  protected final Set<AbstractModelBone> viewableBones = new LinkedHashSet<>();
  protected final Set<ModelBoneHitbox> hittableBones = new LinkedHashSet<>();
  protected final Map<String, ModelBoneVFX> vfxBones = new LinkedHashMap<>();
  private final Collection<ModelBone> additionalBones = new ArrayList<>();
  private ModelBoneSeat seat;
  private ModelBoneHead head;
  private ModelBoneNameTag nameTag;
  private Location position;
  private double globalRotation;
  private final Set<Player> viewers = ConcurrentHashMap.newKeySet();
  private final ModelEngine<ItemStack> modelEngine;

  public AbstractEntityModel(ModelEngine<ItemStack> modelEngine) {
    this.modelEngine = modelEngine;
  }

  @Override
  public double getGlobalRotation() {
    return globalRotation;
  }

  @Override
  public Location getPosition() {
    return position.clone();
  }

  @Override
  public void triggerAnimationEnd(String animation, AnimationDirection direction) {
    Bukkit.getPluginManager().callEvent(new AnimationCompleteEvent(this, animation, direction));
  }

  @Override
  public void spawn(Location location, float scale) {
    this.position = location.toVector().toLocation(location.getWorld());

    JsonObject loadedModel = modelEngine.getModelLoader().loadModel(getId());
    setGlobalRotation(location.getYaw());

    loadBones(location.getWorld(), loadedModel, scale);

    for (ModelBone modelBonePart : parts.values()) {
      if (modelBonePart instanceof ModelBoneViewable && modelBonePart instanceof AbstractModelBone impl) {
        viewableBones.add(impl);
      } else if (modelBonePart instanceof ModelBoneHitbox hitbox) {
        hittableBones.add(hitbox);
      } else if (modelBonePart instanceof ModelBoneVFX vfx) {
        vfxBones.put(vfx.getName(), vfx);
      }

      modelBonePart.spawn(modelBonePart.calculatePosition());
    }

    display();

    setState("normal");
  }

  protected void loadBones(World world, JsonObject loadedModel, float scale) {
    for (JsonElement bone :
        loadedModel.get("minecraft:geometry")
            .getAsJsonArray().get(0).getAsJsonObject()
            .get("bones").getAsJsonArray()) {
      JsonElement pivot = bone.getAsJsonObject().get("pivot");
      String name = bone.getAsJsonObject().get("name").getAsString();

      Vector boneRotation = modelEngine.getPos(bone.getAsJsonObject().get("rotation")).orElseGet(Vector::new).multiply(new Vector(-1, -1, 1));
      Vector pivotPos = modelEngine.getPos(pivot).orElseGet(Vector::new).multiply(new Vector(-1, 1, 1));

      ModelBone modelBonePart = null;

      if (name.equals("nametag") || name.equals("tag_name")) {
        nameTag = new ModelBoneNameTag(pivotPos, name, boneRotation, this, null, scale);
        modelBonePart = nameTag;
      } else if (name.contains("hitbox")) {
        Collection<ModelBoneHitbox> additionalParts = addHitboxParts(world, pivotPos, name, boneRotation, this, bone.getAsJsonObject().getAsJsonArray("cubes"), parts, scale);
        additionalBones.addAll(additionalParts);
      } else if (name.contains("vfx")) {
        modelBonePart = new ModelBoneVFX(pivotPos, name, boneRotation, this, scale);
      } else if (name.contains("seat")) {
        seat = new ModelBoneSeat(world, pivotPos, name, boneRotation, this, scale);
        modelBonePart = seat;
      } else if (name.equals("head")) {
        modelBonePart = new DisplayEntityModelBoneHead(world, pivotPos, name, boneRotation, this, scale);
        head = (ModelBoneHead) modelBonePart;
      } else {
        if (this instanceof PlayerModel) {
          modelBonePart = new ArmorStandModelBone(world, pivotPos, name, boneRotation, this, scale);
        } else {
          modelBonePart = new DisplayEntityModelBone(world, pivotPos, name, boneRotation, this, scale);
        }
      }

      if (modelBonePart != null) {
        parts.put(name, modelBonePart);
      }
    }

    for (JsonElement bone :
        loadedModel.get("minecraft:geometry")
            .getAsJsonArray().get(0).getAsJsonObject()
            .get("bones").getAsJsonArray()) {
      String name = bone.getAsJsonObject().get("name").getAsString();
      JsonElement parent = bone.getAsJsonObject().get("parent");
      String parentString = parent == null ? null : parent.getAsString();

      if (parentString != null) {
        ModelBone child = parts.get(name);
        if (child == null) {
          continue;
        }
        ModelBone parentBone = parts.get(parentString);
        child.setParent(parentBone);
        parentBone.addChild(child);
      }
    }
  }

  private Collection<ModelBoneHitbox> addHitboxParts(World world, Vector pivotPos, String name, Vector boneRotation, EntityModel model, JsonArray cubes, LinkedHashMap<String, ModelBone> parts, float scale) {
    if (cubes.size() < 1) {
      return List.of();
    }

    JsonElement cube = cubes.get(0);
    JsonArray sizeArray = cube.getAsJsonObject().get("size").getAsJsonArray();
    JsonArray p = cube.getAsJsonObject().get("pivot").getAsJsonArray();

    Vector sizePoint = new Vector(sizeArray.get(0).getAsFloat(), sizeArray.get(1).getAsFloat(), sizeArray.get(2).getAsFloat());
    Vector pivotPoint = new Vector(p.get(0).getAsFloat(), p.get(1).getAsFloat(), p.get(2).getAsFloat());

    Vector newOffset = pivotPoint.multiply(new Vector(-1, 1, 1));

    ModelBoneHitbox created = new ModelBoneHitbox(world, pivotPos, name, boneRotation, model, newOffset, sizePoint.getX(), sizePoint.getY(), cubes, true, scale);
    parts.put(name, created);

    return created.getParts();
  }

  @Override
  public void setScale(float scale) {
    for (ModelBone modelBonePart : parts.values()) {
      modelBonePart.setScale(scale);
    }
  }

  @Override
  public void setNameTagEntity(BoneEntity entity) {
    if (nameTag != null) {
      nameTag.linkEntity(entity);
    }
  }

  @Override
  public BoneEntity getNameTagEntity() {
    if (nameTag == null) {
      return null;
    }
    return nameTag.getEntity();
  }

  @Override
  public void setPosition(Location location) {
    if (this.position != null) {
      World world = position.getWorld();
      this.position = location.clone();
      position.setWorld(world);
    } else {
      this.position = location.clone();
    }
  }

  @Override
  public void setGlobalRotation(double globalRotation) {
    globalRotation %= 360;
    if (globalRotation < -180.F) {
      globalRotation += 360.F;
    } else if (globalRotation > 180.F) {
      globalRotation -= 360.F;
    }
    this.globalRotation = globalRotation;
  }

  @Override
  public void mountEntity(Entity entity) {
    if (seat != null) {
      seat.getEntity().getEntity().getBukkitEntity().addPassenger(entity);
    }
  }

  @Override
  public void dismountEntity(Entity entity) {
    if (seat != null) {
      seat.getEntity().getEntity().getBukkitEntity().removePassenger(entity);
    }
  }

  @Override
  public List<Entity> getPassengers() {
    if (seat == null || seat.getEntity() == null) {
      return List.of();
    }

    return seat.getEntity().getEntity().getBukkitEntity().getPassengers();
  }

  @Override
  public void setState(String state) {
    for (ModelBone part : viewableBones) {
      part.setState(state);
    }
  }

  public ModelBone getPart(String boneName) {
    return parts.get(boneName);
  }

  @Override
  public ModelBone getSeat() {
    return seat;
  }

  @Override
  public void display() {
    for (ModelBone modelBone : parts.values()) {
      if (modelBone.getParent() == null) {
        modelBone.display();
      }
    }
  }

  @Override
  public void destroy() {
    for (ModelBone modelBone : parts.values()) {
      modelBone.destroy();
    }

    viewableBones.clear();
    hittableBones.clear();
    vfxBones.clear();
    parts.clear();
  }

  @Override
  public void removeHitboxes() {
    hittableBones.forEach(ModelBone::destroy);
    hittableBones.clear();
  }

  @Override
  public Vector getVfx(String name) {
    ModelBoneVFX vfx = vfxBones.get(name);
    if (vfx == null) {
      return null;
    }
    return vfx.getPosition().toVector();
  }

  @Override
  public void setHeadRotation(double rotation) {
    if (head != null) {
      head.setRotation(rotation);
    }
  }

  @Override
  public List<ModelBone> getParts() {
    List<ModelBone> result = parts.values().stream()
        .filter(Objects::nonNull).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    result.addAll(additionalBones);
    return result;
  }

  @Override
  public Vector getBoneAtTime(String animation, String boneName, int time) {
    ModelBone bone = parts.get(boneName);

    Vector p = bone.getOffset();
    p = bone.simulateTransform(p, animation, time);
    p = bone.calculateRotation(p, new Vector(0, 180 - getGlobalRotation(), 0), getPivot());

    return p.divide(new Vector(6.4, 6.4, 6.4))
        .add(getPosition().toVector())
        .add(getGlobalOffset());
  }

  @Override
  public Vector getPivot() {
    return new Vector();
  }

  @Override
  public Vector getGlobalOffset() {
    return new Vector();
  }

  @Override
  public Vector getDiff(String bone) {
    return modelEngine.getDiffMappings().get(getId() + "/" + bone);
  }

  @Override
  public Vector getOffset(String bone) {
    return modelEngine.getOffsetMappings().get(getId() + "/" + bone);
  }

  @Override
  public void addViewer(Player player) {
    viewers.add(player);
  }

  @Override
  public void removeViewer(Player player) {
    viewers.remove(player);
  }

  @Override
  public Set<Player> getViewers() {
    return viewers;
  }

  @Override
  @SuppressWarnings("unchecked")
  public ModelEngine<ItemStack> getEngine() {
    return modelEngine;
  }
}
