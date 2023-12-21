package su.deltanw.core.impl.entity.model.generator;

import com.google.gson.JsonObject;

import java.util.Map;

public record BBEntityModel(JsonObject geo, JsonObject animations,
                            Map<String, TextureData> textures, String id,
                            AdditionalStates additionalStates) {
}
