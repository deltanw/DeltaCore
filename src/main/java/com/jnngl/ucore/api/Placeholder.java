package com.jnngl.ucore.api;

import java.util.function.BiFunction;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;

public class Placeholder {

  private final String enclosing;
  private final String name;
  private final BiFunction<String, OfflinePlayer, Component> replacement;

  public Placeholder(String enclosing, String name, BiFunction<String, OfflinePlayer, Component> replacement) {
    this.enclosing = enclosing;
    this.name = name;
    this.replacement = replacement;
  }

  public Placeholder(String name, BiFunction<String, OfflinePlayer, Component> replacement) {
    this.enclosing = "%";
    this.name = name;
    this.replacement = replacement;
  }

  public String getEnclosing() {
    return enclosing;
  }

  public String getName() {
    return name;
  }

  public BiFunction<String, OfflinePlayer, Component> getReplacement() {
    return replacement;
  }
}
