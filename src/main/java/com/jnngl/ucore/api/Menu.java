package com.jnngl.ucore.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface Menu {

  String INVENTORY_RESET = "ണ".repeat(177);
  String INVENTORY_START = "ണണണണണണണണ";

  Inventory createInventory(Player player);
  void onClick(int slot, Player player);
}
