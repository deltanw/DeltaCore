package su.deltanw.core.impl.entity.model.generator;

import com.google.gson.JsonObject;

public record TextureData(byte[] value, int width, int height, String name, String id, JsonObject mcmeta) {
}
