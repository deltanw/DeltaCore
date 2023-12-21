package su.deltanw.core.impl.entity.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.util.Vector;
import su.deltanw.core.api.entity.model.ModelEngine;
import su.deltanw.core.api.entity.model.ModelLoader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultModelLoader implements ModelLoader {

  private static final Gson GSON =
      new GsonBuilder()
          .setPrettyPrinting()
          .disableHtmlEscaping()
          .create();

  private final Map<String, JsonObject> loadedAnimations = new HashMap<>();
  private final Map<String, JsonObject> loadedModels = new HashMap<>();
  private final Map<String, Map<String, Map<Short, Vector>>> interpolationTranslateCache = new HashMap<>();
  private final Map<String, Map<String, Map<Short, Vector>>> interpolationRotateCache = new HashMap<>();
  private final ModelEngine<?> modelEngine;

  public DefaultModelLoader(ModelEngine<?> modelEngine) {
    this.modelEngine = modelEngine;
  }

  @Override
  public void clearCache() {
    interpolationTranslateCache.clear();
    interpolationRotateCache.clear();
    loadedAnimations.clear();
    loadedModels.clear();
  }

  @Override
  public JsonObject loadAnimations(String toLoad) {
    if (loadedAnimations.containsKey(toLoad)) {
      return loadedAnimations.get(toLoad);
    }

    JsonObject loadedAnimations;

    try {
      loadedAnimations = GSON.fromJson(
          new InputStreamReader(new FileInputStream(modelEngine.getAnimationPath(toLoad))),
          JsonObject.class
      );
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      loadedAnimations = null;
    }

    this.loadedAnimations.put(toLoad, loadedAnimations);
    return loadedAnimations;
  }

  @Override
  public JsonObject loadModel(String id) {
    if (loadedModels.containsKey(id)) {
      return loadedModels.get(id);
    }

    JsonObject loadedModel;
    try {
      loadedModel = GSON.fromJson(
          new InputStreamReader(new FileInputStream(modelEngine.getGeoPath(id))),
          JsonObject.class
      );
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      loadedModel = null;
    }

    loadedModels.put(id, loadedModel);
    return loadedModel;
  }

  @Override
  public void addTranslationCache(String key, String model, Map<Short, Vector> val) {
    interpolationTranslateCache.computeIfAbsent(model, k -> new HashMap<>()).put(key, val);
  }

  @Override
  public void addRotationCache(String key, String model, Map<Short, Vector> val) {
    interpolationRotateCache.computeIfAbsent(model, k -> new HashMap<>()).put(key, val);
  }

  @Override
  public Map<Short, Vector> getCacheRotation(String key, String model) {
    Map<String, Map<Short, Vector>> m = interpolationRotateCache.get(model);
    if (m == null) {
      return null;
    }
    return m.get(key);
  }

  @Override
  public Map<Short, Vector> getCacheTranslation(String key, String model) {
    Map<String, Map<Short, Vector>> m = interpolationTranslateCache.get(model);
    if (m == null) {
      return null;
    }
    return m.get(key);
  }

  @Override
  public Map<String, JsonObject> parseAnimations(String data) {
    Map<String, JsonObject> res = new LinkedHashMap<>();

    JsonObject animations = GSON.fromJson(new StringReader(data), JsonObject.class);
    for (Map.Entry<String, JsonElement> animation : animations.get("animations").getAsJsonObject().entrySet()) {
      res.put(animation.getKey(), animation.getValue().getAsJsonObject());
    }

    return res;
  }

  public ModelEngine<?> getEngine() {
    return modelEngine;
  }
}
