package su.deltanw.core.impl.pack;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import su.deltanw.core.api.pack.UploadedResourcePack;

public class PackSenderImpl extends AbstractPackSender {

  @Override
  public void send(Player player, UploadedResourcePack pack, Component message) {
    if (message != null) {
      player.setResourcePack(pack.url(), pack.hash(), true, message);
    } else {
      player.setResourcePack(pack.url(), pack.hash(), true);
    }
  }
}
