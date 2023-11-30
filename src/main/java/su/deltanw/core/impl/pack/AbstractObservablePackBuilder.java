package su.deltanw.core.impl.pack;

import su.deltanw.core.api.pack.ObservablePackBuilder;
import su.deltanw.core.api.pack.PackObserver;
import su.deltanw.core.api.pack.ResourcePack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractObservablePackBuilder<T extends AbstractObservablePackBuilder<T>> extends AbstractPackBuilder<T> implements ObservablePackBuilder<T> {

  private final List<PackObserver> observers;

  public AbstractObservablePackBuilder(List<PackObserver> observers) {
    this.observers = observers;
  }

  public AbstractObservablePackBuilder() {
    this(new ArrayList<>());
  }

  @Override
  public void addObserver(PackObserver observer) {
    observers.add(observer);
  }

  @Override
  public void removeObserver(PackObserver observer) {
    observers.remove(observer);
  }

  @Override
  public Collection<PackObserver> getObservers() {
    return observers;
  }

  @Override
  public void notifyObservers(ResourcePack pack) {
    observers.forEach(observer -> observer.updatePack(pack));
  }

  @Override
  public ResourcePack build() throws IOException {
    boolean shouldNotify = isDirty();
    ResourcePack pack = super.build();
    if (shouldNotify) {
      notifyObservers(pack);
    }
    return pack;
  }
}
