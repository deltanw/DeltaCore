package su.deltanw.core.impl.entity.model.bone;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import su.deltanw.core.api.entity.model.EntityModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BoneEntity {

  private final Entity entity;
  private final EntityModel model;

  public BoneEntity(Entity entity, EntityModel model) {
    this.entity = entity;
    this.model = model;
  }

  private List<Pair<EquipmentSlot, ItemStack>> getEquipment(LivingEntity living) {
    List<Pair<EquipmentSlot, ItemStack>> list = new ArrayList<>();
    EquipmentSlot[] equipmentSlots = EquipmentSlot.values();

    for (EquipmentSlot equipmentSlot : equipmentSlots) {
      ItemStack item = living.getItemBySlot(equipmentSlot);
      if (!item.isEmpty()) {
        list.add(Pair.of(equipmentSlot, item));
      }
    }

    return list;
  }

  private List<Packet<?>> createSpawnPackets() {
    List<Packet<?>> packets = new ArrayList<>(4);
    packets.add(new ClientboundAddEntityPacket(entity));
    List<SynchedEntityData.DataValue<?>> metadata = entity.getEntityData().getNonDefaultValues();
    if (metadata != null) {
      packets.add(new ClientboundSetEntityDataPacket(entity.getId(), metadata));
    }
    if ((entity.getType() == EntityType.ZOMBIE || entity.getType() == EntityType.ARMOR_STAND)
        && entity instanceof LivingEntity living) {
      List<Pair<EquipmentSlot, ItemStack>> list = getEquipment(living);

      if (!list.isEmpty()) {
        packets.add(new ClientboundSetEquipmentPacket(entity.getId(), list));
      }
    }
    return packets;
  }

  private List<Packet<?>> createRemovePackets() {
    return List.of(new ClientboundRemoveEntitiesPacket(entity.getId()));
  }

  protected void spawn(Player player, List<Packet<?>> packets) {
    packets.forEach(((CraftPlayer) player).getHandle().connection::send);
  }

  public void spawn(Location location) {
    entity.setPos(location.getX(), location.getY(), location.getZ());
    entity.setRot(location.getYaw(), location.getPitch());
    Set<Player> viewers = model.getViewers();
    if (viewers.isEmpty()) {
      return;
    }
    List<Packet<?>> packets = createSpawnPackets();
    viewers.forEach(player -> spawn(player, packets));
  }

  public void spawn(Player player) {
    List<Packet<?>> packets = createSpawnPackets();
    spawn(player, packets);
  }

  public void updateMeta() {
    List<SynchedEntityData.DataValue<?>> data = entity.getEntityData().packDirty();
    if (data == null || data.isEmpty()) {
      return;
    }
    Packet<?> packet = new ClientboundSetEntityDataPacket(entity.getId(), data);
    model.getViewers().forEach(player -> ((CraftPlayer) player).getHandle().connection.send(packet));
  }

  public void updateEquipment() {
    if (!(entity instanceof LivingEntity living)) {
      return;
    }

    List<Pair<EquipmentSlot, ItemStack>> list = getEquipment(living);

    if (!list.isEmpty()) {
      Packet<?> packet = new ClientboundSetEquipmentPacket(entity.getId(), list);
      model.getViewers().forEach(player -> ((CraftPlayer) player).getHandle().connection.send(packet));
    }
  }

  public void remove() {
    Set<Player> viewers = model.getViewers();
    if (viewers.isEmpty()) {
      return;
    }
    List<Packet<?>> packets = createRemovePackets();
    viewers.forEach(player -> packets.forEach(((CraftPlayer) player).getHandle().connection::send));
  }

  public void remove(Player player) {
    List<Packet<?>> packets = createRemovePackets();
    packets.forEach(((CraftPlayer) player).getHandle().connection::send);
  }

  public Entity getEntity() {
    return entity;
  }

  public EntityModel getModel() {
    return model;
  }
}
