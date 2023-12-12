package su.deltanw.core.impl.entity.model.generator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import su.deltanw.core.impl.entity.model.parser.ModelEngineFiles;
import su.deltanw.core.impl.entity.model.parser.ModelParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelGenerator {

  private static final Gson GSON = new Gson();

  public static BBEntityModel generate(String name, JsonObject data, JsonObject additionalStates) {
    JsonObject animations = AnimationGenerator.generate(data.get("animations").getAsJsonArray());

    int width = 16;
    int height = 16;

    if (data.has("resolution")) {
      JsonObject resolution = data.get("resolution").getAsJsonObject();
      width = resolution.get("width").getAsInt();
      height = resolution.get("height").getAsInt();
    }

    Map<String, JsonObject> mcmetas = new HashMap<>();
    if (data.has("mcmetas")) {
      for (var mcmeta : data.get("mcmetas").getAsJsonObject().asMap().entrySet()) {
        mcmetas.put(mcmeta.getKey(), mcmeta.getValue().getAsJsonObject());
      }
    }

    Map<String, TextureData> textures = TextureGenerator.generate(data.get("textures").getAsJsonArray(), mcmetas, width, height);
    JsonArray bones = GeoGenerator.generate(data.get("elements").getAsJsonArray(), data.get("outliner").getAsJsonArray(), textures);

    JsonObject description = new JsonObject();
    description.addProperty("identifier", "geometry.unknown");
    description.addProperty("texture_width", width);
    description.addProperty("texture_height", height);

    JsonObject geometryData = new JsonObject();
    geometryData.add("description", description);
    geometryData.add("bones", bones);

    JsonArray geometryArray = new JsonArray();
    geometryArray.add(geometryData);

    JsonObject geometry = new JsonObject();
    geometry.addProperty("format_version", "1.12.0");
    geometry.add("minecraft:geometry", geometryArray);

    JsonObject animation = new JsonObject();
    animation.addProperty("format_version", "1.8.0");
    animation.add("animations", animations);

    if (additionalStates != null) {
      return new BBEntityModel(geometry, animation, textures, name, new AdditionalStates(additionalStates, textures));
    } else {
      return new BBEntityModel(geometry, animation, textures, name, AdditionalStates.empty());
    }
  }

  public static BBEntityModel generate(String name, String data, String additionalStates) {
    JsonObject dataJson = GSON.fromJson(data, JsonObject.class);
    JsonObject additionalStatesJson = GSON.fromJson(additionalStates, JsonObject.class);
    return generate(name, dataJson, additionalStatesJson);
  }
}
