package su.deltanw.core.api.pack;

import java.io.IOException;

public interface PackUploader {

  UploadedResourcePack upload(ResourcePack pack) throws IOException;
}
