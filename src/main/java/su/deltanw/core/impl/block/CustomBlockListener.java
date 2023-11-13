package su.deltanw.core.impl.block;

import com.jeff_media.customblockdata.CustomBlockData;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import su.deltanw.core.Core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CustomBlockListener implements Listener {

  private final Map<Player, Pair<BlockPos, String>> pendingOperations = new HashMap<>();
  private final Core plugin;

  public CustomBlockListener(Core plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onCustomBlockPlaced(BlockPlaceEvent event) {
    Pair<BlockPos, String> data = pendingOperations.remove(event.getPlayer());
    if (data == null) {
      return;
    }

    Location location = event.getBlockPlaced().getLocation();
    if (location.getBlockX() != data.getFirst().getX()
        || location.getBlockY() != data.getFirst().getY()
        || location.getBlockZ() != data.getFirst().getZ()) {
      return;
    }

    String key = data.getSecond();
    event.getPlayer().swingHand(event.getHand());
    CustomBlockData blockData = new CustomBlockData(event.getBlockPlaced(), plugin);
    blockData.set(Objects.requireNonNull(NamespacedKey.fromString("deltanw:custom_block")), PersistentDataType.STRING, key);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onCustomBlockPlace(PlayerInteractEvent event) {
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
    String customBlockKey = tag.getString("delta__custom_block");
    if (customBlockKey.isBlank()) {
      return;
    }

    NamespacedKey key = NamespacedKey.fromString(customBlockKey);
    CustomBlock customBlock = CustomBlock.get(key);
    if (customBlock == null) {
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

    pendingOperations.put(event.getPlayer(), Pair.of(nmsPos.relative(nmsDirection), customBlockKey));
    nmsPlayer.gameMode.useItemOn(nmsPlayer, level, customBlock.serversideItem(),
        event.getHand() == EquipmentSlot.OFF_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND,
        new BlockHitResult(nmsVec, nmsDirection, nmsPos, false));
    pendingOperations.remove(event.getPlayer());

    event.setCancelled(true);
  }
}
