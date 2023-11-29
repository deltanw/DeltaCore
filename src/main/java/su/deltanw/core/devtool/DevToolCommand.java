package su.deltanw.core.devtool;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

import com.google.common.collect.Streams;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import su.deltanw.core.api.Menu;
import su.deltanw.core.impl.block.CustomBlock;
import su.deltanw.core.api.commands.BrigadierCommand;
import su.deltanw.core.api.commands.CommandSource;
import su.deltanw.core.impl.item.CustomItem;

public class DevToolCommand extends BrigadierCommand {

  public DevToolCommand() {
    super("devtool");

    // FIXME: permission
    // this.setPermission("commands.devtool");

    this.executes((context, source) -> {
      // FIXME: help message?
    });

    this.subcommand("blocks", this::openBlocks, blocks -> { });
    this.subcommand("items", this::openItems, items -> { });

    this.subcommand("give", (context, source) -> {
      // FIXME: help message?
    }, give -> {
      give.stringArrayArgument("identifier", greedyString(), Streams.concat(
        CustomBlock.getAll().stream().map(CustomBlock::key),
        CustomItem.getAll().stream().map(CustomItem::key)
      ).map(NamespacedKey::toString).toList(), this::giveItem, identifier -> { });
    });
  }

  public void openBlocks(CommandContext<CommandSource> context, CommandSource source) throws CommandSyntaxException {
    this.openMenu(source, new BlocksMenu(source.core(), CustomBlock.getAll(), 0));
  }

  public void openItems(CommandContext<CommandSource> context, CommandSource source) throws CommandSyntaxException {
    this.openMenu(source, new ItemsMenu(source.core(), CustomItem.getAll(), 0));
  }

  public void openMenu(CommandSource source, Menu menu) throws CommandSyntaxException {
    source.core().getMenus().openMenu(menu, source.toPlayerOrThrow());
  }

  public void giveItem(CommandContext<CommandSource> context, CommandSource source, String argument) throws CommandSyntaxException {
    ItemStack itemToGive = null;
    NamespacedKey namespacedKey = NamespacedKey.fromString(argument);

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
      source.toPlayerOrThrow().getInventory().addItem(itemToGive);
    }
  }
}
