package su.deltanw.core.impl.entity.model.bone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import su.deltanw.core.Core;
import su.deltanw.core.api.entity.model.EntityModel;
import su.deltanw.core.api.entity.model.animation.ModelAnimation;
import su.deltanw.core.api.entity.model.bone.ModelBone;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ModelBoneHitbox extends AbstractModelBone {

  private final World world;
  private final JsonArray cubes;
  protected Location actualPosition = null;
  Collection<ModelBoneHitbox> illegalChildren = new ConcurrentLinkedDeque<>();

  public ModelBoneHitbox(World world, Vector pivot, String name, Vector rotation, EntityModel model, Vector newOffset,
                         double sizeX, double sizeY, JsonArray cubes, boolean parent, float scale) {
    super(pivot, name, rotation, model, scale);

    this.world = world;
    this.cubes = cubes;

    if (parent) {
      generateStands(cubes, pivot, name, rotation, model);
      offset = null;
    } else {
      if (offset != null) {
        Interaction interaction = new Interaction(EntityType.INTERACTION, ((CraftWorld) world).getHandle());
        entity = new BoneEntity(interaction, model) {
          @Override
          protected void spawn(Player player, List<Packet<?>> packets) {
            super.spawn(player, packets);
            Packet<?> packet = new ClientboundTeleportEntityPacket(entity.getEntity());
            ((CraftPlayer) player).getHandle().connection.send(packet);
          }
        };

        offset = newOffset;
        interaction.setHeight((float) (sizeY / 4.0F) * scale);
        interaction.setWidth((float) (sizeX / 4.0F) * scale);
      }
    }
  }

  public void generateStands(JsonArray cubes, Vector pivotPos, String name, Vector boneRotation, EntityModel model) {
    for (JsonElement cube : cubes) {
      JsonArray sizeArray = cube.getAsJsonObject().get("size").getAsJsonArray();
      JsonArray p = cube.getAsJsonObject().get("pivot").getAsJsonArray();
      JsonArray origin = cube.getAsJsonObject().get("origin").getAsJsonArray();

      Vector sizePoint = new Vector(sizeArray.get(0).getAsFloat(), sizeArray.get(1).getAsFloat(), sizeArray.get(2).getAsFloat());
      Vector pivotPoint = new Vector(p.get(0).getAsFloat(), p.get(1).getAsFloat(), p.get(2).getAsFloat());
      Vector originPoint = new Vector(origin.get(0).getAsFloat(), origin.get(1).getAsFloat(), origin.get(2).getAsFloat());

      Vector originPivotDiff = pivotPoint.subtract(originPoint);

      double maxSize = Math.max(Math.min(Math.min(sizePoint.getX(), sizePoint.getY()), sizePoint.getZ()), 0.5);
      while (maxSize > (32 / scale)) {
        maxSize /= 2;
      }

      for (int x = 0; x < sizePoint.getX() / maxSize; ++x) {
        for (int y = 0; y < sizePoint.getY() / maxSize; ++y) {
          for (int z = 0; z < sizePoint.getZ() / maxSize; ++z) {
            Vector relativeSize = new Vector(maxSize, maxSize, maxSize);
            Vector relativePivotPoint = new Vector(x * maxSize, y * maxSize, z * maxSize);

            if (relativePivotPoint.getX() + relativeSize.getX() / 2 > sizePoint.getX()) {
              relativePivotPoint.setX(relativePivotPoint.getX() - sizePoint.getX());
            }
            if (relativePivotPoint.getY() + relativeSize.getY() > sizePoint.getY()) {
              relativePivotPoint.subtract(new Vector(0, relativePivotPoint.getY() + relativeSize.getY() - sizePoint.getY(), 0));
            }
            if (relativePivotPoint.getZ() + relativeSize.getZ() / 2 > sizePoint.getZ()) {
              relativePivotPoint.setZ(relativePivotPoint.getZ() - sizePoint.getZ());
            }

            Vector newOffset = pivotPoint.multiply(new Vector(-1, 1, 1)).subtract(new Vector(sizePoint.getX() / 2, originPivotDiff.getY(), sizePoint.getZ() / 2));
            newOffset.add(relativePivotPoint).add(new Vector(relativeSize.getX() / 2, 0, relativeSize.getZ() / 2));

            ModelBoneHitbox created = new ModelBoneHitbox(world, pivotPos, name, boneRotation, model, newOffset, relativeSize.getX(), relativeSize.getY(), cubes, false, scale);
            illegalChildren.add(created);
          }
        }
      }
    }
  }

  @Override
  public void setScale(float scale) {
    super.setScale(scale);
    destroy();
    illegalChildren.clear();
    generateStands(cubes, pivot, name, rotation, model);
    illegalChildren.forEach(modelBone -> modelBone.spawn(model.getPosition().toLocation(null)));
  }

  @Override
  public void setParent(ModelBone parent) {
    super.setParent(parent);
    illegalChildren.forEach(modelBone -> modelBone.setParent(parent));
  }

  public Collection<ModelBoneHitbox> getParts() {
    if (illegalChildren == null) {
      return List.of();
    }
    return illegalChildren;
  }

  @Override
  public void spawn(Location location) {
    illegalChildren.forEach(modelBone -> {
      modelBone.spawn(modelBone.calculatePosition().add(model.getPosition()));
      Bukkit.getScheduler().runTask(Core.getPlugin(Core.class), modelBone::display);
    });
    super.spawn(location);
  }

  @Override
  public void addAnimation(ModelAnimation animation) {
    super.addAnimation(animation);
    illegalChildren.forEach(modelBone -> modelBone.addAnimation(animation));
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

    if (actualPosition == null) {
      actualPosition = p.multiply(0.25).multiply(scale).toLocation(null);
      return actualPosition;
    }

    Location lp = actualPosition.clone();
    Location newLoc = p.multiply(0.25).multiply(scale).toLocation(null);
    actualPosition = Vector.fromJOML(lp.toVector().toVector3d().lerp(newLoc.toVector().toVector3d(), 0.25)).toLocation(null);

    return lp;
  }

  @Override
  public void destroy() {
    super.destroy();
    illegalChildren.forEach(ModelBone::destroy);
  }

  @Override
  public Vector calculateRotation() {
    return new Vector();
  }

  @Override
  public void display() {
    if (illegalChildren.size() > 0) {
      children.forEach(ModelBone::display);
      illegalChildren.forEach(ModelBone::display);
    }

    if (offset == null) {
      return;
    }

    if (entity != null) {
      Location position = calculatePosition();
      entity.getEntity().teleportTo(position.getX(), position.getY(), position.getZ());
      ClientboundTeleportEntityPacket packet = new ClientboundTeleportEntityPacket(entity.getEntity());
      model.getViewers().forEach(p -> ((CraftPlayer) p).getHandle().connection.send(packet));
    }
  }
}
