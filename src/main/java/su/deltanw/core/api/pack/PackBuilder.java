package su.deltanw.core.api.pack;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Map;

public interface PackBuilder<T extends PackBuilder<T>> {

  boolean isDirty();

  T makeDirty();

  T withIcon(RenderedImage image) throws IOException;

  T withPackMeta(PackMeta packMeta);

  T addRawEntry(String path, byte[] data);

  T addImage(String path, RenderedImage image) throws IOException;

  T addText(String path, String text);

  T addFile(String path, byte[] data);

  Map<String, byte[]> getRawEntries();

  ResourcePack build() throws IOException;
}
