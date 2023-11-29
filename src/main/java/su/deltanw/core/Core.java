package su.deltanw.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import com.jeff_media.customblockdata.CustomBlockData;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerQuitEvent;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import su.deltanw.core.api.ComponentFactory;
import su.deltanw.core.api.Menus;
import su.deltanw.core.api.Placeholder;
import su.deltanw.core.api.Placeholders;
import su.deltanw.core.api.injection.Injector;
import su.deltanw.core.api.pack.*;
import su.deltanw.core.config.BlocksConfig;
import su.deltanw.core.config.ItemsConfig;
import su.deltanw.core.devtool.DevToolCommand;
import su.deltanw.core.hook.worldedit.WorldEditHook;
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
import java.util.stream.Stream;
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
import su.deltanw.core.impl.item.CustomItem;
import su.deltanw.core.impl.pack.PackBuilderImpl;
import su.deltanw.core.impl.pack.PackSenderImpl;
import su.deltanw.core.impl.pack.PackUploaderImpl;

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

  private ObservablePackBuilder<?> defaultPackBuilder;
  private CachingPackUploader defaultPackUploader;
  private PackSender defaultPackSender;

  private Component listHeader;
  private Component listFooter;
  @MonotonicNonNull
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
    BlocksConfig.INSTANCE.reload(new File(getDataFolder(), "blocks.yml"));
    ItemsConfig.INSTANCE.reload(new File(getDataFolder(), "items.yml"));

    this.luckPerms = LuckPermsProvider.get();

    final InjectorImpl injectorImpl = new InjectorImpl();
    this.componentFactory = new ComponentFactoryImpl(PIXELS, PIXELS_EXT);
    this.placeholders = new PlaceholdersImpl();
    this.injector = injectorImpl;
    this.menus = new MenusImpl();

    this.defaultPackBuilder = new PackBuilderImpl()
        .withPackMeta(new PackMeta(4, "DeltaNetwork resource pack."));
    this.defaultPackBuilder.addObserver(this::updatePack);
    this.defaultPackUploader = new PackUploaderImpl(getDataFolder().toPath().resolve("pack/dist"));
    this.defaultPackSender = new PackSenderImpl();

    try {
      Path staticPackPath = getDataFolder().toPath().resolve("pack/static");
      Files.createDirectories(staticPackPath);
      try (Stream<Path> stream = Files.walk(staticPackPath)) {
          stream.filter(Files::isRegularFile)
              .forEach(path -> {
                try {
                  byte[] data = Files.readAllBytes(path);
                  String zipPath = staticPackPath.relativize(path).toString();
                  this.defaultPackBuilder.addFile(zipPath, data);
                } catch (IOException e) {
                  throw new RuntimeException("Couldn't read static pack file.", e);
                }
              });
      }
    } catch (IOException e) {
      throw new RuntimeException("Couldn't load static pack files.", e);
    }

    BlocksConfig.INSTANCE.CUSTOM_BLOCKS.forEach(value -> {
      NamespacedKey namespacedKey = NamespacedKey.fromString(value.CUSTOM_BLOCK_KEY, this);
      try {
        CustomBlock.register(namespacedKey, value.SERVERSIDE_BLOCK, value.CLIENTSIDE_BLOCK, value.BLOCK_ITEM);
      } catch (CommandSyntaxException e) {
        throw new IllegalArgumentException(e);
      }
    });

    getLogger().info("Loaded " + CustomBlock.getAll().size() + " custom blocks.");

    ItemsConfig.INSTANCE.CUSTOM_ITEMS.forEach(value -> {
      NamespacedKey namespacedKey = NamespacedKey.fromString(value.CUSTOM_ITEM_KEY, this);
      try {
        CustomItem.register(namespacedKey, value.SERVERSIDE_ITEM);
      } catch (CommandSyntaxException e) {
        throw new IllegalArgumentException(e);
      }
    });

    getLogger().info("Loaded " + CustomItem.getAll().size() + " custom items.");

    injector.addInjector(channel ->
        channel.pipeline().addBefore("packet_handler", "custom_block_handler", new CustomBlockNettyHandler(this)));

    injectorImpl.inject();
    getLogger().info("Successfully injected.");

    if (WorldEditHook.init(this)) {
      getLogger().info("Enabled WorldEdit hook.");
    }

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

    listHeader = Component.text("ஜ\n\n\n\n");
    listFooter = Component.text("\nwww.deltanw.su", TextColor.color(0xE17F30));

    try {
      errorComponent = componentFactory.buildComponent(ImageIO.read(new File(getDataFolder(), "error.png")));
    } catch (IOException e) {
      getLogger().warning("Unable to load error component image.");
      errorComponent = Component.text("[!]", NamedTextColor.RED);
    }

    try {
      this.defaultPackBuilder.build();
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

  public ObservablePackBuilder<?> getDefaultPackBuilder() {
    return defaultPackBuilder;
  }

  public CachingPackUploader getDefaultPackUploader() {
    return defaultPackUploader;
  }

  public PackSender getDefaultPackSender() {
    return defaultPackSender;
  }

  public Component getErrorComponent() {
    return errorComponent;
  }

  public LuckPerms getLuckPerms() {
    return luckPerms;
  }

  public void sendPack(Player player) throws IOException {
    UploadedResourcePack cache = defaultPackUploader.getCache();
    if (cache != null) {
      defaultPackSender.send(player, cache);
    } else {
      forceRebuildPack();
    }
  }

  public void updatePack() throws IOException {
    defaultPackBuilder.build();
  }

  public void forceUpdatePack() throws IOException {
    boolean needUpdate = !defaultPackBuilder.isDirty();
    ResourcePack pack = defaultPackBuilder.build();
    if (needUpdate) {
      updatePack(pack);
    }
  }

  public void forceRebuildPack() throws IOException {
    defaultPackBuilder.makeDirty();
    updatePack();
  }

  private void updatePack(ResourcePack pack) {
    try {
      UploadedResourcePack uploaded = defaultPackUploader.upload(pack);
      getLogger().info("Uploaded pack to " + uploaded.url());
      defaultPackSender.trackingSend(uploaded);
    } catch (IOException e) {
      throw new RuntimeException("Couldn't apply resource pack", e);
    }
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    player.sendPlayerListHeaderAndFooter(listHeader, listFooter);
    try {
      defaultPackSender.addTrackingPlayer(player);
      sendPack(player);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    defaultPackSender.removeTrackingPlayer(event.getPlayer());
  }
}
