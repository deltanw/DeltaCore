package su.deltanw.core.hook.worldedit;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Bukkit;
import su.deltanw.core.Core;
import su.deltanw.core.impl.block.CustomBlock;

public class WorldEditHook {

  private static boolean ENABLED = false;

  public static boolean init(Core plugin) {
    ENABLED = Bukkit.getPluginManager().isPluginEnabled("WorldEdit")
        || Bukkit.getPluginManager().isPluginEnabled("WorldEdit");
    if (ENABLED) {
      Bukkit.getPluginManager().registerEvents(new WorldEditListener(), plugin);
      WorldEdit.getInstance().getBlockFactory().register(new WorldEditCustomBlockParser());
      WorldEdit.getInstance().getEventBus().register(new WorldEditHandler(plugin));
    }
    return ENABLED;
  }

  public static boolean isEnabled() {
    return ENABLED;
  }
}
