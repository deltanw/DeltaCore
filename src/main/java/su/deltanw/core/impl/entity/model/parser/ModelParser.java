package su.deltanw.core.impl.entity.model.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.util.Vector;
import su.deltanw.core.impl.entity.model.generator.BBEntityModel;
import su.deltanw.core.impl.entity.model.generator.TextureData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;

public class ModelParser {

  public record Cube(Vector origin, Vector size, Vector pivot, Vector rotation, Map<TextureFace, UV> uv) {
  }

  public record Bone(String name, List<Cube> cubes, Vector pivot) {
  }

  public record ItemId(String name, String bone, Vector offset, Vector diff, int id) {
  }

  public record MappingEntry(Map<String, Integer> map, Vector offset, Vector diff) {

    public JsonObject asJson() {
      JsonObject object = new JsonObject();
      object.add("id", entrySetToJson(map));
      object.add("offset", pointAsJson(offset));
      object.add("diff", pointAsJson(diff));
      return object;
    }
  }

  public record UV(double x1, double y1, double x2, double y2, String texture) {

    public JsonObject asJson() {
      JsonArray array = new JsonArray();
      array.add(x1);
      array.add(y1);
      array.add(x2);
      array.add(y2);

      JsonObject object = new JsonObject();
      object.add("uv", array);
      object.addProperty("texture", texture);
      return object;
    }
  }

  public record RotationInfo(double angle, String axis, Vector origin) {

    public JsonObject asJson() {
      JsonObject object = new JsonObject();
      object.addProperty("angle", angle);
      object.addProperty("axis", axis);
      object.add("origin", pointAsJson(origin));
      return object;
    }
  }

  public record Element(Vector from, Vector to, Map<TextureFace, UV> faces, RotationInfo rotation) {

    public JsonObject asJson() {
      JsonObject object = new JsonObject();
      object.add("from", pointAsJson(from));
      object.add("to", pointAsJson(to));
      object.add("faces", facesAsJson(faces));
      object.add("rotation", rotation.asJson());
      return object;
    }
  }

  private static JsonObject facesAsJson(Map<TextureFace, UV> faces) {
    JsonObject object = new JsonObject();
    for (TextureFace face : faces.keySet()) {
      object.add(face.name().toLowerCase(Locale.ROOT), faces.get(face).asJson());
    }

    return object;
  }

  private static JsonArray pointAsJson(Vector from) {
    JsonArray array = new JsonArray();
    array.add(Math.round(from.getX() * 10000) / 10000.0);
    array.add(Math.round(from.getY() * 10000) / 10000.0);
    array.add(Math.round(from.getZ() * 10000) / 10000.0);
    return array;
  }

  private static UV convertUV(UV uv, int width, int height, boolean inverse) {
    double sx = uv.x1 * (16.0 / width);
    double sy = uv.y1 * (16.0 / height);
    double ex = uv.x2 * (16.0 / width);
    double ey = uv.y2 * (16.0 / height);

    if (inverse) {
      return new UV(ex + sx, ey + sy, sx, sy, uv.texture);
    } else {
      return new UV(sx, sy, ex + sx, ey + sy, uv.texture);
    }
  }

  private static JsonObject entrySetToJson(Map<String, Integer> map) {
    JsonObject object = new JsonObject();
    map.forEach(object::addProperty);
    return object;
  }

  private static JsonArray elementsToJson(List<Element> elements) {
    JsonArray array = new JsonArray();
    elements.forEach(e -> array.add(e.asJson()));
    return array;
  }

  private static Map<TextureFace, UV> getUV(JsonObject uv) {
    Map<TextureFace, UV> map = new HashMap<>();

    for (TextureFace face : TextureFace.values()) {
      String faceName = face.name().toLowerCase(Locale.ROOT);

      JsonObject faceData = uv.get(faceName).getAsJsonObject();
      JsonArray faceUV = faceData.get("uv").getAsJsonArray();
      JsonArray faceSize = faceData.get("uv_size").getAsJsonArray();
      String texture = faceData.get("texture").getAsString();
      UV uvObj = new UV(faceUV.get(0).getAsDouble(), faceUV.get(1).getAsDouble(),
          faceSize.get(0).getAsDouble(), faceSize.get(1).getAsDouble(), texture);
      map.put(face, uvObj);
    }

    return map;
  }

  private static JsonObject createPredicate(int id, String name, String state, String bone) {
    JsonObject customModelData = new JsonObject();
    customModelData.addProperty("custom_model_data", id);

    JsonObject object = new JsonObject();
    object.add("predicate", customModelData);
    object.addProperty("model", "custom/entities/" + name + "/" + state + "/" + bone);

    return object;
  }

