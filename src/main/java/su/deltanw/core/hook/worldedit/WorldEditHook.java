package su.deltanw.core.hook.worldedit;

import com.sk89q.worldedit.WorldEdit;
import org.bukkit.Bukkit;
import su.deltanw.core.Core;

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
