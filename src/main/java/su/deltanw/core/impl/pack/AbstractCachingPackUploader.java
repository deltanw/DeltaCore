package su.deltanw.core.impl.pack;

import su.deltanw.core.api.pack.CachingPackUploader;
import su.deltanw.core.api.pack.ResourcePack;
import su.deltanw.core.api.pack.UploadedResourcePack;

import java.io.IOException;

public abstract class AbstractCachingPackUploader implements CachingPackUploader {

  protected UploadedResourcePack cache = null;

  @Override
  public void cache(UploadedResourcePack pack) {
    cache = pack;
  }

  @Override
  public UploadedResourcePack getCache() {
    return cache;
  }

  public abstract UploadedResourcePack uploadPack(ResourcePack pack) throws IOException;

  @Override
  public UploadedResourcePack upload(ResourcePack pack) throws IOException {
    return cache = uploadPack(pack);
  }
}
