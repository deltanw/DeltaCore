package su.deltanw.core.impl.entity.model.parser;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

public record TextureState(double r, double g, double b, String name) {

  public static TextureState NORMAL = new TextureState(1.0, 1.0, 1.0, "normal");
  public static TextureState HIT = new TextureState(2.0, 0.7, 0.7, "hit");

  public BufferedImage multiplyColor(BufferedImage image) {
    ColorModel cm = image.getColorModel();
    boolean isAlphaPremul = cm.isAlphaPremultiplied();
    WritableRaster raster = image.copyData(null);
    BufferedImage newImage = new BufferedImage(cm, raster, isAlphaPremul, null);

    for (int i = 0; i < newImage.getWidth(); i++) {
      for (int j = 0; j < newImage.getHeight(); j++) {
        int rgb = newImage.getRGB(i, j);
        int a = (rgb >>> 24) & 0xFF;
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        newImage.setRGB(i, j, (a << 24) |
            (Math.min((int) (r * this.r), 255) << 16) |
            (Math.min((int) (g * this.g), 255) << 8) |
            Math.min((int) (b * this.b), 255));
      }
    }

    return newImage;
  }
}
