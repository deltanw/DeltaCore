package su.deltanw.core.impl.model;

import com.jeff_media.customblockdata.CustomBlockData;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import su.deltanw.core.Core;
import su.deltanw.core.api.util.EntityUtil;

import java.util.*;

public class CustomModelListener implements Listener {

  private final Map<Player, Either<String, Location>> pendingOperations = new HashMap<>();
  private final Core plugin;

  public CustomModelListener(Core plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onCustomModelPlaced(BlockPlaceEvent event) {
    Either<String, Location> value = pendingOperations.get(event.getPlayer());
    if (value == null) {
      return;
    }

    if (value.left().isEmpty()) {
      return;
    }

    event.getPlayer().swingHand(event.getHand());
    pendingOperations.put(event.getPlayer(), Either.right(event.getBlockPlaced().getLocation()));
    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onCustomModelPlace(PlayerInteractEvent event) {
    if (event.useInteractedBlock() != Event.Result.ALLOW) {
      return;
    }

    if (pendingOperations.containsKey(event.getPlayer())) {
      return;
    }

    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }

    if (event.getClickedBlock() == null) {
      return;
    }

    if (event.getItem() == null) {
      return;
    }

    if (event.getHand() == null || !event.getHand().isHand()) {
      return;
    }

    if (event.getHand() == EquipmentSlot.OFF_HAND) {
      return;
    }

    if (!(event.getItem() instanceof CraftItemStack)) {
      return;
    }

    ItemStack nmsItem = ((CraftItemStack) event.getItem()).handle;
    if (!nmsItem.hasTag()) {
      return;
    }

    CompoundTag tag = nmsItem.getTag();
    assert tag != null;
    String customModelKey = tag.getString("delta__custom_model");
    if (customModelKey.isBlank()) {
      return;
    }

    NamespacedKey key = NamespacedKey.fromString(customModelKey);
    CustomModel customModel = CustomModel.get(key);
    if (customModel == null) {
      return;
    }

    ServerPlayer nmsPlayer = ((CraftPlayer) event.getPlayer()).getHandle();
    ServerLevel level = ((CraftWorld) event.getClickedBlock().getLocation().getWorld()).getHandle();
    Vector vector = event.getClickedPosition();
    assert vector != null;
    Vec3 nmsVec = new Vec3(vector.getX(), vector.getY(), vector.getZ());
    Direction nmsDirection = switch (event.getBlockFace()) {
      case DOWN -> Direction.DOWN;
      case EAST -> Direction.EAST;
      case WEST -> Direction.WEST;
      case NORTH -> Direction.NORTH;
      case SOUTH -> Direction.SOUTH;
      default -> Direction.UP;
    };
    Location blockLocation = event.getClickedBlock().getLocation();
    BlockPos nmsPos = new BlockPos(blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ());

    pendingOperations.put(event.getPlayer(), Either.left(customModelKey));
    nmsPlayer.gameMode.useItemOn(nmsPlayer, level, CustomModel.HITBOX_BLOCK_ITEM,
        event.getHand() == EquipmentSlot.OFF_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND,
        new BlockHitResult(nmsVec, nmsDirection, nmsPos, false));
    Either<String, Location> result = pendingOperations.remove(event.getPlayer());
    if (result == null) {
      return;
    }

    if (result.right().isEmpty()) {
      return;
    }

    Location placedLocation = result.right().get();

    for (BlockVector offset : customModel.hitboxes()) {
      Location hitboxLocation = placedLocation.clone().add(offset);
      if (hitboxLocation.getBlock().isSolid()) {
        return;
      }
    }

    int entityId = EntityUtil.allocatePrivateEntityId(event.getPlayer().getWorld());

    // TODO: Model rotation based on direction

    CustomBlockData blockData = new CustomBlockData(placedLocation.getBlock(), plugin);
    blockData.set(CustomModel.MODEL_PDC_KEY, PersistentDataType.STRING, customModelKey);
    blockData.set(CustomModel.MODEL_PDC_EID_KEY, PersistentDataType.INTEGER, entityId);

    for (BlockVector offset : customModel.hitboxes()) {
      Location hitboxLocation = placedLocation.clone().add(offset);
      hitboxLocation.getWorld().setBlockData(hitboxLocation, Bukkit.createBlockData(Material.BARRIER));

      CustomBlockData hitboxData = new CustomBlockData(hitboxLocation.getBlock(), plugin);
      hitboxData.set(CustomModel.MODEL_PDC_GROUP_KEY, PersistentDataType.INTEGER_ARRAY,
          new int[]{placedLocation.getBlockX(), placedLocation.getBlockY(), placedLocation.getBlockZ()});
    }

    List<Packet<?>> spawnPackets = customModel.createSpawnPackets(entityId, placedLocation.toVector().toBlockVector());

    for (Player player : placedLocation.getNearbyPlayers(16 * 33)) {
      ServerPlayerConnection connection = ((CraftPlayer) player).getHandle().connection;
      spawnPackets.forEach(connection::send);
    }

    event.setCancelled(true);
  }

  @EventHandler
  public void onPlayerBreak(BlockBreakEvent event) {
    if (event.isCancelled()) {
      return;
    }

    CustomBlockData blockData = new CustomBlockData(event.getBlock(), plugin);
    int[] modelPosition = blockData.get(CustomModel.MODEL_PDC_GROUP_KEY, PersistentDataType.INTEGER_ARRAY);
    if (modelPosition == null || modelPosition.length < 3) {
      return;
    }

    World world = event.getBlock().getLocation().getWorld();
    Block modelBlock = world.getBlockAt(modelPosition[0], modelPosition[1], modelPosition[2]);
    CustomBlockData modelData = new CustomBlockData(modelBlock, plugin);
    NamespacedKey key = NamespacedKey.fromString(Objects.requireNonNull(modelData.get(CustomModel.MODEL_PDC_KEY, PersistentDataType.STRING)));
    int entityId = Objects.requireNonNull(modelData.get(CustomModel.MODEL_PDC_EID_KEY, PersistentDataType.INTEGER));

    modelData.remove(CustomModel.MODEL_PDC_KEY);
    modelData.remove(CustomModel.MODEL_PDC_EID_KEY);

    CustomModel customModel = CustomModel.get(key);
    List<Packet<?>> destroyPackets = customModel.createDestroyPackets(entityId);

    for (Player player : modelBlock.getLocation().getNearbyPlayers(16 * 33)) {
      ServerPlayerConnection connection = ((CraftPlayer) player).getHandle().connection;
      destroyPackets.forEach(connection::send);
    }

    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
      for (BlockVector offset : customModel.hitboxes()) {
        Location hitboxLocation = modelBlock.getLocation().add(offset);
        CustomBlockData hitboxData = new CustomBlockData(hitboxLocation.getBlock(), plugin);
        hitboxData.remove(CustomModel.MODEL_PDC_GROUP_KEY);
        hitboxLocation.getWorld().setBlockData(hitboxLocation, Bukkit.createBlockData(Material.AIR));
      }
    });

    event.setCancelled(true);
  }
}
