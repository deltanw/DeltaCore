package su.deltanw.core.devtool;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

import com.google.common.collect.Streams;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
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

    this.executes(context -> {
      // FIXME: help message?
      return 0;
    });

    this.subCommand("blocks", blocks -> blocks.executes(this::openBlocks));
    this.subCommand("items", items -> items.executes(this::openItems));

    this.subCommand("give", give -> {
      give.executes(context -> {
        // FIXME: help message
        return 0;
      });

      List<String> keys = Streams.concat(
        CustomBlock.getAll().stream().map(CustomBlock::key),
        CustomItem.getAll().stream().map(CustomItem::key)
      ).map(NamespacedKey::toString).toList();

      give.stringArrayArg("identifier", greedyString(), keys, identifier -> {
        identifier.executes(this::giveItem);
      });
    });
  }

  public int openBlocks(CommandContext<CommandSource> context) throws CommandSyntaxException {
    return this.openMenu(context.getSource(),
        new BlocksMenu(context.getSource().core(), CustomBlock.getAll(), 0));
  }

  public int openItems(CommandContext<CommandSource> context) throws CommandSyntaxException {
    return this.openMenu(context.getSource(),
        new ItemsMenu(context.getSource().core(), CustomItem.getAll(), 0));
  }

  public int openMenu(CommandSource source, Menu menu) throws CommandSyntaxException {
    source.core().getMenus().openMenu(menu, source.toPlayerOrThrow());
    return 0;
  }

  public int giveItem(CommandContext<CommandSource> context) throws CommandSyntaxException {
    ItemStack itemToGive = null;
    NamespacedKey namespacedKey = NamespacedKey.fromString(context.getArgument("identifier", String.class));

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
      context.getSource().toPlayerOrThrow().getInventory().addItem(itemToGive);
    }

    return 0;
  }
}
