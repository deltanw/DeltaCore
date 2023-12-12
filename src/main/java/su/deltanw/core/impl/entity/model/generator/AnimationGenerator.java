package su.deltanw.core.impl.entity.model.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AnimationGenerator {

  private static double parseDoubleElement(JsonElement element) {
    if (element == null) {
      return 0.0;
    }

    if (!element.isJsonPrimitive()) {
      return 0.0;
    }

    JsonPrimitive primitive = element.getAsJsonPrimitive();
    if (primitive.isString()) {
      return Double.parseDouble(primitive.getAsString());
    } else if (primitive.isNumber()) {
      return primitive.getAsDouble();
    } else {
      return 0.0;
    }
  }

  private static JsonArray convertDatapoints(JsonObject obj, double scale) {
    JsonArray array = new JsonArray();

    if (obj.has("effect")) {
      return null;
    }

    array.add(parseDoubleElement(obj.get("x")) * scale);
    array.add(parseDoubleElement(obj.get("y")) * scale);
    array.add(parseDoubleElement(obj.get("z")) * scale);

    return array;
  }

  public static JsonObject generate(JsonArray animationRaw) {
    JsonObject animations = new JsonObject();
    if (animationRaw == null) {
      return animations;
    }

    for (int i = 0; i < animationRaw.size(); i++) {
      JsonObject animation = animationRaw.get(i).getAsJsonObject();

      String name = animation.get("name").getAsString();
      double length = animation.get("length").getAsDouble();

      JsonObject bones = new JsonObject();

      JsonObject animators = animation.get("animators").getAsJsonObject();
      if (animators == null) {
        continue;
      }

      Collection<JsonElement> animatorValues = animators.asMap().values();
      for (JsonElement animatorRaw : animatorValues) {
        JsonObject animator = animatorRaw.getAsJsonObject();

        String boneName = animator.get("name").getAsString();

        List<Map.Entry<Double, JsonObject>> rotation = new ArrayList<>();
        List<Map.Entry<Double, JsonObject>> position = new ArrayList<>();

        JsonArray keyframes = animator.get("keyframes").getAsJsonArray();
        for (int k = 0; k < keyframes.size(); k++) {
          JsonObject keyframe = keyframes.get(k).getAsJsonObject();
          String channel = keyframe.get("channel").getAsString();
          double time = keyframe.get("time").getAsDouble();
          JsonObject dataPointsRaw = keyframe.get("data_points").getAsJsonArray().get(0).getAsJsonObject();
          JsonArray dataPoints = channel.equals("position") ?
              convertDatapoints(dataPointsRaw, 0.25) : convertDatapoints(dataPointsRaw, 1);
          if (dataPoints == null) {
            continue;
          }

          String interpolation = keyframe.get("interpolation").getAsString();

          JsonObject object = new JsonObject();
          object.add("post", dataPoints);
          object.addProperty("lerp_mode", interpolation);

          if (channel.equals("rotation")) {
            rotation.add(Map.entry(time, object));
          } else if (channel.equals("position")) {
            position.add(Map.entry(time, object));
          }
        }

        rotation.sort(Map.Entry.comparingByKey());
        position.sort(Map.Entry.comparingByKey());

        JsonObject rotationJson = new JsonObject();
        JsonObject positionJson = new JsonObject();

        for (var entry : rotation) {
          rotationJson.add(entry.getKey().toString(), entry.getValue());
        }

        for (var entry : position) {
          positionJson.add(entry.getKey().toString(), entry.getValue());
        }

        JsonObject object = new JsonObject();
        object.add("rotation", rotationJson);
        object.add("position", positionJson);

        bones.add(boneName, object);
      }

      JsonObject object = new JsonObject();
      object.addProperty("loop", animation.get("loop").getAsString().equals("loop"));
      object.addProperty("animation_length", length);
      object.add("bones", bones);

      animations.add(name, object);
    }

    return animations;
  }
}
