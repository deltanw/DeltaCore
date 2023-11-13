package su.deltanw.core.impl.injection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPromise;
import org.jetbrains.annotations.NotNull;
import su.deltanw.core.api.injection.ChannelInjector;

import java.util.List;

@ChannelHandler.Sharable
public class ChannelInjectionHandler extends ChannelInboundHandlerAdapter {

  private final List<ChannelInjector> injectors;

  public ChannelInjectionHandler(List<ChannelInjector> injectors) {
    this.injectors = injectors;
  }

  @Override
  public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
    Channel channel = (Channel) msg;

    channel.pipeline().addLast(new ChannelInitializer<>() {

      @Override
      protected void initChannel(@NotNull Channel ch) {
        channel.pipeline().addLast(new ChannelDuplexHandler() {

          @Override
          public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
            ctx.pipeline().remove(this);
            inject(ctx.channel());

            super.channelRead(ctx, msg);
          }

          @Override
          public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            ctx.pipeline().remove(this);
            inject(ctx.channel());

            super.write(ctx, msg, promise);
          }
        });
      }
    });

    super.channelRead(ctx, msg);
  }

  private void inject(Channel channel) {
    injectors.forEach(injector -> injector.inject(channel));
  }
}