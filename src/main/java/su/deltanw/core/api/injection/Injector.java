package su.deltanw.core.api.injection;

import io.netty.channel.ChannelFuture;

import java.util.List;

public interface Injector {

  void addInjector(ChannelInjector injector);

  void removeInjector(ChannelInjector injector);

  List<ChannelInjector> getInjectors();

  List<? extends ChannelFuture> getOpenChannels();
}
