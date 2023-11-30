package su.deltanw.core.api.pack;

public interface CachingPackUploader extends PackUploader {

  void cache(UploadedResourcePack pack);

  UploadedResourcePack getCache();
}
