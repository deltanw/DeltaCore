package su.deltanw.core.impl.entity.model.parser;

import com.google.gson.JsonObject;

import java.util.Map;

public record ModelFile(Map<String, JsonObject> bones, Map<String, byte[]> textures, String id,
                        TextureState state, int textureWidth, int textureHeight) {
  
}
