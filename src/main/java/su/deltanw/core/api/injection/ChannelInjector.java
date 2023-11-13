package su.deltanw.core.api.injection;

import io.netty.channel.Channel;

public interface ChannelInjector {

  void inject(Channel channel);
}