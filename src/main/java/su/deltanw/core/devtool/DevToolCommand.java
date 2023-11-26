package su.deltanw.core.devtool;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

import com.google.common.collect.Streams;
import com.mojang.brigadier.context.CommandContext;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import su.deltanw.core.api.Menu;
import su.deltanw.core.impl.block.CustomBlock;
import su.deltanw.core.api.commands.BrigadierCommand;
import su.deltanw.core.api.commands.CommandSource;
import su.deltanw.core.api.commands.SyntaxException;
import su.deltanw.core.impl.item.CustomItem;

public class DevToolCommand extends BrigadierCommand {

  public DevToolCommand() {
    super("devtool");

    // FIXME: permission
    // this.setPermission("commands.devtool");

    this.executes((context, source) -> {
      // FIXME: help message?
    });

    this.voidArgument("blocks", this::openBlocks, blocks -> { });
    this.voidArgument("items", this::openItems, items -> { });

    this.stringArrayArgument("give", greedyString(), Streams.concat(
        CustomBlock.getAll().stream().map(CustomBlock::key),
        CustomItem.getAll().stream().map(CustomItem::key)
    ).map(NamespacedKey::toString).toList(), this::giveItem, give -> { });
  }

  public void openBlocks(CommandContext<CommandSource> context, CommandSource source) throws SyntaxException {
    this.openMenu(source, new BlocksMenu(source.core(), CustomBlock.getAll(), 0));
  }

  public void openItems(CommandContext<CommandSource> context, CommandSource source) throws SyntaxException {
    this.openMenu(source, new ItemsMenu(source.core(), CustomItem.getAll(), 0));
  }

  public void openMenu(CommandSource source, Menu menu) throws SyntaxException {
    source.core().getMenus().openMenu(menu, source.asPlayer());
  }

  public void giveItem(CommandContext<CommandSource> context, CommandSource source, String argument) throws SyntaxException {
    if (argument == null) {
      source.sendMessage(Component.text("Использование: /devtool give <ключ>"));
      return;
    }

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
      context.getSource().asPlayer().getInventory().addItem(itemToGive);
    }
  }
}
