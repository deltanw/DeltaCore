package su.deltanw.core.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import su.deltanw.core.api.ComponentFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import su.deltanw.core.api.pack.PackBuilder;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

public class ComponentFactoryImpl implements ComponentFactory {

  private final Map<String, TextComponent> animatedCache = new HashMap<>();
  private final char[] pixels;
  private final char[] pixelsExt;
  private char animatedChar = 0x0D80;

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

  @Override
  public TextComponent buildAnimatedComponent(String name, PackBuilder<? extends PackBuilder<?>> packBuilder, List<BufferedImage> frames, double duration, double height, double ascent) throws IOException {
    TextComponent cache = animatedCache.get(name);
    if (cache != null) {
      return cache;
    }

    if (frames == null || frames.isEmpty()) {
      throw new IllegalArgumentException("no input frames");
    }

    float ratio = (float) frames.get(0).getWidth() / frames.get(0).getHeight();
    boolean isMultiPart = ratio > 1.0;
    if (isMultiPart) {
      int parts = (int) Math.ceil(ratio);
      TextComponent.Builder builder = Component.text();

      for (int i = 0; i < parts; i++) {
        List<BufferedImage> subFrames = new ArrayList<>(frames.size());
        for (BufferedImage frame : frames) {
          int offset = frame.getHeight() * i;
          int width = Math.min(frame.getWidth() - offset, frame.getHeight());
          subFrames.add(frame.getSubimage(offset, 0, width, frame.getHeight()));
        }

        builder.append(buildAnimatedComponent(name + i, packBuilder, subFrames, duration));
      }

      return builder.build();
    }

    int numFrames = frames.size();
    int framesPerLine = (int) Math.ceil(Math.sqrt(numFrames));
    int frameDim = Math.max(frames.get(0).getWidth(), frames.get(0).getHeight());
    int totalWidth = framesPerLine * frameDim + 2;
    int totalHeight = framesPerLine * frameDim + 2;
    BufferedImage mergedFrames = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = mergedFrames.createGraphics();

    for (int i = 0; i < frames.size(); i++) {
      BufferedImage frame = frames.get(i);
      graphics.drawImage(frame, (i % framesPerLine) * frameDim + 1, (i / framesPerLine) * frameDim + 1, null);
    }

    graphics.dispose();

    int offsetX = totalWidth - 1;
    int offsetY = totalHeight - 1;
    int seconds = (int) Math.floor(duration);
    int subseconds = ((int) Math.floor((duration - seconds) * 255.0));

    mergedFrames.setRGB(0, 0, 0x0195D54B);
    mergedFrames.setRGB(1, 0, (totalWidth - 2) << 16 | (totalHeight - 2) << 8 | 0x0100004B);
    mergedFrames.setRGB(2, 0, frameDim << 16 | numFrames << 8 | 0x0100004B);
    mergedFrames.setRGB(3, 0, seconds << 16 | subseconds << 8 | 0x0100004B);

    mergedFrames.setRGB(offsetX, 0, offsetX << 16 | 0x0100004B);
    mergedFrames.setRGB(0, offsetY, offsetY << 8 | 0x0100004B);
    mergedFrames.setRGB(offsetX, offsetY, offsetX << 16 | offsetY << 8 | 0x0100004B);

    String text = String.valueOf(animatedChar++);

    JsonArray chars = new JsonArray();
    chars.add(text);

    JsonObject provider = new JsonObject();
    provider.addProperty("type", "bitmap");
    provider.addProperty("file", "minecraft:animated/" + name + ".png");
    provider.addProperty("ascent", ascent);
    provider.addProperty("height", height);
    provider.add("chars", chars);

    JsonArray providers = new JsonArray();
    providers.add(provider);

    JsonObject font = new JsonObject();
    font.add("providers", providers);

    packBuilder.addImage("assets/minecraft/textures/animated/" + name + ".png", mergedFrames);
    packBuilder.mergeJson("assets/minecraft/font/default.json", font.toString());

    TextComponent component = Component.text(text)
            .append(Component.text("render")); // TODO: Generate other symbols instead of hard-coding them
    animatedCache.put(name, component);

    return component;
  }

  @Override
  public TextComponent buildAnimatedComponent(String name, PackBuilder<? extends PackBuilder<?>> packBuilder, ImageInputStream input, double fontHeight, double fontAscent) throws IOException {
    TextComponent cache = animatedCache.get(name);
    if (cache != null) {
      return cache;
    }

    List<BufferedImage> frames = new ArrayList<>();
    List<String> disposals = new ArrayList<>();

    ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
    reader.setInput(input);

    int width = -1;
    int height = -1;
    int totalDelay = 0;

    IIOMetadata metadata = reader.getStreamMetadata();
    if (metadata != null) {
      IIOMetadataNode globalRoot = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());

      NodeList globalScreenDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor");

      if (globalScreenDescriptor.getLength() > 0) {
        IIOMetadataNode screenDescriptor = (IIOMetadataNode) globalScreenDescriptor.item(0);

        if (screenDescriptor != null) {
          width = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
          height = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
        }
      }
    }

    BufferedImage master = null;
    Graphics2D masterGraphics = null;

    for (int frameIndex = 0;; frameIndex++) {
      BufferedImage image;
      try {
        image = reader.read(frameIndex);
      } catch (IndexOutOfBoundsException e) {
        break;
      }

      if (width == -1 || height == -1) {
        width = image.getWidth();
        height = image.getHeight();
      }

      IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0");
      IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
      int delay = Integer.parseInt(gce.getAttribute("delayTime"));
      String disposal = gce.getAttribute("disposalMethod");

      int x = 0;
      int y = 0;

      if (master == null) {
        master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        masterGraphics = master.createGraphics();
        masterGraphics.setBackground(new Color(0, 0, 0, 0));
      } else {
        NodeList children = root.getChildNodes();
        for (int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++) {
          Node nodeItem = children.item(nodeIndex);
          if (nodeItem.getNodeName().equals("ImageDescriptor")) {
            NamedNodeMap map = nodeItem.getAttributes();
            x = Integer.parseInt(map.getNamedItem("imageLeftPosition").getNodeValue());
            y = Integer.parseInt(map.getNamedItem("imageTopPosition").getNodeValue());
          }
        }
      }
      masterGraphics.drawImage(image, x, y, null);

      BufferedImage copy = new BufferedImage(master.getColorModel(), master.copyData(null), master.isAlphaPremultiplied(), null);
      frames.add(copy);

      totalDelay += delay * 10;
      disposals.add(disposal);

      if (disposal.equals("restoreToPrevious")) {
        BufferedImage from = null;
        for (int i = frameIndex - 1; i >= 0; i--) {
          if (!disposals.get(i).equals("restoreToPrevious") || frameIndex == 0) {
            from = frames.get(i);
            break;
          }
        }

        assert from != null;
        master = new BufferedImage(from.getColorModel(), from.copyData(null), from.isAlphaPremultiplied(), null);
        masterGraphics = master.createGraphics();
        masterGraphics.setBackground(new Color(0, 0, 0, 0));
      } else if (disposal.equals("restoreToBackgroundColor")) {
        masterGraphics.clearRect(x, y, image.getWidth(), image.getHeight());
      }
    }
    reader.dispose();

    return buildAnimatedComponent(name, packBuilder, frames, (double) totalDelay / 1000.0, fontHeight, fontAscent);
  }
}
