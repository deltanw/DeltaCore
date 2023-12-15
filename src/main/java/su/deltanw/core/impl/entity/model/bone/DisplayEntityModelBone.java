package su.deltanw.core.impl.entity.model.bone;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import su.deltanw.core.api.entity.model.EntityModel;
import su.deltanw.core.api.entity.model.bone.ModelBone;
import su.deltanw.core.api.entity.model.bone.ModelBoneViewable;
import su.deltanw.core.impl.entity.model.ModelMath;

import java.util.List;
import java.util.Objects;

public class DisplayEntityModelBone extends AbstractModelBone implements ModelBoneViewable {

  private static final EntityDataAccessor<Vector3f> TRANSLATION = new EntityDataAccessor<>(10, EntityDataSerializers.VECTOR3);
  private static final EntityDataAccessor<Vector3f> SCALE = new EntityDataAccessor<>(11, EntityDataSerializers.VECTOR3);
  private static final EntityDataAccessor<Quaternionf> ROTATION_RIGHT = new EntityDataAccessor<>(13, EntityDataSerializers.QUATERNION);

  private final World world;
  protected int sendTick = 0;
  private BoneEntity baseStand;

  public DisplayEntityModelBone(World world, Vector pivot, String name, Vector rotation, EntityModel model, float scale) {
    super(pivot, name, rotation, model, scale);
    this.world = world;

    if (offset != null) {
      Display.ItemDisplay entity = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, ((CraftWorld) world).getHandle());
      this.entity = new BoneEntity(entity, model);
      SynchedEntityData data = entity.getEntityData();
      data.set(SCALE, new Vector3f(scale));
      entity.setItemTransform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
      entity.setInterpolationDuration(3);
      entity.setViewRange(1000.0F);
    }
  }

  @Override
  public void setScale(float scale) {
    super.setScale(scale);
    if (entity != null) {
      entity.getEntity().getEntityData().set(SCALE, new Vector3f(scale));
      entity.updateMeta();
    }
  }

  @Override
  public void destroy() {
    super.destroy();
    if (baseStand != null) {
      baseStand.remove();
    }
  }

  @Override
  public Location calculatePosition() {
    return model.getPosition().toLocation(world);
  }

  private Vector calculatePosition0() {
    if (offset == null) {
      return new Vector();
    }
    Vector v = offset.clone();
    v = applyTransform(v);
    v = calculateGlobalRotation(v);
    return v.multiply(0.25).multiply(scale);
  }

  @Override
  public Vector calculateRotation() {
    Quaterniond q = calculateFinalAngle(ModelMath.quaternion(getPropogatedRotation()));
    return ModelMath.toEuler(q);
  }

  @Override
  public void display() {
    if (baseStand != null && !baseStand.getEntity().position().toVector3f().equals(model.getPosition().toVector().toVector3f())) {
      Location pos = model.getPosition();
      baseStand.getEntity().teleportTo(pos.getX(), pos.getY(), pos.getZ());
      ClientboundTeleportEntityPacket packet = new ClientboundTeleportEntityPacket(baseStand.getEntity());
      model.getViewers().forEach(p -> ((CraftPlayer) p).getHandle().connection.send(packet));
    }

    children.forEach(ModelBone::display);
    if (offset == null) {
      return;
    }

    if (sendTick % 2 == 0 && entity != null && entity.getEntity().getType() == EntityType.ITEM_DISPLAY) {
      Vector position = calculatePosition0();
      Quaterniond q = calculateFinalAngle(ModelMath.quaternion(getPropogatedRotation()));
      Quaterniond pq = ModelMath.quaternion(new Vector(0, 180 - model.getGlobalRotation(), 0));
      q = pq.mul(q);

      SynchedEntityData data = entity.getEntity().getEntityData();
      data.set(ROTATION_RIGHT, new Quaternionf(q));
      data.set(TRANSLATION, position.toVector3f());
      if (data.isDirty()) {
        ((Display.ItemDisplay) entity.getEntity()).setInterpolationDelay(0);
        entity.updateMeta();
      }
    }

    ++sendTick;
  }

  @Override
  public void spawn(Location location) {
    super.spawn(location);

    if (!(getParent() instanceof DisplayEntityModelBone)) {
      ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, ((CraftWorld) world).getHandle());
      baseStand = new BoneEntity(armorStand, model) {
        @Override
        protected void spawn(Player player, List<Packet<?>> packets) {
          super.spawn(player, packets);
          baseStand.getEntity().passengers = model.getParts().stream()
              .map(ModelBone::<BoneEntity>getEntity)
              .filter(Objects::nonNull)
              .map(BoneEntity::getEntity)
              .filter(e -> e.getType() == EntityType.ITEM_DISPLAY)
              .collect(ImmutableList.toImmutableList());
          var packet = new ClientboundSetPassengersPacket(baseStand.getEntity());
          ((CraftPlayer) player).getHandle().connection.send(packet);
        }
      };
      armorStand.setNoGravity(true);
      armorStand.setInvisible(true);
      armorStand.setMarker(true);
      armorStand.setPos(location.getX(), location.getY(), location.getZ());
      armorStand.setRot(location.getYaw(), location.getPitch());
      baseStand.spawn(location);
    }
  }

  @Override
  public void setState(String state) {
    if (entity != null && entity.getEntity().getType() == EntityType.ITEM_DISPLAY) {
      if (state.equals("invisible")) {
        ((Display.ItemDisplay) entity.getEntity()).setItemStack(ItemStack.EMPTY);
        entity.updateMeta();
        return;
      }

      ItemStack item = items.get(state);
      if (item != null) {
        ((Display.ItemDisplay) entity.getEntity()).setItemStack(item);
        entity.updateMeta();
      }
    }
  }
}
