package su.deltanw.core.impl.pack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.codehaus.plexus.util.FileUtils;
import su.deltanw.core.api.pack.PackMeta;
import su.deltanw.core.api.pack.ResourcePack;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PackBuilderImpl extends AbstractObservablePackBuilder<PackBuilderImpl> {

  private static final Gson GSON = new Gson();

  private final Map<String, byte[]> rawEntries = new HashMap<>();

  @Override
  public PackBuilderImpl addRawEntry(String path, byte[] data) {
    rawEntries.put(path, data);
    makeDirty();
    return this;
  }

  @Override
  public PackBuilderImpl withIcon(RenderedImage image) throws IOException {
    addImage("pack.png", image);
    return this;
  }

  @Override
  public PackBuilderImpl withPackMeta(PackMeta packMeta) {
    JsonObject json = new JsonObject();
    json.add("pack", GSON.toJsonTree(packMeta));
    addText("pack.mcmeta", GSON.toJson(json));
    return this;
  }

  @Override
  public PackBuilderImpl addFile(String path, byte[] data) {
    addRawEntry(path, data);
    return this;
  }

  @Override
  public byte[] getFile(String path) {
    return rawEntries.get(path);
  }

  @Override
  public PackBuilderImpl addImage(String path, RenderedImage image) throws IOException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      ImageIO.write(image, "PNG", outputStream);
      addFile(path, outputStream.toByteArray());
      return this;
    }
  }

  @Override
  public PackBuilderImpl addText(String path, String text) {
    addFile(path, text.getBytes(StandardCharsets.UTF_8));
    return this;
  }

  private void mergeJson(JsonObject destination, JsonObject source) {
    for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
      String key = entry.getKey();
      JsonElement value = entry.getValue();
      if (destination.has(key)) {
        JsonElement currentValue = destination.get(key);
        if (value.isJsonArray() && currentValue.isJsonArray()) {
          JsonArray currentArray = currentValue.getAsJsonArray();
          JsonArray arrayToMerge = value.getAsJsonArray();
          currentArray.addAll(arrayToMerge);
        } else if (value.isJsonObject() && currentValue.isJsonObject()) {
          mergeJson(currentValue.getAsJsonObject(), value.getAsJsonObject());
        } else {
          destination.add(key, value);
        }
      } else {
        destination.add(key, value);
      }
    }
  }

  @Override
  public PackBuilderImpl mergeJson(String path, String data) {
    byte[] current = getFile(path);
    if (current == null) {
      return addText(path, data);
    }

    JsonObject currentJson = GSON.fromJson(new String(current, StandardCharsets.UTF_8), JsonObject.class);
    JsonObject toMerge = GSON.fromJson(data, JsonObject.class);

    mergeJson(currentJson, toMerge);

    addText(path, GSON.toJson(currentJson));
    return this;
  }

  @Override
  public Map<String, byte[]> getRawEntries() {
    return rawEntries;
  }

  @Override
  protected ResourcePack buildPack() throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ZipOutputStream zos = new ZipOutputStream(baos)) {
      Set<String> seenDirectories = new HashSet<>();
      rawEntries.entrySet().stream()
          .sorted(Map.Entry.comparingByKey())
          .forEach(entry -> {
            try {
              String path = entry.getKey();
              String dirname = FileUtils.dirname(path);
              if (seenDirectories.add(dirname)) {
                zos.putNextEntry(new ZipEntry(dirname.endsWith("/") ? dirname : dirname + "/"));
                zos.closeEntry();
              }
              ZipEntry zipEntry = new ZipEntry(path);
              zipEntry.setLastModifiedTime(FileTime.fromMillis(0));
              zos.putNextEntry(zipEntry);
              zos.write(entry.getValue());
              zos.closeEntry();
            } catch (IOException e) {
              throw new RuntimeException("Couldn't add zip entry.", e);
            }
          });

      zos.finish();
      return new ResourcePack(baos.toByteArray());
    }
  }
}
