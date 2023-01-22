package com.jnngl.ucore.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

public interface Menus extends Listener  {

  void register(Inventory inventory, Menu menu);

  void unregister(Inventory inventory);

  void openMenu(Menu menu, Player player);
}
