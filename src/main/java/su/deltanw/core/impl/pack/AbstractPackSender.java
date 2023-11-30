package su.deltanw.core.impl.pack;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import su.deltanw.core.api.pack.PackSender;
import su.deltanw.core.api.pack.UploadedResourcePack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractPackSender implements PackSender {

  protected final List<Player> trackingPlayers;
  protected Component defaultMessage = null;

  public AbstractPackSender(List<Player> tackingPlayers) {
    this.trackingPlayers = tackingPlayers;
  }

  public AbstractPackSender() {
    this(new ArrayList<>());
  }

  @Override
  public void setDefaultMessage(Component component) {
    defaultMessage = component;
  }

  @Override
  public Component getDefaultMessage() {
    return defaultMessage;
  }

  @Override
  public void addTrackingPlayer(Player player) {
    trackingPlayers.add(player);
  }

  @Override
  public void removeTrackingPlayer(Player player) {
    trackingPlayers.remove(player);
  }

  @Override
  public Collection<Player> getTrackingPlayers() {
    return trackingPlayers;
  }

  @Override
  public void trackingSend(UploadedResourcePack pack, Component message) {
    trackingPlayers.forEach(player -> send(player, pack, message));
  }
}
