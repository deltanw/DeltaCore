package su.deltanw.core.impl.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import org.bukkit.command.CommandSender;
import org.slf4j.Logger;
import su.deltanw.core.Core;
import su.deltanw.core.impl.commands.exceptions.SyntaxException;

public class CommandManager {

  private static final Logger LOGGER = LogUtils.getLogger();

  private final Core core;
  private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
  private final BrigadierConsoleHighlighter highlighter;

  public CommandManager(Core core) {
    this.core = core;
    this.highlighter = new BrigadierConsoleHighlighter(core);
  }

  public void register(BrigadierCommand command) {
    for (String name : command.getNames()) {
      LiteralArgumentBuilder<CommandSource> builder =
          LiteralArgumentBuilder.<CommandSource>literal(name).requires(command);

      command.buildCommand(builder);

      this.dispatcher.register(builder);
    }
  }

  public CommandDispatcher<CommandSource> getDispatcher() {
    return this.dispatcher;
  }

  public StringReader prepareReader(String message) {
    StringReader reader = new StringReader(message);

    // Skip command identifier
    if (reader.canRead() && reader.peek() == '/') {
      reader.skip();
    }

    return reader;
  }

  public ParseResults<CommandSource> parseCommand(String command, CommandSender sender) {
    return this.getDispatcher().parse(prepareReader(command), new CommandSource(this.core, sender));
  }

  public Suggestions suggest(String command, CommandSender sender) {
    ParseResults<CommandSource> results = this.parseCommand(command, sender);
    return this.getDispatcher().getCompletionSuggestions(results).join();
  }

  public boolean executeCommand(String input, CommandSender sender) {
    ParseResults<CommandSource> results = this.parseCommand(input, sender);
    CommandSource source = results.getContext().getSource();

    try {
      this.getDispatcher().execute(results);
    } catch (CommandRuntimeException exception) {
      source.sendMessage(exception.getComponent());
    } catch (CommandSyntaxException syntax) {
      if (syntax instanceof SyntaxException customSyntax) {
        source.sendMessage(customSyntax.getComponent());
        return true;
      }

      // No command found
      SimpleCommandExceptionType exceptionType =
          CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand();
      if (exceptionType == syntax.getType()) {
        return false;
      }

      source.sendMessage(ComponentUtils.fromMessage(syntax.getRawMessage()));
    } catch (Throwable throwable) {
      LOGGER.error("Command exception: " + input, throwable);
      source.sendMessage(Component.translatable("command.failed"));
    }

    return true;
  }

  public void deject() {
    this.highlighter.deject();
  }
}
