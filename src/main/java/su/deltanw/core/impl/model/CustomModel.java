package su.deltanw.core.impl.model;

import com.google.common.collect.Streams;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;
import org.joml.Vector2f;
import org.joml.Vector3f;
import su.deltanw.core.api.model.DisplayMode;

import java.util.*;

public record CustomModel(NamespacedKey key, DisplayMode displayMode, Vector3f scale, Vector3f translation, Vector2f rotation, List<BlockVector> hitboxes, ItemStack item) {

  private static final Map<NamespacedKey, CustomModel> MODEL_REGISTRY = new HashMap<>();

  private static final HolderLookup.Provider HOLDER_LOOKUP_PROVIDER = VanillaRegistries.createLookup();
  private static final HolderLookup<Item> ITEM_HOLDER_LOOKUP = HOLDER_LOOKUP_PROVIDER.lookup(Registries.ITEM).orElseThrow();

  public static final net.minecraft.world.item.ItemStack HITBOX_BLOCK_ITEM;

  public static NamespacedKey MODEL_PDC_KEY = Objects.requireNonNull(NamespacedKey.fromString("deltanw:custom_model"));
  public static NamespacedKey MODEL_PDC_EID_KEY = Objects.requireNonNull(NamespacedKey.fromString("deltanw:custom_model_eid"));
  public static NamespacedKey MODEL_PDC_GROUP_KEY = Objects.requireNonNull(NamespacedKey.fromString("deltanw:custom_model_group"));

  static {
    try {
      ItemParser.ItemResult itemResult = ItemParser.parseForItem(ITEM_HOLDER_LOOKUP, new StringReader("minecraft:barrier"));
      ItemInput itemInput = new ItemInput(itemResult.item(), itemResult.nbt());
      HITBOX_BLOCK_ITEM = itemInput.createItemStack(1, false);
    } catch (CommandSyntaxException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static List<CustomModel> getAll() {
    return MODEL_REGISTRY.values().stream().toList();
  }

  public static CustomModel get(NamespacedKey key) {
    return MODEL_REGISTRY.get(key);
  }

  public static CustomModel register(NamespacedKey key, DisplayMode displayMode, Vector3f scale, Vector3f translation, Vector2f rotation, List<BlockVector> hitboxes, ItemStack item) {
    net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
    CompoundTag tag = nmsItem.getOrCreateTag();
    tag.putString("delta__custom_model", key.toString());
    tag.putString("delta__custom_item", key.toString());
    ItemStack craftItemMirror = CraftItemStack.asCraftMirror(nmsItem);
    CustomModel model = new CustomModel(key, displayMode, scale, translation, rotation, hitboxes, craftItemMirror);
    MODEL_REGISTRY.put(key, model);
    return model;
  }

  public static CustomModel register(NamespacedKey key, DisplayMode displayMode, Vector3f scale, Vector3f translation, Vector2f rotation, List<BlockVector> hitboxes, String item) throws CommandSyntaxException {
    ItemParser.ItemResult itemResult = ItemParser.parseForItem(ITEM_HOLDER_LOOKUP, new StringReader(item));
    ItemInput itemInput = new ItemInput(itemResult.item(), itemResult.nbt());
    net.minecraft.world.item.ItemStack nmsItem = itemInput.createItemStack(1, false);
    return register(key, displayMode, scale, translation, rotation, hitboxes, CraftItemStack.asCraftMirror(nmsItem));
  }

  public List<Packet<?>> createSpawnPackets(int entityId, BlockVector position, Vector3f translation, Vector2f rotation, Vector3f scale) {
    return List.of(
        new ClientboundAddEntityPacket(entityId, UUID.randomUUID(),
            position.getX() + translation.x + translation().x,
            position.getY() + translation.y + translation().y,
            position.getZ() + translation.z + translation().z,
            rotation().y + rotation.y, rotation().x + rotation.x,
            EntityType.ITEM_DISPLAY, 0, new Vec3(0, 0, 0),
            rotation().x + rotation.x),
        new ClientboundSetEntityDataPacket(entityId,
            List.of(
                new SynchedEntityData.DataValue<>(11, EntityDataSerializers.VECTOR3, new Vector3f(scale()).mul(scale)), // 11 - Scale
                new SynchedEntityData.DataValue<>(22, EntityDataSerializers.ITEM_STACK, ((CraftItemStack) item()).handle), // 22 - Displayed item
                new SynchedEntityData.DataValue<>(23, EntityDataSerializers.BYTE, (byte) displayMode().ordinal()) // 23 - Display type
            )
        )
    );
  }

  public List<Packet<?>> createSpawnPackets(int entityId, BlockVector position) {
    return createSpawnPackets(entityId, position, new Vector3f(0, 0, 0), new Vector2f(0, 0), new Vector3f(1, 1, 1));
  }

  public List<Packet<?>> createDestroyPackets(int entityId) {
    return List.of(new ClientboundRemoveEntitiesPacket(entityId));
  }
}
