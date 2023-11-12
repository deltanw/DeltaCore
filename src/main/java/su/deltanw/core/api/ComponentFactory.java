package su.deltanw.ucore.api;

import java.awt.image.BufferedImage;
import net.kyori.adventure.text.TextComponent;

public interface ComponentFactory {

  TextComponent buildComponent(BufferedImage image);
}
