package su.deltanw.core.impl.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record CustomItem(NamespacedKey key, ItemStack item) {

  private static final Map<NamespacedKey, CustomItem> CUSTOM_ITEM_REGISTRY = new HashMap<>();

  private static final HolderLookup.Provider HOLDER_LOOKUP_PROVIDER = VanillaRegistries.createLookup();
  private static final HolderLookup<Item> ITEM_HOLDER_LOOKUP = HOLDER_LOOKUP_PROVIDER.lookup(Registries.ITEM).orElseThrow();

  public static List<CustomItem> getAll() {
    return CUSTOM_ITEM_REGISTRY.values().stream().toList();
  }

  public static CustomItem get(NamespacedKey key) {
    return CUSTOM_ITEM_REGISTRY.get(key);
  }

  public static CustomItem register(NamespacedKey key, ItemStack item) {
    net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
    CompoundTag tag = nmsItem.getOrCreateTag();
    tag.putString("delta__custom_item", key.toString());
    ItemStack craftItemMirror = CraftItemStack.asCraftMirror(nmsItem);
    CustomItem customItem = new CustomItem(key, craftItemMirror);
    CUSTOM_ITEM_REGISTRY.put(key, customItem);
    return customItem;
  }

  public static CustomItem register(NamespacedKey key, String item) throws CommandSyntaxException {
    ItemParser.ItemResult itemResult = ItemParser.parseForItem(ITEM_HOLDER_LOOKUP, new StringReader(item));
    ItemInput itemInput = new ItemInput(itemResult.item(), itemResult.nbt());
    net.minecraft.world.item.ItemStack nmsItem = itemInput.createItemStack(1, false);
    return register(key, CraftItemStack.asCraftMirror(nmsItem));
  }
}
