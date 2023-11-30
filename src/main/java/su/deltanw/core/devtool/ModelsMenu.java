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
import su.deltanw.core.impl.model.CustomModel;

import java.util.List;

public class ModelsMenu implements Menu {

  private static final ItemStack PREV;
  private static final ItemStack NEXT;

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
  private final List<CustomModel> models;
  private int page;

  public ModelsMenu(Core core, List<CustomModel> models, int page) {
    this.core = core;
    this.models = models;
    this.page = page;
  }

  public ModelsMenu(Core core, List<CustomModel> models) {
    this(core, models, 0);
  }

  @Override
  public Inventory createInventory(Player player) {
    Inventory inventory = Bukkit.createInventory(player, 54,
        Component.text(INVENTORY_START + "같").color(NamedTextColor.WHITE).color(NamedTextColor.WHITE));

    ItemStack[] contents = new ItemStack[]{
        null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null,
        PREV, PREV, null, null, null, null, null, NEXT, NEXT,
    };

    models.stream()
        .skip(page * 45L).limit(45)
        .map(CustomModel::item)
        .toList().toArray(contents);

    inventory.setContents(contents);
    return inventory;
  }

  @Override
  public void onClick(int slot, Player player) {
    if (slot >= 0 && slot < 45) {
      int index = page * 45 + slot;
      if (index >= models.size()) {
        return;
      }

      CustomModel model = models.get(index);
      if (model == null) {
        return;
      }

      player.getInventory().addItem(model.item());
    } else if (slot == 45 || slot == 46) {
      if (page == 0) {
        return;
      }

      --page;
      core.getMenus().openMenu(this, player);
    } else if (slot == 52 || slot == 53) {
      if ((page + 1) * 45 >= models.size()) {
        return;
      }

      ++page;
      core.getMenus().openMenu(this, player);
    }
  }
}
