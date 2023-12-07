package su.deltanw.core.devapi;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import su.deltanw.core.config.DevTokens;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class NettyRequestHandler extends SimpleChannelInboundHandler<HttpObject> {

  private static final HttpDataFactory HTTP_DATA_FACTORY = new DefaultHttpDataFactory(true);

  private final NettyHttpServer httpServer;
  private HttpPostRequestDecoder httpDecoder;
  private Path targetPath;
  private Path root;

  public NettyRequestHandler(NettyHttpServer httpServer) {
    this.httpServer = httpServer;
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
    if (msg instanceof io.netty.handler.codec.http.HttpRequest request) {
      String uri = request.uri();
      String[] params = uri.split("/");
      if (params.length < 2) {
        ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
        ctx.close();
        return;
      }

      if (!params[1].equals("upload")) {
        return;
      }

      String path = String.join("/", Arrays.copyOfRange(params, 2, params.length));
      if (path.contains("tokens.yml")) {
        ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
        ctx.close();
        return;
      }

      List<String> values = request.headers().getAll("Auth");
      List<String> patterns = null;
      for (String value : values) {
        patterns = DevTokens.INSTANCE.FILE_TOKENS.get(value);
        if (patterns != null) {
          break;
        }
      }

      if (patterns == null) {
        ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED));
        ctx.close();
        return;
      }

      boolean allowed = false;
      for (String pattern : patterns) {
        if (path.matches(pattern)) {
          allowed = true;
          break;
        }
      }

      if (!allowed) {
        ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
        ctx.close();
        return;
      }

      root = httpServer.getCore().getDataFolder().getParentFile().toPath();
      targetPath = root.resolve(String.join("/", Arrays.copyOfRange(params, 2, params.length)));

      if (!targetPath.toAbsolutePath().startsWith(root.toAbsolutePath())) {
        ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
        ctx.close();
        return;
      }

      if (request.method() == HttpMethod.POST) {
        httpDecoder = new HttpPostRequestDecoder(HTTP_DATA_FACTORY, request);
        httpDecoder.setDiscardThreshold(0);
      } else {
        ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED));
        ctx.close();
        return;
      }
    }

    if (msg instanceof HttpContent chunk) {
      if (httpDecoder != null) {
        httpDecoder.offer(chunk);

        decodeChunk(ctx);

        if (chunk instanceof LastHttpContent) {
          targetPath = null;
          root = null;
          httpDecoder.destroy();
          httpDecoder = null;
        }
      }
    }
  }

  private void decodeChunk(ChannelHandlerContext ctx) throws IOException {
    while (httpDecoder.hasNext()) {
      InterfaceHttpData data = httpDecoder.next();
      if (data != null) {
        switch (data.getHttpDataType()) {
          case Attribute:
            break;

          case FileUpload:
            boolean exists = Files.exists(targetPath);
            if (exists && Files.isDirectory(targetPath)) {
              ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
              ctx.close();
              break;
            }
            if (!exists) {
              Files.createDirectories(targetPath.getParent());
              Files.createFile(targetPath);
            }
            FileUpload fileUpload = (FileUpload) data;
            try (FileInputStream fileInputStream = new FileInputStream(fileUpload.getFile());
                 FileChannel outputChannel = FileChannel.open(targetPath, StandardOpenOption.WRITE)) {
              FileChannel inputChannel = fileInputStream.getChannel();
              outputChannel.truncate(inputChannel.size());
              outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
              ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
              ctx.close();

              String relative = root.relativize(targetPath).toString();
              Bukkit.getScheduler().scheduleSyncDelayedTask(httpServer.getCore(), () -> {
                Component diffComponent =
                    (exists ? Component.text("[M] ", NamedTextColor.BLUE) :
                        Component.text("[+] ", NamedTextColor.GREEN))
                            .append(Component.text(relative));
                Bukkit.broadcast(
                    Component.text("DeltaNetwork")
                        .append(Component.text(" | ", NamedTextColor.GRAY))
                        .append(diffComponent)
                );

                String relativeLower = relative.toLowerCase(Locale.ROOT);
                if (relativeLower.matches(".*deltacore/.*pack/.*")) {
                  try {
                    httpServer.getCore().loadPack();
                    httpServer.getCore().getDefaultPackBuilder().build();
                    Bukkit.broadcast(Component.text("Ресурс пак успешно обновлен.", NamedTextColor.GRAY));
                  } catch (IOException e) {
                    e.printStackTrace();
                    Bukkit.broadcast(Component.text("Невозможно загрузить ресурс пак:", NamedTextColor.RED));
                    StringWriter writer = new StringWriter();
                    e.printStackTrace(new PrintWriter(writer));
                    for (String line : writer.toString().split("\n")) {
                      Bukkit.broadcast(Component.text(line, NamedTextColor.RED));
                    }
                  }
                } else if (relativeLower.matches(".*deltacore/.*models\\.yml")) {
                  httpServer.getCore().loadCustomModels();
                  Bukkit.broadcast(Component.text("Модели успешно загружены.", NamedTextColor.GRAY));
                  Bukkit.broadcast(httpServer.getCore().getErrorComponent().append(
                      Component.text(" Для применения некоторых изменений необходимо перезайти и/или перезагрузить сервер.", NamedTextColor.RED)));
                } else if (relativeLower.matches(".*deltacore/.*blocks\\.yml")) {
                  httpServer.getCore().loadCustomBlocks();
                  Bukkit.broadcast(Component.text("Блоки успешно загружены.", NamedTextColor.GRAY));
                  Bukkit.broadcast(httpServer.getCore().getErrorComponent().append(
                      Component.text(" Для применения некоторых изменений необходимо перезайти и/или перезагрузить сервер.", NamedTextColor.RED)));
                } else if (relativeLower.matches(".*deltacore/.*items\\.yml")) {
                  httpServer.getCore().loadCustomItems();
                  Bukkit.broadcast(Component.text("Предметы успешно загружены.", NamedTextColor.GRAY));
                  Bukkit.broadcast(httpServer.getCore().getErrorComponent().append(
                      Component.text(" Для применения некоторых изменений необходимо перезайти и/или перезагрузить сервер.", NamedTextColor.RED)));
                } else {
                  Bukkit.broadcast(httpServer.getCore().getErrorComponent().append(
                      Component.text(" Для применения изменений необходимо перезагрузить сервер.", NamedTextColor.RED)));
                }
              });
            }
            break;
        }
      }
    }
  }
}
