package su.deltanw.core.api;

import java.awt.image.BufferedImage;
import net.kyori.adventure.text.TextComponent;

public interface ComponentFactory {

  TextComponent buildComponent(BufferedImage image);
}
