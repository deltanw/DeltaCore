package su.deltanw.core.config;

import java.util.List;
import net.elytrium.commons.config.YamlConfig;

public class BiomesConfig extends YamlConfig {

  @Ignore
  public static final BiomesConfig INSTANCE = new BiomesConfig();

  public List<SerializedCustomBiome> CUSTOM_BIOMES = List.of();

  public static class SerializedCustomBiome {

    public String BIOME_KEY;

    public boolean PRECIPITATION;
    public double TEMPERATURE;
    public double DOWNFALL;

    @Create
    public Effects EFFECTS;

    public static class Effects {

      public int FOG_COLOR;
      public int WATER_COLOR;
      public int WATER_FOG_COLOR;
      public int SKY_COLOR;
      public int FOLIAGE_COLOR;
      public int GRASS_COLOR;
      public String GRASS_COLOR_MODIFIER;

      @Create
      public Ambient AMBIENT;

      public static class Ambient {

        public String SOUND;

        @Create
        public Particle PARTICLE;

        public static class Particle {

          public String PARTICLE;
          public double PROBABILITY;
        }

        @Create
        public Mood MOOD;

        public static class Mood {

          public String SOUND;
          public int TICK_DELAY;
          public int BLOCK_SEARCH_EXTENT;
          public double SOUND_POSITION_OFFSET;
        }

        @Create
        public Additions ADDITIONS;

        public static class Additions {

          public String SOUND;
          public double TICK_CHANCE;
        }

        @Create
        public Music MUSIC;

        public static class Music {

          public String SOUND;
          public int MIN_DELAY;
          public int MAX_DELAY;
          public boolean REPLACE_CURRENT_MUSIC;
        }
      }
    }

    // TODO: mob spawning / biome generation
  }
}
