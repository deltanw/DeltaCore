package su.deltanw.core.api.commands;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.adventure.PaperAdventure;
import net.elytrium.commons.config.Placeholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.deltanw.core.Core;
import su.deltanw.core.config.MessagesConfig;
import su.deltanw.core.impl.commands.SyntaxExceptions;

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

    throw SyntaxExceptions.INSTANCE.playersOnly.create();
  }

  public void sendSyntaxHighlight(String input, SyntaxException syntax) {
    net.kyori.adventure.text.Component component = syntax.getComponent();

    if (syntax.getInput() != null && syntax.getCursor() >= 0) {
      int cursor = Math.min(syntax.getInput().length(), syntax.getCursor());

      component = component.append(text("\n"));
      String argument = syntax.getInput().substring(Math.max(0, cursor - 10), cursor);
      if (cursor > 10) {
        argument = "..." + argument;
      }

      argument += Placeholders.replace(MessagesConfig.INSTANCE.BRIGADIER.COMMAND_INCORRECT_PART,
          syntax.getInput().substring(cursor));

      component = component.append(Core.getSerializer().deserialize(
          Placeholders.replace(MessagesConfig.INSTANCE.BRIGADIER.COMMAND_SYNTAX, argument)));
    }

    this.sendMessage(component);
  }
}
