package su.deltanw.core.api.block;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public interface CustomBlocks {

  ItemStack transformBlockItem(NamespacedKey key, ItemStack itemStack);

  ItemStack createCustomBlockItem(NamespacedKey key, int amount);

  default ItemStack createCustomBlockItem(NamespacedKey key) {
    return createCustomBlockItem(key, 1);
  }

  void placeCustomBlock(NamespacedKey key, Location location);
}
