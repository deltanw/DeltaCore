package su.deltanw.core;

import net.elytrium.commons.config.YamlConfig;

import java.util.List;

public class Settings extends YamlConfig {

  @Ignore
  public static final Settings INSTANCE = new Settings();

  public List<SerializedCustomBlock> CUSTOM_BLOCKS = List.of();

  public static class SerializedCustomBlock {

    public String CUSTOM_BLOCK_KEY;
    public String CLIENTSIDE_BLOCK;
    public String SERVERSIDE_BLOCK;
    public String BLOCK_ITEM;
  }

}
