package su.deltanw.core.devtool;

import com.google.common.collect.Streams;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import su.deltanw.core.impl.block.CustomBlock;
import su.deltanw.core.impl.commands.BrigadierCommand;
import su.deltanw.core.impl.commands.PlayerSource;
import su.deltanw.core.impl.commands.arguments.Arguments;
import su.deltanw.core.impl.item.CustomItem;

public class DevToolCommand extends BrigadierCommand {

  public DevToolCommand() {
    this.setNames("devtool");

    // FIXME: permission
    // this.setPermission("commands.devtool");
  }

  @Override
  public void buildCommand(LiteralArgumentBuilder<PlayerSource> builder) {
    this.buildDefault(builder);

    this.buildBlocks(builder);
    this.buildItems(builder);
    this.buildGive(builder);
  }

  private void buildDefault(LiteralArgumentBuilder<PlayerSource> builder) {
    builder.executes(context -> {
      // FIXME: help message?
      return 0;
    });
  }

  private void buildBlocks(LiteralArgumentBuilder<PlayerSource> builder) {
    builder.then(Arguments.literal("blocks").executes(ctx -> {
      PlayerSource source = ctx.getSource();
      source.core().getMenus().openMenu(new BlocksMenu(source.core(), CustomBlock.getAll(), 0), source.asPlayer());
      return 0;
    }));
  }

  private void buildItems(LiteralArgumentBuilder<PlayerSource> builder) {
    builder.then(Arguments.literal("items").executes(ctx -> {
      PlayerSource source = ctx.getSource();
      source.core().getMenus().openMenu(new ItemsMenu(source.core(), CustomItem.getAll(), 0), source.asPlayer());
      return 0;
    }));
  }

  private void buildGive(LiteralArgumentBuilder<PlayerSource> builder) {
    builder.then(Arguments.namespace("give", Streams.concat(
        CustomBlock.getAll().stream().map(CustomBlock::key),
        CustomItem.getAll().stream().map(CustomItem::key)
    ), ctx -> {
      ItemStack itemToGive = null;
      NamespacedKey namespacedKey = NamespacedKey.fromString(ctx.getArgument("give", String.class));

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
        ctx.getSource().asPlayer().getInventory().addItem(itemToGive);
      }
      return 0;
    })).executes(context -> {
      // FIXME: help message?
      return 0;
    });
  }
}
