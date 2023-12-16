package su.deltanw.core.impl.entity.model;

import com.google.gson.*;
import net.kyori.adventure.text.Component;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import su.deltanw.core.api.entity.model.ModelEngine;
import su.deltanw.core.api.entity.model.ModelLoader;

import java.io.Reader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ModelEngineImpl implements ModelEngine<ItemStack> {

  private static final Gson GSON =
      new GsonBuilder()
          .setPrettyPrinting()
          .disableHtmlEscaping()
          .create();

  private final Map<String, Map<String, ItemStack>> blockMappings = new HashMap<>();
  private final Map<String, Vector> offsetMappings = new HashMap<>();
  private final Map<String, Vector> diffMappings = new HashMap<>();
  private final ModelLoader modelLoader;
  private Path modelPath;

  public ModelEngineImpl(ModelLoader modelLoader) {
    this.modelLoader = modelLoader;
  }

  public ModelEngineImpl(Function<ModelEngine<ItemStack>, ModelLoader> modelLoaderConstructor) {
    this.modelLoader = modelLoaderConstructor.apply(this);
  }

  public ModelEngineImpl() {
    this.modelLoader = new ModelLoaderImpl(this);
  }

  private static ItemStack generateBoneItem(int modelId) {
    var itemStack = new org.bukkit.inventory.ItemStack(Material.LEATHER_HORSE_ARMOR);
    ItemMeta meta = itemStack.getItemMeta();
    meta.displayName(Component.empty());
    meta.setUnbreakable(true);
    meta.addItemFlags(ItemFlag.values());
    meta.setCustomModelData(modelId);
    itemStack.setItemMeta(meta);
    return CraftItemStack.asNMSCopy(itemStack);
  }

  @Override
  public Optional<Vector> getPos(JsonElement pivot) {
    if (pivot == null) {
      return Optional.empty();
    } else {
      JsonArray array = pivot.getAsJsonArray();
      return Optional.of(new Vector(
          array.get(0).getAsDouble(),
          array.get(1).getAsDouble(),
          array.get(2).getAsDouble()
      ));
    }
  }

  @Override
  public void loadMappings(Reader mappingsData, Path modelPath) {
    JsonObject map = GSON.fromJson(mappingsData, JsonObject.class);
    this.modelPath = modelPath;
    blockMappings.clear();
    offsetMappings.clear();
    diffMappings.clear();
    modelLoader.clearCache();

    map.entrySet().forEach(entry -> {
      Map<String, ItemStack> keys = new HashMap<>();

      JsonObject object = entry.getValue().getAsJsonObject();
      object
          .get("id")
          .getAsJsonObject()
          .entrySet()
          .forEach(id -> keys.put(id.getKey(), generateBoneItem(id.getValue().getAsInt())));

      blockMappings.put(entry.getKey(), keys);
      offsetMappings.put(entry.getKey(), getPos(object.get("offset")).orElseGet(Vector::new));
      diffMappings.put(entry.getKey(), getPos(object.get("diff")).orElseGet(Vector::new));
    });
  }

  @Override
  public Map<String, ItemStack> getItems(String model, String name) {
    return blockMappings.get(model + "/" + name);
  }

  @Override
  public String getGeoPath(String id) {
    return modelPath + "/" + id + "/model.geo.json";
  }

  @Override
  public Map<String, Vector> getDiffMappings() {
    return diffMappings;
  }

  @Override
  public Map<String, Vector> getOffsetMappings() {
    return offsetMappings;
  }

  @Override
  public String getAnimationPath(String id) {
    return modelPath + "/" + id + "/model.animation.json";
  }

  @Override
  public ModelLoader getModelLoader() {
    return modelLoader;
  }
}
