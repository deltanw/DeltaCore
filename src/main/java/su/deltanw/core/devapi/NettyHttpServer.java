package su.deltanw.core.devapi;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import su.deltanw.core.Core;

public class NettyHttpServer {

  private final boolean epoll = Epoll.isAvailable();
  private final EventLoopGroup group = epoll ? new EpollEventLoopGroup() : new NioEventLoopGroup();
  private final EventLoopGroup childGroup = epoll ? new EpollEventLoopGroup() : new NioEventLoopGroup();
  private final Class<? extends ServerChannel> socketChannel = epoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
  private final NettyChannelInitializer channelInitializer = new NettyChannelInitializer(this);
  private final Core core;
  private final int port;
  private ChannelFuture channelFuture;

  public NettyHttpServer(Core core, int port) {
    this.core = core;
    this.port = port;
  }

  public void start() {
    this.channelFuture = new ServerBootstrap()
        .group(group, childGroup)
        .channel(socketChannel)
        .childHandler(channelInitializer)
        .bind(port);
  }

  public void close() {
    channelFuture.channel().close();
    childGroup.shutdownGracefully();
    group.shutdownGracefully();
  }

  public NettyRequestHandler getRequestHandler() {
    return new NettyRequestHandler(this);
  }

  public Core getCore() {
    return core;
  }
}