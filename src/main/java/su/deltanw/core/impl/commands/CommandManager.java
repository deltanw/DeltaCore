package su.deltanw.core.impl.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.network.chat.ComponentUtils;
import org.bukkit.command.CommandSender;
import org.slf4j.Logger;
import su.deltanw.core.Core;
import su.deltanw.core.api.commands.BrigadierCommand;
import su.deltanw.core.api.commands.CommandSource;
import su.deltanw.core.api.commands.CommandException;

public class CommandManager {

  private static final Logger LOGGER = LogUtils.getLogger();

  private final Core core;
  private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
  private final BrigadierConsoleHighlighter highlighter;
  private final BrigadierTranslator translator;

  public CommandManager(Core core) {
    this.core = core;
    this.highlighter = new BrigadierConsoleHighlighter(core);
    this.translator = new BrigadierTranslator();

    this.reload();
  }

  public void reload() {
    CommandExceptions.INSTANCE.reload();
    this.translator.mapTypes(CommandSyntaxException.BUILT_IN_EXCEPTIONS, CommandExceptions.INSTANCE);
  }

  public void register(BrigadierCommand command) {
    this.dispatcher.getRoot().addChild(command.build());

    for (String alias : command.getAliases()) {
      command.setName(alias);
      this.dispatcher.getRoot().addChild(command.build());
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
    return this.getDispatcher().parse(this.prepareReader(command), new CommandSource(this.core, sender));
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
      // No usable command found
      if (results.getContext().getNodes().isEmpty()) {
        return false;
      }

      CommandSyntaxException translated = this.translator.translate(syntax);
      if (translated instanceof CommandException exception) {
        source.sendSyntaxHighlight(input, exception);
        return true;
      }

      source.sendMessage(ComponentUtils.fromMessage(translated.getRawMessage()));
    } catch (Throwable throwable) {
      LOGGER.error("Command exception: " + input, throwable);
      source.sendMessage(CommandExceptions.INSTANCE.commandFailed);
    }

    return true;
  }

  public void deject() {
    this.highlighter.deject();
  }
}
