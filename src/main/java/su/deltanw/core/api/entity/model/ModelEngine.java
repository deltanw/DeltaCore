package su.deltanw.core.api.entity.model;

import com.google.gson.JsonElement;
import org.bukkit.util.Vector;

import java.io.Reader;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public interface ModelEngine<I> {

  Optional<Vector> getPos(JsonElement pivot);

  void loadMappings(Reader mappingsData, Path modelPath);

  Map<String, I> getItems(String model, String name);

  String getGeoPath(String id);

  Map<String, Vector> getDiffMappings();

  Map<String, Vector> getOffsetMappings();

  String getAnimationPath(String id);

  ModelLoader getModelLoader();
}
