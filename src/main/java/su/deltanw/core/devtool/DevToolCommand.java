package su.deltanw.core.devtool;

import com.google.common.collect.Streams;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.deltanw.core.Core;
import su.deltanw.core.impl.block.CustomBlock;
import su.deltanw.core.impl.item.CustomItem;

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
      case "give" -> {
        if (args.length < 2) {
          return true;
        }
        String key = args[1];
        ItemStack itemToGive = null;
        NamespacedKey namespacedKey = NamespacedKey.fromString(key);
        CustomBlock customBlock = CustomBlock.get(namespacedKey);
        if (customBlock != null) {
          itemToGive = customBlock.item();
        }
        if (itemToGive == null) {
          CustomItem customItem = CustomItem.get(namespacedKey);
          if (customItem != null) {
            itemToGive = customItem.item();
          }
        }
        if (itemToGive != null) {
          player.getInventory().addItem(itemToGive);
        }
      }
      case "blocks" -> core.getMenus().openMenu(new BlocksMenu(core, CustomBlock.getAll(), 0), player);
      case "items" -> core.getMenus().openMenu(new ItemsMenu(core, CustomItem.getAll(), 0), player);
    }

    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (args.length == 0) {
      return null;
    }

    if (args.length == 1) {
      return List.of("blocks", "give", "items");
    }

    switch (args[0]) {
      case "give":
        if (args.length == 2) {
          return Streams.concat(
              CustomBlock.getAll().stream().map(CustomBlock::key),
              CustomItem.getAll().stream().map(CustomItem::key)
          ).map(NamespacedKey::toString).toList();
        }
        break;
    }

    return null;
  }
}
