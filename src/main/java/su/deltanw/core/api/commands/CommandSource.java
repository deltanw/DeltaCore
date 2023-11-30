package su.deltanw.core.api.commands;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.adventure.PaperAdventure;
import net.elytrium.commons.config.Placeholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.deltanw.core.Core;
import su.deltanw.core.config.MessagesConfig;
import su.deltanw.core.impl.commands.CommandExceptions;

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

  public Player toPlayer() {
    if (this.sender instanceof Player player) {
      return player;
    }

    return null;
  }

  public Player toPlayerOrThrow() throws CommandSyntaxException {
    Player player = this.toPlayer();
    if (player != null) {
      return player;
    }

    throw CommandExceptions.INSTANCE.playersOnly.create();
  }

  public void sendSyntaxHighlight(String input, CommandException exception) {
    net.kyori.adventure.text.Component component = exception.getComponent();

    if (exception.getInput() != null && exception.getCursor() >= 0) {
      int cursor = Math.min(exception.getInput().length(), exception.getCursor());

      component = component.append(text("\n"));

      String incorrectPart = exception.getInput().substring(Math.max(0, cursor - 10), cursor);
      if (cursor > 10) {
        incorrectPart = "..." + incorrectPart;
      }

      incorrectPart += Placeholders.replace(MessagesConfig.INSTANCE.MAIN.COMMAND_INCORRECT_PART,
          exception.getInput().substring(cursor));

      component = component.append(Core.getSerializer().deserialize(
          Placeholders.replace(MessagesConfig.INSTANCE.MAIN.COMMAND_SYNTAX, incorrectPart)));
    }

    this.sendMessage(component);
  }
}
