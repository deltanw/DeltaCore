package su.deltanw.core.config;

import net.elytrium.commons.config.YamlConfig;

public class DevConfig extends YamlConfig {

  @Ignore
  public static DevConfig INSTANCE = new DevConfig();

  public boolean ENABLE_DEV_SERVER = false;
  public int DEV_SERVER_PORT = 8003;
}
