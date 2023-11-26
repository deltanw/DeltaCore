package su.deltanw.core.impl.block;

import com.jeff_media.customblockdata.CustomBlockData;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record CustomBlock(NamespacedKey key, BlockState serversideBlock, BlockState clientsideBlock, net.minecraft.world.item.ItemStack serversideItem, ItemStack item) {

  private static final Map<NamespacedKey, CustomBlock> CUSTOM_BLOCK_REGISTRY = new HashMap<>();

  private static final HolderLookup.Provider HOLDER_LOOKUP_PROVIDER = VanillaRegistries.createLookup();
  private static final HolderLookup<Block> BLOCK_HOLDER_LOOKUP = HOLDER_LOOKUP_PROVIDER.lookup(Registries.BLOCK).orElseThrow();
  private static final HolderLookup<Item> ITEM_HOLDER_LOOKUP = HOLDER_LOOKUP_PROVIDER.lookup(Registries.ITEM).orElseThrow();

  public static List<CustomBlock> getAll() {
    return CUSTOM_BLOCK_REGISTRY.values().stream().toList();
  }

  public static CustomBlock get(NamespacedKey key) {
    return CUSTOM_BLOCK_REGISTRY.get(key);
  }

  public static CustomBlock register(NamespacedKey key, String serverside, String clientside, ItemStack item) throws CommandSyntaxException {
    BlockState serversideBlockState = BlockStateParser.parseForBlock(BLOCK_HOLDER_LOOKUP, new StringReader(serverside), true).blockState();
    BlockState clientsideBlockState = BlockStateParser.parseForBlock(BLOCK_HOLDER_LOOKUP, new StringReader(clientside), true).blockState();
    net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
    CompoundTag tag = nmsItem.getOrCreateTag();
    tag.putString("delta__custom_block", key.toString());
    tag.putString("delta__custom_item", key.toString());
    ItemStack craftItemMirror = CraftItemStack.asCraftMirror(nmsItem);
    Block block = serversideBlockState.getBlock();
    var serverboundItem = new net.minecraft.world.item.ItemStack(block);
    serverboundItem.getOrCreateTag().putString("delta__custom_block", key.toString());
    serverboundItem.getOrCreateTag().putString("delta__custom_item", key.toString());
    CustomBlock customBlock = new CustomBlock(key, serversideBlockState, clientsideBlockState, serverboundItem, craftItemMirror);
    CUSTOM_BLOCK_REGISTRY.put(key, customBlock);
    return customBlock;
  }

  public static CustomBlock register(NamespacedKey key, String serverside, String clientside, String item) throws CommandSyntaxException {
    ItemParser.ItemResult itemResult = ItemParser.parseForItem(ITEM_HOLDER_LOOKUP, new StringReader(item));
    ItemInput itemInput = new ItemInput(itemResult.item(), itemResult.nbt());
    net.minecraft.world.item.ItemStack nmsItem = itemInput.createItemStack(1, false);
    return register(key, serverside, clientside, CraftItemStack.asCraftMirror(nmsItem));
  }

  public void place(Plugin plugin, Location location) {
    ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();
    level.setBlock(new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), serversideBlock, 11 /* FLAGS????? */);
    CustomBlockData blockData = new CustomBlockData(location.getBlock(), plugin);
    blockData.set(Objects.requireNonNull(NamespacedKey.fromString("deltanw:custom_block")), PersistentDataType.STRING, key.asString());
  }
}
