package ru.arbuzikland.ucore.impl;

import ru.arbuzikland.ucore.api.Menu;
import ru.arbuzikland.ucore.api.Menus;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class MenusImpl implements Menus {

  private final Map<Inventory, Menu> sessions = new HashMap<>();

  @Override
  public void register(Inventory inventory, Menu menu) {
    this.sessions.put(inventory, menu);
  }

  @Override
  public void unregister(Inventory inventory) {
    this.sessions.remove(inventory);
  }

  @EventHandler
  public void onClick(InventoryClickEvent event) {
    Menu menu = sessions.get(event.getView().getTopInventory());
    if (menu != null) {
      event.setCancelled(true);
      HumanEntity whoClicked = event.getWhoClicked();
      if (whoClicked instanceof Player player) {
        menu.onClick(event.getRawSlot(), player);
      }
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onClose(InventoryCloseEvent event) {
    sessions.remove(event.getView().getTopInventory());
  }

  @Override
  public void openMenu(Menu menu, Player player) {
    Inventory inventory = menu.createInventory(player);
    register(inventory, menu);
    sessions.remove(player.getOpenInventory().getTopInventory());
    player.openInventory(inventory);
  }
}
