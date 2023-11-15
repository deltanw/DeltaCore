package su.deltanw.core.devtool;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.deltanw.core.Core;
import su.deltanw.core.api.Menu;
import su.deltanw.core.impl.block.CustomBlock;

import java.util.List;

public class BlocksMenu implements Menu {

  private static ItemStack PREV;
  private static ItemStack NEXT;

  static {
    {
      PREV = new ItemStack(Material.STICK);
      ItemMeta meta = PREV.getItemMeta();
      meta.displayName(
          Component.text("Предыдущая страница")
              .color(NamedTextColor.GRAY)
              .decoration(TextDecoration.ITALIC, false)
      );
      meta.setCustomModelData(4000001);
      PREV.setItemMeta(meta);
    }

    {
      NEXT = new ItemStack(Material.STICK);
      ItemMeta meta = NEXT.getItemMeta();
      meta.displayName(
          Component.text("Следующая страница")
              .color(NamedTextColor.GRAY)
              .decoration(TextDecoration.ITALIC, false)
      );
      meta.setCustomModelData(4000001);
      NEXT.setItemMeta(meta);
    }
  }

  private final Core core;
  private final List<CustomBlock> blocks;
  private int page;

  public BlocksMenu(Core core, List<CustomBlock> blocks, int page) {
    this.core = core;
    this.blocks = blocks;
    this.page = page;
  }

  public BlocksMenu(Core core, List<CustomBlock> blocks) {
    this(core, blocks, 0);
  }

  @Override
  public Inventory createInventory(Player player) {
    Inventory inventory = Bukkit.createInventory(player, 54,
        Component.text(INVENTORY_START + "갖").color(NamedTextColor.WHITE).color(NamedTextColor.WHITE));

    ItemStack[] contents = new ItemStack[]{
        null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null,
        PREV, PREV, null, null, null, null, null, NEXT, NEXT,
    };

    blocks.stream()
        .skip(page * 45L).limit(45)
        .map(CustomBlock::item)
        .toList().toArray(contents);

    inventory.setContents(contents);
    return inventory;
  }

  @Override
  public void onClick(int slot, Player player) {
    if (slot >= 0 && slot < 45) {
      int index = page * 45 + slot;
      if (index >= blocks.size()) {
        return;
      }

      CustomBlock block = blocks.get(index);
      if (block == null) {
        return;
      }

      player.getInventory().addItem(block.item());
    } else if (slot == 45 || slot == 46) {
      if (page == 0) {
        return;
      }

      --page;
      core.getMenus().openMenu(this, player);
    } else if (slot == 52 || slot == 53) {
      if ((page + 1) * 45 >= blocks.size()) {
        return;
      }

      ++page;
      core.getMenus().openMenu(this, player);
    }
  }
}
