package su.deltanw.core.api.commands;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.deltanw.core.Core;

public record CommandSource(Core core, CommandSender sender) {
  public boolean hasPermission(String permission) {
    return this.sender.hasPermission(permission);
  }

  public void sendMessage(net.kyori.adventure.text.Component component) {
    this.sender.sendMessage(component);
  }

  public void sendMessage(net.minecraft.network.chat.Component component) {
    this.sendMessage(PaperAdventure.asAdventure(component));
  }

  public Player asPlayer() throws SyntaxException {
    if (this.sender instanceof Player player) {
      return player;
    }

    throw new SyntaxException(Component.text("Только игроки могут использовать эту команду."));
  }
}
