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
    public EFFECTS EFFECTS;

    public static class EFFECTS {

      public int FOG_COLOR;
      public int WATER_COLOR;
      public int WATER_FOG_COLOR;
      public int SKY_COLOR;
      public int FOLIAGE_COLOR;
      public int GRASS_COLOR;
      public String GRASS_COLOR_MODIFIER;

      @Create
      public AMBIENT AMBIENT;

      public static class AMBIENT {

        public String SOUND;

        @Create
        public PARTICLE PARTICLE;

        public static class PARTICLE {

          public String PARTICLE;
          public double PROBABILITY;
        }

        @Create
        public MOOD MOOD;

        public static class MOOD {

          public String SOUND;
          public int TICK_DELAY;
          public int BLOCK_SEARCH_EXTENT;
          public double SOUND_POSITION_OFFSET;
        }

        @Create
        public ADDITIONS ADDITIONS;

        public static class ADDITIONS {

          public String SOUND;
          public double TICK_CHANCE;
        }

        @Create
        public MUSIC MUSIC;

        public static class MUSIC {

          public String SOUND;
          public int MIN_DELAY;
          public int MAX_DELAY;
          public boolean REPLACE_CURRENT_MUSIC;
        }
      }
    }

    @Create
    public MOB_SPAWN MOB_SPAWN;

    public static class MOB_SPAWN {

      public double CREATURE_GENERATION_PROBABILITY;

      public List<SPAWNERS> SPAWNERS;

      public static class SPAWNERS {

        public String ENTITY_TYPE;
        public String MOB_CATEGORY;
        public int WEIGHT;
        public int MIN_COUNT;
        public int MAX_COUNT;
      }

      public List<SPAWN_COST> SPAWN_COST;

      public static class SPAWN_COST {

        public String ENTITY_TYPE;
        public double ENERGY_BUDGET;
        public double CHARGE;
      }
    }

    // TODO: biome generation
  }
}
