package su.deltanw.core.devapi;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class NettyChannelInitializer extends ChannelInitializer<SocketChannel> {

  private final NettyHttpServer httpServer;

  public NettyChannelInitializer(NettyHttpServer httpServer) {
    this.httpServer = httpServer;
  }

  @Override
  protected void initChannel(SocketChannel channel) {
    ChannelPipeline pipeline = channel.pipeline();
    pipeline.addLast(new HttpRequestDecoder());
    pipeline.addLast(new HttpResponseEncoder());
    pipeline.addLast(httpServer.getRequestHandler());
  }
}