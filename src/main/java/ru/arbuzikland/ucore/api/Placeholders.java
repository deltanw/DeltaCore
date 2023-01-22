package ru.arbuzikland.ucore.api;

import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;

public interface Placeholders {

  void addPlaceholder(Placeholder placeholder);

  Component expandPlaceholders(OfflinePlayer player, Component message);

}
