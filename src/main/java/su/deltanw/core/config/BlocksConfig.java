package su.deltanw.core.config;

import net.elytrium.commons.config.YamlConfig;

import java.util.List;

public class BlocksConfig extends YamlConfig {

  @Ignore
  public static final BlocksConfig INSTANCE = new BlocksConfig();

  public List<SerializedCustomBlock> CUSTOM_BLOCKS = List.of();

  public static class SerializedCustomBlock {

    public String CUSTOM_BLOCK_KEY;
    public String CLIENTSIDE_BLOCK;
    public String SERVERSIDE_BLOCK;
    public String BLOCK_ITEM;
  }

}
