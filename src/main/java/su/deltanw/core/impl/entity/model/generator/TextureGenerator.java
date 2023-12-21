package su.deltanw.core.impl.entity.model.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class TextureGenerator {

  public static Map<String, TextureData> generate(JsonArray textures, Map<String, JsonObject> mcmetas, int width, int height) {
    Map<String, TextureData> textureMap = new LinkedHashMap<>();

    for (JsonElement texture : textures) {
      JsonObject textureJson = texture.getAsJsonObject();

      String id = textureJson.get("id").getAsString();
      String dataB64 = textureJson.get("source").getAsString();
      byte[] data = Base64.getDecoder().decode(dataB64.substring("data:image/png;base64,".length()));
      String name = textureJson.get("name").getAsString();

      JsonElement uuid = textureJson.get("uuid");
      JsonObject mcmeta = null;

      if (uuid != null && uuid.isJsonPrimitive()) {
        JsonPrimitive uuidPrimitive = uuid.getAsJsonPrimitive();
        if (uuidPrimitive.isString()) {
          String uuidString = uuidPrimitive.getAsString();
          mcmeta = mcmetas.get(uuidString);
        }
      }

      if (mcmeta == null) {
        JsonElement frameTime = textureJson.get("frame_time");
        JsonElement frameInterpolate = textureJson.get("frame_interpolate");

        boolean hasValues = false;
        JsonObject animation = new JsonObject();

        if (frameTime != null && frameTime.isJsonPrimitive()) {
          JsonPrimitive frameTimePrimitive = frameTime.getAsJsonPrimitive();
          if (frameTimePrimitive.isNumber()) {
            hasValues = true;
            animation.add("frametime", frameTimePrimitive);
          }
        }

        if (frameInterpolate != null && frameInterpolate.isJsonPrimitive()) {
          JsonPrimitive frameInterpolatePrimitive = frameInterpolate.getAsJsonPrimitive();
          if (frameInterpolatePrimitive.isBoolean()) {
            hasValues = true;
            animation.add("interpolate", frameInterpolatePrimitive);
          }
        }

        if (hasValues) {
          mcmeta = new JsonObject();
          mcmeta.add("animation", animation);
        }
      }

      textureMap.put(id, new TextureData(data, width, height, name, id, mcmeta));
    }

    return textureMap;
  }
}
