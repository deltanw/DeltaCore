package su.deltanw.core.config;

import net.elytrium.commons.config.YamlConfig;

import java.util.List;

public class ItemsConfig extends YamlConfig {

  @Ignore
  public static final ItemsConfig INSTANCE = new ItemsConfig();

  public List<SerializedCustomItem> CUSTOM_ITEMS = List.of();

  public static class SerializedCustomItem {

    public String CUSTOM_ITEM_KEY;
    public String SERVERSIDE_ITEM;
  }
}
