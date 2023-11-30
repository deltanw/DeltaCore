package su.deltanw.core.config;

import net.elytrium.commons.config.YamlConfig;
import su.deltanw.core.api.model.DisplayMode;

import java.util.List;

public class ModelsConfig extends YamlConfig {

  @Ignore
  public static final ModelsConfig INSTANCE = new ModelsConfig();

  public List<SerializedCustomModel> CUSTOM_MODELS = List.of();

  public static class SerializedCustomModel {

    public String MODEL_KEY;
    public String MODEL_ITEM;
    public DisplayMode DISPLAY_MODE;
    @Create
    public SCALE SCALE;

    public static class SCALE {

      public double X;
      public double Y;
      public double Z;
    }

    @Create
    public TRANSLATION TRANSLATION;

    public static class TRANSLATION {

      public double X;
      public double Y;
      public double Z;
    }

    @Create
    public ROTATION ROTATION;

    public static class ROTATION {

      public double X;
      public double Y;
    }
    public List<HitboxVector> HITBOXES;

    public static class HitboxVector {

      public int X;
      public int Y;
      public int Z;
    }
  }
}
