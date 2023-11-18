package su.deltanw.core;

import java.util.UUID;

import com.jeff_media.customblockdata.CustomBlockData;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.NamespacedKey;
import su.deltanw.core.api.ComponentFactory;
import su.deltanw.core.api.Menus;
import su.deltanw.core.api.Placeholder;
import su.deltanw.core.api.Placeholders;
import su.deltanw.core.api.injection.Injector;
import su.deltanw.core.devtool.DevToolCommand;
import su.deltanw.core.impl.ComponentFactoryImpl;
import su.deltanw.core.impl.MenusImpl;
import su.deltanw.core.impl.PlaceholdersImpl;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.imageio.ImageIO;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import su.deltanw.core.impl.block.CustomBlock;
import su.deltanw.core.impl.block.CustomBlockListener;
import su.deltanw.core.impl.block.CustomBlockNettyHandler;
import su.deltanw.core.impl.injection.InjectorImpl;

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
  private Injector injector;
  private Menus menus;

  private Component listHeader;
  private Component listFooter;
  private Component errorComponent;

  private LuckPerms luckPerms;

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

  public String getPrefix(UUID player) {
    User user = luckPerms.getUserManager().loadUser(player).join();

    String prefixValue = user.resolveDistinctInheritedNodes(QueryOptions.nonContextual())
        .stream()
        .filter(node -> node.getValue() && node.getKey().startsWith("chat.prefix."))
        .map(Node::getKey)
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
    Settings.INSTANCE.reload(new File(getDataFolder(), "config.yml"));

    this.luckPerms = LuckPermsProvider.get();

    final InjectorImpl injectorImpl = new InjectorImpl();
    this.componentFactory = new ComponentFactoryImpl(PIXELS, PIXELS_EXT);
    this.placeholders = new PlaceholdersImpl();
    this.injector = injectorImpl;
    this.menus = new MenusImpl();

    Settings.INSTANCE.CUSTOM_BLOCKS.forEach(value -> {
      NamespacedKey namespacedKey = NamespacedKey.fromString(value.CUSTOM_BLOCK_KEY, this);
      try {
        CustomBlock.register(namespacedKey, value.SERVERSIDE_BLOCK, value.CLIENTSIDE_BLOCK, value.BLOCK_ITEM);
      } catch (CommandSyntaxException e) {
        throw new IllegalArgumentException(e);
      }
    });

    getLogger().info("Loaded " + Settings.INSTANCE.CUSTOM_BLOCKS.size() + " custom blocks.");

    injector.addInjector(channel ->
        channel.pipeline().addBefore("packet_handler", "custom_block_handler", new CustomBlockNettyHandler(this)));

    injectorImpl.inject();
    getLogger().info("Successfully injected.");

    Bukkit.getPluginManager().registerEvents(this, this);
    Bukkit.getPluginManager().registerEvents(menus, this);
    Bukkit.getPluginManager().registerEvents(new CustomBlockListener(this), this);
    CustomBlockData.registerListener(this);

    Objects.requireNonNull(Bukkit.getPluginCommand("devtool")).setExecutor(new DevToolCommand(this));

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
        (data, player) -> this.prefixes.getOrDefault(this.getPrefix(player.getUniqueId()), Component.empty())
    ));

    this.placeholders.addPlaceholder(new Placeholder(
        "prefix_string",
        (data, player) -> Component.text(Objects.requireNonNullElse(this.getPrefix(player.getUniqueId()), ""))
    ));

    try {
      listHeader = Component.text("ஜ\n\n\n\n");
      listFooter = Component.text("\nwww.deltanw.su", TextColor.color(0xE17F30));
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

  public Injector getInjector() {
    return injector;
  }

  public Menus getMenus() {
    return menus;
  }

  public Component getErrorComponent() {
    return errorComponent;
  }

  public LuckPerms getLuckPerms() {
    return luckPerms;
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