  private final Map<String, MappingEntry> mappings = new HashMap<>();
  private final List<JsonObject> predicates = new ArrayList<>();
  private final List<TextureState> textureStates;
  private int minIndex = 0;
  private int index = 0;

  public ModelParser(List<TextureState> textureStates) {
    this.textureStates = textureStates;
  }

  public ModelParser() {
    this(List.of(TextureState.HIT, TextureState.NORMAL));
  }

  public void setMinIndex(int minIndex) {
    this.minIndex = minIndex;
  }

  public int getMinIndex() {
    return minIndex;
  }

  public JsonObject display(Vector offset) {
    JsonArray translationHead = new JsonArray();
    translationHead.add(offset.getX() * -4.0);
    translationHead.add(offset.getY() * 4.0 - 6.5);
    translationHead.add(offset.getZ() * -4.0);
    
    JsonArray translationDisplay = new JsonArray();
    translationDisplay.add(offset.getX() * -4.0);
    translationDisplay.add(offset.getY() * 4.0);
    translationDisplay.add(offset.getZ() * -4.0);
    
    JsonArray translationArm = new JsonArray();
    translationArm.add(offset.getX() * -4.0 - 1);
    translationArm.add(offset.getZ() * 4.0 - 2);
    translationArm.add(offset.getY() * 4.0 + 10);
    
    JsonArray scaleHead = new JsonArray();
    scaleHead.add(-4);
    scaleHead.add(4);
    scaleHead.add(-4);
    
    JsonArray scaleArm = new JsonArray();
    scaleArm.add(4);
    scaleArm.add(4);
    scaleArm.add(4);
    
    JsonArray rotationArm = new JsonArray();
    rotationArm.add(90);
    rotationArm.add(180);
    rotationArm.add(0);
    
    JsonObject head = new JsonObject();
    head.add("translation", translationHead);
    head.add("scale", scaleHead);
    
    JsonObject arm = new JsonObject();
    arm.add("rotation", rotationArm);
    arm.add("translation", translationArm);
    arm.add("scale", scaleArm);
    
    JsonObject display = new JsonObject();
    display.add("translation", translationDisplay);
    display.add("scale", scaleHead);
    
    JsonObject object = new JsonObject();
    object.add("head", head);
    object.add("thirdperson_righthand", arm);
    object.add("thirdperson_lefthand", display);
    
    return object;
  }

  public Optional<Vector> getPos(JsonElement pivot) {
    if (pivot == null) {
      return Optional.empty();
    } else {
      JsonArray array = pivot.getAsJsonArray();
      return Optional.of(new Vector(
          array.get(0).getAsDouble(),
          array.get(1).getAsDouble(),
          array.get(2).getAsDouble()
      ));
    }
  }
  
  public ModelEngineFiles parse(Collection<BBEntityModel> data, BiConsumer<String, String> writer) throws Exception {
    List<ModelFile> models = new ArrayList<>();

    index = minIndex;
    predicates.clear();
    mappings.clear();

    for (BBEntityModel folder : data) {
      models.addAll(createFiles(folder, writer));
    }

    JsonObject textures = new JsonObject();
    textures.addProperty("layer0", "minecraft:item/leather_horse_armor");

    JsonObject leatherArmorFile = new JsonObject();
    leatherArmorFile.addProperty("parent", "item/generated");
    leatherArmorFile.add("textures", textures);
    leatherArmorFile.add("overrides", predicatesToJson());

    return new ModelEngineFiles(mappingsToJson(), leatherArmorFile, models);
  }

