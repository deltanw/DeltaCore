package com.jnngl.ucore.impl;

import com.jnngl.ucore.api.ComponentFactory;
import java.awt.image.BufferedImage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

public class ComponentFactoryImpl implements ComponentFactory {

  private final char[] pixels;
  private final char[] pixelsExt;

  public ComponentFactoryImpl(char[] pixels, char[] pixelsExt) {
    this.pixels = pixels;
    this.pixelsExt = pixelsExt;
  }

  @Override
  public TextComponent buildComponent(BufferedImage image) {
    if (image == null) {
      throw new IllegalArgumentException("image is null");
    }

    if (image.getHeight() > 10 && image.getHeight() < 16) {
      throw new IllegalArgumentException("image height should be <= 10");
    }

    if (image.getHeight() > 18) {
      throw new IllegalArgumentException("image height should be <= 18");
    }

    TextComponent.Builder component = Component.text();

    StringBuilder builder = new StringBuilder();
    TextColor currentColor = null;

    char[] palette = image.getHeight() > 10 ? pixelsExt : pixels;

    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        int rgb = image.getRGB(x, image.getHeight() - 1 - y);
        int alpha = (rgb >>> 24) & 0xFF;
        if (alpha < 100) {
          if (y == image.getHeight() - 1) {
            builder.append(palette[palette.length - 1]);
          }

          continue;
        }

        TextColor nextColor = TextColor.color(rgb);
        if (currentColor != null && !nextColor.equals(currentColor)) {
          component.append(
              Component
                  .text(builder.toString())
                  .color(currentColor)
          );

          builder.setLength(0);
        }

        int index = y + palette.length - 1 - image.getHeight();
        builder.append(palette[index]);
        currentColor = nextColor;
      }
    }

    if (builder.length() > 0 && currentColor != null) {
      component.append(
          Component
              .text(builder.toString())
              .color(currentColor)
      );
    }

    return component.build();
  }
}
