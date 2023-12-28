package su.deltanw.core.api;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import net.kyori.adventure.text.TextComponent;
import su.deltanw.core.api.pack.PackBuilder;

import javax.imageio.stream.ImageInputStream;

public interface ComponentFactory {

  TextComponent buildComponent(BufferedImage image);

  TextComponent buildAnimatedComponent(String name, PackBuilder<? extends PackBuilder<?>> packBuilder, List<BufferedImage> frames, double duration, double height, double ascent) throws IOException;

  default TextComponent buildAnimatedComponent(String name, PackBuilder<? extends PackBuilder<?>> packBuilder, List<BufferedImage> frames, double duration) throws IOException {
    return buildAnimatedComponent(name, packBuilder, frames, duration, 10, 8);
  }

  TextComponent buildAnimatedComponent(String name, PackBuilder<? extends PackBuilder<?>> packBuilder, ImageInputStream input, double duration, double height, double ascent) throws IOException;

  default TextComponent buildAnimatedComponent(String name, PackBuilder<? extends PackBuilder<?>> packBuilder, ImageInputStream input, double height, double ascent) throws IOException {
    return buildAnimatedComponent(name, packBuilder, input, -1.0, height, ascent);
  }

  default TextComponent buildAnimatedComponent(String name, PackBuilder<? extends PackBuilder<?>> packBuilder, ImageInputStream input, double duration) throws IOException {
    return buildAnimatedComponent(name, packBuilder, input, duration, 10, 8);
  }

  default TextComponent buildAnimatedComponent(String name, PackBuilder<? extends PackBuilder<?>> packBuilder, ImageInputStream input) throws IOException {
    return buildAnimatedComponent(name, packBuilder, input, -1.0, 10, 8);
  }

  void reset();
}
