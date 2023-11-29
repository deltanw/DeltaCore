package su.deltanw.core.impl.pack;

import su.deltanw.core.api.pack.PackBuilder;
import su.deltanw.core.api.pack.ResourcePack;

import java.io.IOException;

public abstract class AbstractPackBuilder<T extends AbstractPackBuilder<T>> implements PackBuilder<T> {

  protected ResourcePack cache = null;

  @Override
  public boolean isDirty() {
    return cache == null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T makeDirty() {
    cache = null;
    return (T) this;
  }

  protected abstract ResourcePack buildPack() throws IOException;

  @Override
  public ResourcePack build() throws IOException {
    if (cache == null) {
      cache = buildPack();
    }

    return cache;
  }
}
