package su.deltanw.core.api.pack;

import java.util.Collection;

public interface ObservablePackBuilder<T extends ObservablePackBuilder<?>> extends PackBuilder<T> {

  void addObserver(PackObserver observer);

  void removeObserver(PackObserver observer);

  Collection<PackObserver> getObservers();

  void notifyObservers(ResourcePack pack);
}
