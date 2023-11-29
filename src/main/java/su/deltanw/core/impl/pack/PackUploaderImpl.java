package su.deltanw.core.impl.pack;

import com.google.common.hash.Hashing;
import su.deltanw.core.api.pack.ResourcePack;
import su.deltanw.core.api.pack.UploadedResourcePack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class PackUploaderImpl extends AbstractCachingPackUploader {

  private final Path distPath;

  public PackUploaderImpl(Path distPath) {
    this.distPath = distPath;
  }

  @Override
  public UploadedResourcePack uploadPack(ResourcePack pack) throws IOException {
    @SuppressWarnings("deprecation")
    String hash = Hashing.sha1().hashBytes(pack.data()).toString();
    Files.write(distPath.resolve("pack.zip"), pack.data());
    Files.writeString(distPath.resolve("pack.zip.sha1"), hash, StandardCharsets.UTF_8);
    return new UploadedResourcePack("https://api.deltanw.su/v1/pack/" + hash, hash);
  }
}
