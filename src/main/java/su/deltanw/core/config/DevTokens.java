package su.deltanw.core.config;

import net.elytrium.commons.config.YamlConfig;

import java.util.List;
import java.util.Map;

public class DevTokens extends YamlConfig {

  @Ignore
  public static DevTokens INSTANCE = new DevTokens();

  public Map<String, List<String>> FILE_TOKENS = Map.of();
}
