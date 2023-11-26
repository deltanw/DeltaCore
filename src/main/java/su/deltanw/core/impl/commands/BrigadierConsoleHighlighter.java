package su.deltanw.core.impl.commands;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.regex.Pattern;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.bukkit.Bukkit;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import su.deltanw.core.Core;
import su.deltanw.core.api.commands.CommandSource;

public class BrigadierConsoleHighlighter implements Highlighter {

  private static final int[] COLORS = { AttributedStyle.CYAN, AttributedStyle.YELLOW, AttributedStyle.GREEN, AttributedStyle.MAGENTA, AttributedStyle.BLUE };

  private final Core core;
  private Highlighter previous;

  public BrigadierConsoleHighlighter(Core core) {
    this.core = core;

    this.inject();
  }

  public void inject() {
    if (TerminalConsoleAppender.getReader() instanceof LineReaderImpl reader) {
      this.previous = reader.getHighlighter();
      reader.setHighlighter(this);
    }
  }

  public void deject() {
    if (TerminalConsoleAppender.getReader() instanceof LineReaderImpl reader) {
      reader.setHighlighter(this.previous);
    }
  }

  @Override
  public AttributedString highlight(LineReader reader, String buffer) {
    // Paper command highlighter, but with a custom CommandDispatcher
    AttributedStringBuilder builder = new AttributedStringBuilder();
    ParseResults<CommandSource> results = this.core.getCommandManager().getDispatcher().parse(
        this.core.getCommandManager().prepareReader(buffer), new CommandSource(this.core, Bukkit.getConsoleSender()));
    int pos = 0;
    if (buffer.startsWith("/")) {
      builder.append("/", AttributedStyle.DEFAULT);
      pos = 1;
    }

    int component = -1;
    for (ParsedCommandNode<CommandSource> node : results.getContext().getLastChild().getNodes()) {
      if (node.getRange().getStart() >= buffer.length()) {
        break;
      }

      int start = node.getRange().getStart();
      int end = Math.min(node.getRange().getEnd(), buffer.length());

      builder.append(buffer.substring(pos, start), AttributedStyle.DEFAULT);
      if (node.getNode() instanceof LiteralCommandNode) {
        builder.append(buffer.substring(start, end), AttributedStyle.DEFAULT);
      } else {
        if (++component >= COLORS.length) {
          component = 0;
        }
        builder.append(buffer.substring(start, end), AttributedStyle.DEFAULT.foreground(COLORS[component]));
      }

      pos = end;
    }

    if (pos < buffer.length()) {
      // There is no valid options found, jump to a previous one
      if (this.previous != null) {
        return this.previous.highlight(reader, buffer);
      }

      builder.append(buffer.substring(pos), AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
    }
    return builder.toAttributedString();
  }

  @Override
  public void setErrorPattern(Pattern errorPattern) {
    if (this.previous != null) {
      this.previous.setErrorPattern(errorPattern);
    }
  }

  @Override
  public void setErrorIndex(int errorIndex) {
    if (this.previous != null) {
      this.previous.setErrorIndex(errorIndex);
    }
  }
}
