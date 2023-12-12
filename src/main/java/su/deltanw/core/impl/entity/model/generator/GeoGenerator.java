package su.deltanw.core.impl.entity.model.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoGenerator {

  public static JsonArray applyInflate(JsonArray from, double inflate) {
    JsonArray inflated = new JsonArray();
    for (int i = 0; i < from.size(); i++) {
      double val = from.get(i).getAsDouble() + inflate;
      inflated.add(val);
    }
    return inflated;
  }

  public static List<JsonObject> parseRecursive(JsonObject obj, Map<String, JsonObject> cubeMap, String parent) {
    List<JsonObject> output = new ArrayList<>();
    float scale = 0.25F;
    String name = obj.get("name").getAsString();
    JsonArray origin = obj.get("origin").getAsJsonArray();
    JsonArray pivot = new JsonArray();
    pivot.add(-origin.get(0).getAsDouble() * scale);
    pivot.add(origin.get(1).getAsDouble() * scale);
    pivot.add(origin.get(2).getAsDouble() * scale);

    JsonArray cubes = new JsonArray();

    JsonArray rotation;
    if (!obj.has("rotation")) {
      rotation = new JsonArray();
      rotation.add(0);
      rotation.add(0);
      rotation.add(0);
    } else {
      rotation = obj.get("rotation").getAsJsonArray();
      JsonArray newRotation = new JsonArray();
      newRotation.add(-rotation.get(0).getAsDouble());
      newRotation.add(-rotation.get(1).getAsDouble());
      newRotation.add(rotation.get(2).getAsDouble());
      rotation = newRotation;
    }

    for (JsonElement child : obj.get("children").getAsJsonArray()) {
      if (child.isJsonObject()) {
        output.addAll(parseRecursive(child.getAsJsonObject(), cubeMap, name));
      } else {
        JsonObject cube = cubeMap.get(child.toString());
        cubes.add(cube);
      }
    }

    JsonObject element = new JsonObject();
    element.addProperty("name", name);
    element.add("pivot", pivot);
    element.add("rotation", rotation);
    element.add("cubes", cubes);

    if (parent != null) {
      element.addProperty("parent", parent);
    }

    output.add(element);
    return output;
  }

  public static JsonArray generate(JsonArray elements, JsonArray outliner, Map<String, TextureData> textures) {
    Map<String, JsonObject> blocks = new HashMap<>();

    for (JsonElement element : elements) {
      JsonObject elementJson = element.getAsJsonObject();
      float scale = 0.25F;
      double inflate = 0;
      if (elementJson.has("inflate")) {
        inflate = elementJson.get("inflate").getAsDouble() * scale;
      }

      JsonArray origin = elementJson.get("origin").getAsJsonArray();

      JsonArray newOrigin = new JsonArray();
      newOrigin.add(-Math.round(origin.get(0).getAsDouble() * 10000 * scale) / 10000.0);
      newOrigin.add(Math.round(origin.get(1).getAsDouble() * 10000 * scale) / 10000.0);
      newOrigin.add(Math.round(origin.get(2).getAsDouble() * 10000 * scale) / 10000.0);
      origin = newOrigin;

      JsonArray from = applyInflate(elementJson.get("from").getAsJsonArray(), -inflate);
      JsonArray to = applyInflate(elementJson.get("to").getAsJsonArray(), inflate);

      JsonArray newTo = new JsonArray();
      newTo.add(Math.round(to.get(0).getAsDouble() * 10000 * scale) / 10000.0);
      newTo.add(Math.round(to.get(1).getAsDouble() * 10000 * scale) / 10000.0);
      newTo.add(Math.round(to.get(2).getAsDouble() * 10000 * scale) / 10000.0);
      to = newTo;

      JsonArray newFrom = new JsonArray();
      newFrom.add(Math.round(from.get(0).getAsDouble() * 10000 * scale) / 10000.0);
      newFrom.add(Math.round(from.get(1).getAsDouble() * 10000 * scale) / 10000.0);
      newFrom.add(Math.round(from.get(2).getAsDouble() * 10000 * scale) / 10000.0);
      from = newFrom;

      JsonArray size = buildSize(from, to);

      newFrom = new JsonArray();
      newFrom.add(-(from.get(0).getAsDouble() + size.get(0).getAsDouble()));
      newFrom.add(from.get(1).getAsDouble());
      newFrom.add(from.get(2).getAsDouble());
      from = newFrom;

      JsonArray rotation = new JsonArray();
      if (!elementJson.has("rotation")) {
        rotation.add(0);
        rotation.add(0);
        rotation.add(0);
      } else {
        JsonArray rot = elementJson.get("rotation").getAsJsonArray();
        rotation.add(-Math.round(rot.get(0).getAsDouble() * 100) / 100.0);
        rotation.add(-Math.round(rot.get(1).getAsDouble() * 100) / 100.0);
        rotation.add(Math.round(rot.get(2).getAsDouble() * 100) / 100.0);
      }

      JsonObject faces = parseFaces(elementJson.get("faces").getAsJsonObject(), textures);
      JsonObject object = new JsonObject();
      object.add("origin", from);
      object.add("size", size);
      object.add("pivot", origin);
      object.add("rotation", rotation);
      object.add("uv", faces);

      blocks.put(elementJson.get("uuid").toString(), object);
    }

    List<JsonObject> bonesList = new ArrayList<>();
    for (JsonElement outline : outliner) {
      JsonObject outlineJson = outline.getAsJsonObject();
      bonesList.addAll(parseRecursive(outlineJson, blocks, null));
    }

    JsonArray bones = new JsonArray();
    for (JsonElement bone : bonesList) {
      bones.add(bone);
    }

    return bones;
  }

  private static JsonObject parseFaces(JsonObject obj, Map<String, TextureData> textures) {
    JsonObject faces = new JsonObject();

    for (var entry : obj.asMap().entrySet()) {
      boolean invert = entry.getKey().equals("up") || entry.getKey().equals("down");
      String face = entry.getKey();
      JsonElement uv = entry.getValue().getAsJsonObject().get("uv");
      JsonArray shape = uv.getAsJsonArray();

      JsonArray size = new JsonArray();
      size.add(shape.get(2).getAsDouble() - shape.get(0).getAsDouble());
      size.add(shape.get(3).getAsDouble() - shape.get(1).getAsDouble());

      JsonArray from = new JsonArray();
      from.add(shape.get(0).getAsDouble());
      from.add(shape.get(1).getAsDouble());

      if (invert) {
        JsonArray newFrom = new JsonArray();
        newFrom.add(from.get(0).getAsDouble() + size.get(0).getAsDouble());
        newFrom.add(from.get(1).getAsDouble() + size.get(1).getAsDouble());
        from = newFrom;

        JsonArray newSize = new JsonArray();
        newSize.add(-size.get(0).getAsDouble());
        newSize.add(-size.get(1).getAsDouble());
        size = newSize;
      }

      String texture = "#0";
      if (textures.size() > 0) {
        texture = textures.values().toArray(new TextureData[0])[0].id();
      }

      JsonElement n = entry.getValue().getAsJsonObject().get("texture");
      if (n != null && !n.isJsonNull()) {
        int ni = n.getAsInt();
        if (textures.values().size() < ni) {
          texture = "#" + ni;
        } else {
          texture = textures.values().toArray(new TextureData[0])[ni].id();
        }
      }

      JsonObject faceParsed = new JsonObject();
      faceParsed.add("uv", from);
      faceParsed.add("uv_size", size);
      faceParsed.addProperty("texture", texture);

      faces.add(face, faceParsed);
    }

    return faces;
  }

  private static JsonArray buildSize(JsonArray from, JsonArray to) {
    JsonArray size = new JsonArray();

    for (int i = 0; i < from.size(); i++) {
      double fromd = from.get(i).getAsDouble();
      double tod = to.get(i).getAsDouble();
      size.add(Math.round((tod - fromd) * 100000) / 100000.0);
    }

    return size;
  }
}
