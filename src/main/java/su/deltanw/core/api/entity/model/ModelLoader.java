package su.deltanw.core.api.entity.model;

import com.google.gson.JsonObject;
import org.bukkit.util.Vector;

import java.util.Map;

public interface ModelLoader {

  void clearCache();

  JsonObject loadAnimations(String toLoad);

  JsonObject loadModel(String id);

  void addTranslationCache(String key, String model, Map<Short, Vector> val);

  void addRotationCache(String key, String model, Map<Short, Vector> val);

  Map<Short, Vector> getCacheRotation(String key, String model);

  Map<Short, Vector> getCacheTranslation(String key, String model);

  Map<String, JsonObject> parseAnimations(String animation);
}
