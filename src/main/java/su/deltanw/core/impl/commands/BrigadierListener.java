package su.deltanw.core.impl.commands;

import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent;
import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendSuggestionsEvent;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent.Completion;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.papermc.paper.adventure.PaperAdventure;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ComponentUtils;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import su.deltanw.core.Core;

public class BrigadierListener implements Listener {

  private CommandManager manager;

  public BrigadierListener(Core core) {
    this.manager = core.getCommandManager();
  }

  @EventHandler(ignoreCancelled = true)
  public void onCommands(AsyncPlayerSendCommandsEvent<CommandSourceStack> commands) {
    // Make sure that it runs first time (in Paper's ThreadPool)
    if (!commands.isAsynchronous()) {
      return;
    }

    RootCommandNode<CommandSourceStack> root = commands.getCommandNode();
    for (CommandNode<CommandSource> child : this.manager.getDispatcher().getRoot().getChildren()) {
      // This should be safe as the packet is not parsing this command
      root.addChild((CommandNode<CommandSourceStack>) (Object) child);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onConsoleExecute(ServerCommandEvent command) {
    if (command.getSender() instanceof ConsoleCommandSender) {
      command.setCancelled(this.manager.executeCommand(command.getCommand(), command.getSender()));
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerExecute(PlayerCommandPreprocessEvent command) {
    command.setCancelled(this.manager.executeCommand(command.getMessage(), command.getPlayer()));
  }

  // Paper command completer, but with a custom CommandDispatcher
  @EventHandler(ignoreCancelled = true)
  public void onConsoleSuggestions(AsyncTabCompleteEvent tabComplete) {
    if (!(tabComplete.getSender() instanceof ConsoleCommandSender)) {
      return;
    }

    Suggestions suggested = this.manager.suggest(tabComplete.getBuffer(), tabComplete.getSender());
    if (suggested.isEmpty()) {
      return;
    }

    List<Completion> completions = new ArrayList<>();
    for (Suggestion suggestion : suggested.getList()) {
      if (suggestion.getText().isEmpty()) {
        continue;
      }

      if (suggestion.getTooltip() == null) {
        completions.add(Completion.completion(suggestion.getText()));
      } else {
        completions.add(Completion.completion(suggestion.getText(),
            PaperAdventure.asAdventure(ComponentUtils.fromMessage(suggestion.getTooltip()))));
      }
    }

    for (Completion completion : tabComplete.completions()) {
      if (suggested.getList().stream().anyMatch(it -> it.getText().equals(completion.suggestion()))) {
        continue;
      }
      completions.add(completion);
    }

    tabComplete.completions(completions);
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerSuggestions(AsyncPlayerSendSuggestionsEvent suggestions) {
    Suggestions suggested = this.manager.suggest(suggestions.getBuffer(), suggestions.getPlayer());
    if (!suggested.isEmpty()) {
      suggestions.setSuggestions(suggested);
      suggestions.setCancelled(false);
    }
  }
}
