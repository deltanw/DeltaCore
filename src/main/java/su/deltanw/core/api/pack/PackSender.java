package su.deltanw.core.api.pack;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Collection;

public interface PackSender {

  void setDefaultMessage(Component component);

  Component getDefaultMessage();

  void send(Player player, UploadedResourcePack pack, Component message);

  default void send(Player player, UploadedResourcePack pack) {
    send(player, pack, getDefaultMessage());
  }

  void addTrackingPlayer(Player player);

  void removeTrackingPlayer(Player player);

  Collection<Player> getTrackingPlayers();

  void trackingSend(UploadedResourcePack pack, Component message);

  default void trackingSend(UploadedResourcePack pack) {
    trackingSend(pack, getDefaultMessage());
  }
}
