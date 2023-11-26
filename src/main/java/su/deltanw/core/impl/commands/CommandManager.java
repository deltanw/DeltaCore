package su.deltanw.core.impl.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import org.bukkit.command.CommandSender;
import org.slf4j.Logger;
import su.deltanw.core.Core;
import su.deltanw.core.api.commands.BrigadierCommand;
import su.deltanw.core.api.commands.CommandSource;
import su.deltanw.core.api.commands.SyntaxException;

public class CommandManager {

  private static final Logger LOGGER = LogUtils.getLogger();

  private final Core core;
  private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
  private final BrigadierMessageTranslator messageTranslator;
  private final BrigadierConsoleHighlighter highlighter;

  public CommandManager(Core core) {
    this.core = core;
    this.highlighter = new BrigadierConsoleHighlighter(core);

    this.messageTranslator = new BrigadierMessageTranslator();
    this.messageTranslator.translateDefault();
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

  public BrigadierMessageTranslator getMessageTranslator() {
    return this.messageTranslator;
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
      CommandSyntaxException translated = this.messageTranslator.translateSyntax(syntax);

      if (translated instanceof SyntaxException customSyntax) {
        source.sendMessage(customSyntax.getComponent());
        return true;
      }

      // No usable command found
      if (results.getContext().getNodes().isEmpty()) {
        return false;
      }

      MutableComponent component = Component.empty().withStyle(ChatFormatting.GRAY).withStyle(modifier ->
          modifier.withClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/" + input)));
      component.append(ComponentUtils.fromMessage(translated.getRawMessage()));

      if (translated.getInput() != null && translated.getCursor() >= 0) {
        int cursor = Math.min(translated.getInput().length(), translated.getCursor());

        component.append(Component.literal("\n"));
        if (cursor > 20) {
          component.append(CommonComponents.ELLIPSIS);
        }

        component.append(translated.getInput().substring(Math.max(0, cursor - 20), cursor));
        if (cursor < translated.getInput().length()) {
          component.append(Component.literal(translated.getInput().substring(cursor)).withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE));
        }

        component.append(Component.translatable("command.context.here").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
      }

      source.sendMessage(component);
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
