package su.deltanw.core.devtool;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.deltanw.core.Core;
import su.deltanw.core.impl.block.CustomBlock;

import java.util.List;

// TODO: переписать на Brigadier
public class DevToolCommand implements CommandExecutor, TabCompleter {

  private final Core core;

  public DevToolCommand(Core core) {
    this.core = core;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player player)) {
      return true;
    }

    if (args.length < 1) {
      return true;
    }

    switch (args[0]) {
      case "give":
        if (args.length < 2) {
          return true;
        }

        String key = args[1];
        CustomBlock customBlock = CustomBlock.get(NamespacedKey.fromString(key));
        if (customBlock == null) {
          return true;
        }

        player.getInventory().addItem(customBlock.item());
        break;

      case "blocks":
        core.getMenus().openMenu(new BlocksMenu(core, CustomBlock.getAll(), 0), player);
        break;
    }

    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (args.length == 0) {
      return null;
    }

    if (args.length == 1) {
      return List.of("blocks", "give");
    }

    switch (args[0]) {
      case "give":
        if (args.length == 2) {
          return CustomBlock.getAll().stream().map(CustomBlock::key).map(NamespacedKey::toString).toList();
        }
        break;
    }

    return null;
  }
}
