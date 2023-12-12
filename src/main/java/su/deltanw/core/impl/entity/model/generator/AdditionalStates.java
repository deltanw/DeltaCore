package su.deltanw.core.impl.entity.model.generator;

import com.google.gson.JsonObject;
import su.deltanw.core.impl.entity.model.parser.TextureState;

import java.util.HashMap;
import java.util.Map;

public class AdditionalStates {

  public record StateDescription(Map<String, String> boneTextureMappings, TextureState state) {

  }

  private final Map<String, StateDescription> states = new HashMap<>();

  public AdditionalStates(JsonObject obj, Map<String, TextureData> data) {
    Map<String, String> nameMapping = new HashMap<>();

    for (var entry : data.entrySet()) {
      nameMapping.put(entry.getValue().name(), entry.getValue().id());
    }

    obj.asMap().forEach((k, v) -> {
      Map<String, String> mappings = new HashMap<>();
      v.getAsJsonObject().asMap().forEach((x, y) -> mappings.put(x, nameMapping.get(y.getAsString())));
      states.put(k, new StateDescription(mappings, new TextureState(1, 1, 1, k)));
    });
  }

  private AdditionalStates() {
  }

  public static AdditionalStates empty() {
    return new AdditionalStates();
  }


  public Map<String, StateDescription> states() {
    return states;
  }

  @Override
  public String toString() {
    return "AdditionalStates{" +
        "states=" + states +
        '}';
  }
}