  private Map<String, JsonObject> createIndividualModels(List<Bone> bones, int textureWidth, int textureHeight, BBEntityModel model,
                                                         JsonObject textures, JsonArray textureSize, TextureState state) {
    Map<String, JsonObject> modelInfo = new HashMap<>();

    for (Bone bone : bones) {
      String boneName = bone.name;

      List<Element> elements = new ArrayList<>();

      double cubeMinX = 100000;
      double cubeMinY = 100000;
      double cubeMinZ = 100000;

      double cubeMaxX = -100000;
      double cubeMaxY = -100000;
      double cubeMaxZ = -100000;

      for (Cube cube : bone.cubes) {
        Vector cubeOrigin = cube.origin;
        Vector cubeSize = cube.size;

        cubeMinX = Math.min(cubeMinX, cubeOrigin.getX());
        cubeMinY = Math.min(cubeMinY, cubeOrigin.getY());
        cubeMinZ = Math.min(cubeMinZ, cubeOrigin.getZ());

        cubeMaxX = Math.max(cubeMaxX, cubeOrigin.getX() + cubeSize.getX());
        cubeMaxY = Math.max(cubeMaxY, cubeOrigin.getY() + cubeSize.getY());
        cubeMaxZ = Math.max(cubeMaxZ, cubeOrigin.getZ() + cubeSize.getZ());
      }

      Vector cubeMid = new Vector((cubeMaxX + cubeMinX) / 2 - 8, (cubeMaxY + cubeMinY) / 2 - 8, (cubeMaxZ + cubeMinZ) / 2 - 8);
      Vector trueMid = bone.pivot.clone().multiply(new Vector(-1, 1, 1)).subtract(new Vector(8, 8, 8));
      Vector midOffset = cubeMid.clone().subtract(trueMid);

      Vector cubeDiff = new Vector(trueMid.getX() - cubeMinX + 16, trueMid.getY() - cubeMinY + 16, trueMid.getZ() - cubeMinZ + 16);

      for (Cube cube : bone.cubes()) {
        Vector cubePivot = new Vector(-(cube.pivot().getX() + cubeMid.getX()), cube.pivot.getY() - cubeMid.getY(), cube.pivot.getZ() - cubeMid.getZ());
        Vector cubeSize = cube.size;
        Vector cubeOrigin = cube.origin;

        Vector cubeFrom = new Vector(cubeOrigin.getX() - cubeMid.getX(), cubeOrigin.getY() - cubeMid.getY(), cubeOrigin.getZ() - cubeMid.getZ());
        Vector cubeTo = new Vector(cubeFrom.getX() + cubeSize.getX(), cubeFrom.getY() + cubeSize.getY(), cubeFrom.getZ() + cubeSize.getZ());

        Vector cubeRotation = new Vector(-cube.rotation.getX(), -cube.rotation.getY(), -cube.rotation.getZ());

        Map<TextureFace, UV> uvs = new HashMap<>();
        for (TextureFace face : cube.uv.keySet()) {
          UV newUV = convertUV(cube.uv.get(face), textureWidth, textureHeight, face == TextureFace.UP || face == TextureFace.DOWN);
          uvs.put(face, newUV);
        }

        double rotationAmount = 0;
        String rotationAxis = "z";

        if (cubeRotation.getX() != 45
            && cubeRotation.getX() != -22.5
            && cubeRotation.getX() != 22.5
            && cubeRotation.getX() != -45
            && cubeRotation.getX() != 0) {
          throw new IllegalArgumentException("Invalid rotation (X)");
        }

        if (cubeRotation.getY() != 45
            && cubeRotation.getY() != -22.5
            && cubeRotation.getY() != 22.5
            && cubeRotation.getY() != -45
            && cubeRotation.getY() != 0) {
          throw new IllegalArgumentException("Invalid rotation (Y)");
        }

        if (cubeRotation.getZ() != 45
            && cubeRotation.getZ() != -22.5
            && cubeRotation.getZ() != 22.5
            && cubeRotation.getZ() != -45
            && cubeRotation.getZ() != 0) {
          throw new IllegalArgumentException("Invalid rotation (Z)");
        }

        if (cubeRotation.getX() != 0) {
          rotationAmount = cubeRotation.getX();
          rotationAxis = "x";
        }

        if (cubeRotation.getY() != 0) {
          if (rotationAmount != 0) {
            throw new IllegalArgumentException("Cannot rotate on multiple axis");
          }

          rotationAmount = cubeRotation.getY();
          rotationAxis = "y";
        }

        if (cubeRotation.getZ() != 0) {
          if (rotationAmount != 0) {
            throw new IllegalArgumentException("Cannot rotate on multiple axis");
          }

          rotationAmount = cubeRotation.getZ();
          rotationAxis = "z";
        }

        RotationInfo rotationInfo = new RotationInfo(rotationAmount, rotationAxis, cubePivot);
        Element newElement = new Element(cubeFrom, cubeTo, uvs, rotationInfo);
        elements.add(newElement);

        if (!mappings.containsKey(model.id() + "/" + boneName) ||
            mappings.get(model.id() + "/" + boneName).map.get(state.name()) == null) {
          ItemId item = new ItemId(
              model.id(), boneName,
              new Vector(cubeMinX + cubeDiff.getX() - 8, cubeMinY + cubeDiff.getY() - 8, cubeMinZ + cubeDiff.getZ() - 8),
              cubeDiff,
              ++index
          );

          mappings.computeIfAbsent(item.name + "/" + item.bone, k -> new MappingEntry(new HashMap<>(), item.offset, item.diff));
          mappings.get(item.name + "/" + item.bone).map.put(state.name(), item.id);
          predicates.add(createPredicate(item.id, model.id(), state.name(), item.bone));
        }

        JsonObject boneInfo = new JsonObject();
        boneInfo.add("textures", textures);
        boneInfo.add("elements", elementsToJson(elements));
        boneInfo.add("texture_size", textureSize);
        boneInfo.add("display", display(midOffset));
        modelInfo.put(boneName + ".json", boneInfo);
      }
    }

    return modelInfo;
  }

