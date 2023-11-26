package su.deltanw.core.impl.commands;

import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent;
import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendSuggestionsEvent;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.slf4j.Logger;
import su.deltanw.core.Core;
import su.deltanw.core.impl.commands.exceptions.SyntaxException;

public class BrigadierListener implements Listener {

  private static final Logger LOGGER = LogUtils.getLogger();

  private Core core;
  private CommandManager manager;

  public BrigadierListener(Core core) {
    this.core = core;
    this.manager = core.getCommandManager();
  }

  private StringReader createReader(String message) {
    StringReader reader = new StringReader(message);

    // Skip command identifier
    if (reader.canRead() && reader.peek() == '/') {
      reader.skip();
    }

    return reader;
  }

  private ParseResults<PlayerSource> parseCommand(String command, Player player) {
    StringReader reader = this.createReader(command);
    return this.manager.getDispatcher().parse(reader, new PlayerSource(this.core, player));
  }

  @EventHandler
  public void onCommands(AsyncPlayerSendCommandsEvent<CommandSourceStack> commands) {
    // Make sure that it runs first time (in Paper's ThreadPool)
    if (!commands.isAsynchronous()) {
      return;
    }

    RootCommandNode<CommandSourceStack> root = commands.getCommandNode();
    for (CommandNode<PlayerSource> child : this.manager.getDispatcher().getRoot().getChildren()) {
      // This should be safe as the packet is not parsing this command
      root.addChild((CommandNode<CommandSourceStack>) (Object) child);
    }
  }

  @EventHandler
  public void onExecute(PlayerCommandPreprocessEvent command) {
    ParseResults<PlayerSource> results = this.parseCommand(command.getMessage(), command.getPlayer());
    PlayerSource source = results.getContext().getSource();

    try {
      this.manager.getDispatcher().execute(results);
    } catch (CommandRuntimeException exception) {
      source.sendMessage(exception.getComponent());
    } catch (CommandSyntaxException syntax) {
      if (syntax instanceof SyntaxException customSyntax) {
        source.sendMessage(customSyntax.getComponent());
      }

      // No commands found, ignore
      SimpleCommandExceptionType exceptionType =
          CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand();
      if (exceptionType == syntax.getType()) {
        return;
      }

      source.sendMessage(ComponentUtils.fromMessage(syntax.getRawMessage()));
    } catch (Throwable throwable) {
      source.sendMessage(Component.translatable("command.failed"));
    }

    command.setCancelled(true);
  }

  @EventHandler
  public void onSuggestions(AsyncPlayerSendSuggestionsEvent suggestions) {
    ParseResults<PlayerSource> results = this.parseCommand(suggestions.getBuffer(), suggestions.getPlayer());

    this.manager.getDispatcher().getCompletionSuggestions(results)
        .thenAccept(suggested -> {
          if (!suggested.isEmpty()) {
            suggestions.setSuggestions(suggested);
            suggestions.setCancelled(false);
          }
        }).join();
  }
}
