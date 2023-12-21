package su.deltanw.core.impl.entity.model.parser;

import com.google.gson.JsonObject;

import java.util.List;

public record ModelEngineFiles(JsonObject mappings, JsonObject binding, List<ModelFile> models) {
  
}
