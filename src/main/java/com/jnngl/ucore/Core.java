package com.jnngl.ucore;

import com.jnngl.ucore.api.ComponentFactory;
import com.jnngl.ucore.api.Menus;
import com.jnngl.ucore.api.Placeholder;
import com.jnngl.ucore.api.Placeholders;
import com.jnngl.ucore.impl.ComponentFactoryImpl;
import com.jnngl.ucore.impl.MenusImpl;
import com.jnngl.ucore.impl.PlaceholdersImpl;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.imageio.ImageIO;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;

public final class Core extends JavaPlugin implements Listener {

  private static final char[] PIXELS = {
      0x0D03, 0x0D05, 0x0D06, 0x0D07, 0x0D08, 0x0D09, 0x0D0A, 0x0D0B, 0x0D0C, 0x0D0E, 0x0D0F
  };

  private static final char[] PIXELS_EXT = {
      0x0A08, 0x0A09, 0x0A0A, 0x0A0F, 0x0A10, 0x0A13, 0x0A14, 0x0A15, 0x0A16, 0x0A17, 0x0A18, 0x0A19, 0x0A1A, 0x0A1B, 0x0A1C, 0x0A1D, 0x0A1E, 0x0A1F, 0x0A20
  };

  private final Map<String, TextComponent> prefixes = new HashMap<>();

  private ComponentFactory componentFactory;
  private Placeholders placeholders;
  private Menus menus;

  private Component listHeader;
  private Component listFooter;
  private Component errorComponent;

  private Map<String, TextComponent> listComponents(File directory) {
    Map<String, TextComponent> componentMap = new HashMap<>();
    if (!directory.mkdirs()) {
      File[] prefixFiles = directory.listFiles();
      if (prefixFiles != null) {
        for (File prefixFile : prefixFiles) {
          try {
            BufferedImage prefixImage = ImageIO.read(prefixFile);
            String filename = prefixFile.getName();
            int index = filename.lastIndexOf('.');
            String name = (index == -1) ? filename : filename.substring(0, index);
            componentMap.put(name, this.componentFactory.buildComponent(prefixImage));
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }

    return componentMap;
  }

  public String getPrefix(Player player) {
    String prefixValue = player.getEffectivePermissions().stream()
        .filter(permission -> permission.getValue() && permission.getPermission().startsWith("chat.prefix."))
        .map(PermissionAttachmentInfo::getPermission)
        .max(Comparator.comparingInt(permission -> Integer.parseInt(permission.split("\\.")[2])))
        .orElse("");
    String[] parts = prefixValue.split("\\.", 4);
    if (parts.length > 3) {
      return parts[3];
    } else {
      return null;
    }
  }

  public String getFallbackPrefix(Player player) {
    String prefixValue = player.getEffectivePermissions().stream()
        .filter(permission -> permission.getValue() && permission.getPermission().startsWith("chat.fallback_prefix."))
        .map(PermissionAttachmentInfo::getPermission)
        .max(Comparator.comparingInt(permission -> Integer.parseInt(permission.split("\\.")[2])))
        .orElse("");
    String[] parts = prefixValue.split("\\.", 4);
    if (parts.length > 3) {
      return parts[3];
    } else {
      return null;
    }
  }

  @Override
  public void onEnable() {
    this.componentFactory = new ComponentFactoryImpl(PIXELS, PIXELS_EXT);
    this.placeholders = new PlaceholdersImpl();
    this.menus = new MenusImpl();

    Bukkit.getPluginManager().registerEvents(this, this);
    Bukkit.getPluginManager().registerEvents(menus, this);

    this.placeholders.addPlaceholder(new Placeholder(
        "player_name",
        (data, player) -> {
          Player online = player.getPlayer();
          if (online != null) {
            return online.name();
          } else {
            String name = player.getName();
            return Component.text(name != null ? name : "");
          }
        }
    ));

    this.prefixes.putAll(this.listComponents(new File(this.getDataFolder(), "prefixes")));
    getLogger().info("Loaded " + this.prefixes.size() + " prefixes");

    Map<String, TextComponent> emojis = this.listComponents(new File(this.getDataFolder(), "emojis"));
    emojis.forEach((name, component) -> placeholders.addPlaceholder(new Placeholder(":", name, (data, player) ->
        component
            .hoverEvent(HoverEvent.showText(
                Component.text(" ")
                    .append(component)
                    .append(
                        Component.text("  :" + name + ":")
                            .color(NamedTextColor.GRAY)
                    )
            ))
            .clickEvent(ClickEvent.suggestCommand(":" + name + ":"))
    )));
    getLogger().info("Loaded " + emojis.size() + " emojis");

    this.placeholders.addPlaceholder(new Placeholder(
        "prefix",
        (data, player) -> {
          Player online = player.getPlayer();
          if (online == null) {
            return Component.empty();
          }

          if (online.getResourcePackStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            return this.prefixes.getOrDefault(this.getPrefix(online), Component.empty());
          } else {
            String fallbackPrefix = this.getFallbackPrefix(online);
            return Optional.ofNullable(fallbackPrefix)
                .map(prefix -> LegacyComponentSerializer.legacyAmpersand().deserialize(fallbackPrefix))
                .orElse(Component.empty());
          }
        }
    ));

    this.placeholders.addPlaceholder(new Placeholder(
        "prefix_string",
        (data, player) -> {
          Player online = player.getPlayer();
          if (online == null) {
            return Component.empty();
          }

          return Component.text(Objects.requireNonNullElse(this.getPrefix(online), ""));
        }
    ));

    try {
      listHeader = Component.text("ஜ\n\n\n\n");
      listFooter = Component.text("\nOpen Beta", NamedTextColor.DARK_GRAY)
          .append(Component.text("\nwww.arbuzikland.ru", TextColor.color(0xDA726A)));
      errorComponent = componentFactory.buildComponent(ImageIO.read(new File(getDataFolder(), "error.png")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public ComponentFactory getComponentFactory() {
    return this.componentFactory;
  }

  public Placeholders getPlaceholders() {
    return placeholders;
  }

  public Menus getMenus() {
    return menus;
  }

  public Component getErrorComponent() {
    return errorComponent;
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    player.sendPlayerListHeaderAndFooter(listHeader, listFooter);
  }

  //  @EventHandler
  //  public void onPreLogin(AsyncPlayerPreLoginEvent event) {
  //    if (event.getName().equalsIgnoreCase("VuTuV")) {
  //      event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
  //      event.kickMessage(
  //          Component.text("Вы заблокированы на этом сервере!")
  //              .color(NamedTextColor.RED)
  //      );
  //    }
  //  }
}