  private ModelFile generateModelFile(TextureState state, BBEntityModel model, List<Bone> bones,
                                      JsonArray textureSize, int textureWidth, int textureHeight) throws IOException {
    Map<String, byte[]> textures = new HashMap<>();

    JsonObject modelTextureJson = new JsonObject();
    for (Map.Entry<String, TextureData> t : model.textures().entrySet()) {
      modelTextureJson.addProperty(t.getKey(), "minecraft:custom/entities/" + model.id() + "/" + state.name() + "/" + t.getKey());

      byte[] textureByte = t.getValue().value();
      BufferedImage texture = ImageIO.read(new BufferedInputStream(new ByteArrayInputStream(textureByte)));
      BufferedImage stateTexture = state.multiplyColor(texture);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(stateTexture, "PNG", baos);
      textures.put(t.getKey(), baos.toByteArray());
    }

    Map<String, JsonObject> modelInfo = createIndividualModels(bones, textureWidth, textureHeight, model, modelTextureJson, textureSize, state);
    return new ModelFile(modelInfo, textures, model.id(), state, textureWidth, textureHeight);
  }

  private List<ModelFile> createFiles(BBEntityModel model, BiConsumer<String, String> writer) throws Exception {
    List<ModelFile> output = new ArrayList<>();

    int textureHeight = 16;
    int textureWidth = 16;

    JsonObject modelGeoFile = model.geo().get("minecraft:geometry").getAsJsonArray().get(0).getAsJsonObject();

    JsonArray bonesJson = modelGeoFile.get("bones").getAsJsonArray();
    JsonObject description = modelGeoFile.get("description").getAsJsonObject();

    if (description != null) {
      textureHeight = description.get("texture_height").getAsInt();
      textureWidth = description.get("texture_width").getAsInt();
    }

    JsonArray textureSize = new JsonArray();
    textureSize.add(textureWidth);
    textureSize.add(textureHeight);

    List<Bone> bones = new ArrayList<>();
    for (JsonElement bone : bonesJson) {
      JsonObject boneJson = bone.getAsJsonObject();
      if (boneJson.get("cubes") == null) {
        continue;
      }

      String name = boneJson.get("name").getAsString();
      Vector bonePivot = getPos(bone.getAsJsonObject().get("pivot")).orElseGet(Vector::new);

      List<Cube> cubes = new ArrayList<>();
      for (JsonElement cube : boneJson.get("cubes").getAsJsonArray()) {
        JsonObject cubeJson = cube.getAsJsonObject();
        Optional<Vector> origin = getPos(cubeJson.get("origin"));
        Optional<Vector> size = getPos(cubeJson.get("size"));
        Optional<Vector> pivot = getPos(cubeJson.get("pivot"));
        Optional<Vector> rotation = getPos(cubeJson.get("rotation"));

        Map<TextureFace, UV> uv = getUV(cubeJson.get("uv").getAsJsonObject());

        if (origin.isPresent() && size.isPresent()) {
          Vector actualOrigin = origin.get();
          actualOrigin.setX(-actualOrigin.getX() - size.get().getX());
          Cube cubeObj = new Cube(actualOrigin, size.get(), pivot.orElseGet(Vector::new), rotation.orElseGet(Vector::new), uv);
          cubes.add(cubeObj);
        }
      }

      if (cubes.size() > 0) {
        bones.add(new Bone(name, cubes, bonePivot));
      }
    }

    for (TextureState state : textureStates) {
      ModelFile modelFile = generateModelFile(state, model, bones, textureSize, textureWidth, textureHeight);
      output.add(modelFile);

      for (var substate : model.additionalStates().states().entrySet()) {
        for (var subBone : substate.getValue().boneTextureMappings().entrySet()) {
          JsonObject modelTextureJson = new JsonObject();

          for (String t : modelFile.textures().keySet()) {
            modelTextureJson.addProperty(t, "minecraft:custom/entities/" + model.id() + "/" + state.name() + "/" + subBone.getValue());
          }

          List<Bone> subBones = bones.stream()
              .filter(bone -> subBone.getKey().equals(bone.name()))
              .toList();

          var toWrite = createIndividualModels(subBones, textureWidth, textureHeight, model, modelTextureJson, textureSize, substate.getValue().state());

          for (var w : toWrite.entrySet()) {
            writer.accept(model.id() + "/" + substate.getKey() + "/" + w.getKey(), w.getValue().toString());
          }
        }
      }
    }

    return output;
  }

  private JsonObject mappingsToJson() {
    JsonObject object = new JsonObject();
    mappings.forEach((k, v) -> object.add(k, v.asJson()));
    return object;
  }

  private JsonArray predicatesToJson() {
    JsonArray array = new JsonArray();
    predicates.forEach(array::add);
    return array;
  }
}
