package su.deltanw.core.config;

import net.elytrium.commons.config.YamlConfig;

import java.util.Map;

public class EmojisConfig extends YamlConfig {

    @Ignore
    public static final EmojisConfig INSTANCE = new EmojisConfig();

    @Create
    public ANIMATED ANIMATED;

    public static class ANIMATED {

        public Map<String, AnimatedEmojiProperties> PROPERTIES = Map.of();
    }

    public static class AnimatedEmojiProperties {

        public double HEIGHT = 10.0;
        public double ASCENT = 8.0;
        public double DURATION = -1.0;
    }
}
