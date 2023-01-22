package com.jnngl.ucore.impl;

import com.jnngl.ucore.api.Placeholder;
import com.jnngl.ucore.api.Placeholders;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.OfflinePlayer;

public class PlaceholdersImpl implements Placeholders {

  private final List<Placeholder> placeholders = new ArrayList<>();

  @Override
  public void addPlaceholder(Placeholder placeholder) {
    this.placeholders.add(placeholder);
  }

  @Override
  public synchronized Component expandPlaceholders(OfflinePlayer player, Component message) {
    for (Placeholder placeholder : this.placeholders) {
      String enclosingRegex = "\\" + placeholder.getEnclosing();
      message = message.replaceText(
          TextReplacementConfig.builder()
              .match("(\\\\)?(" + enclosingRegex + placeholder.getName() + "(\\.(.*?))?" + enclosingRegex + ")")
              .replacement((result, builder) -> {
                String group1 = result.group(1);
                if (group1 != null && group1.equals("\\")) {
                  return Component.text(result.group(2));
                }

                String argument = Objects.requireNonNullElse(result.group(4), "");
                return placeholder.getReplacement().apply(argument, player);
              })
              .build()
      );
    }

    return message;
  }
}
