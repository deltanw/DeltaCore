package su.deltanw.core.api.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;

public class CommandException extends CommandSyntaxException {

  private static final CommandExceptionType EMPTY_TYPE = new CommandExceptionType() { };
  private static final Message EMPTY_MESSAGE = () -> "";

  private Component component;

  public CommandException(Component component) {
    super(EMPTY_TYPE, EMPTY_MESSAGE);
    this.component = component;
  }

  public CommandException(Component component, String input, int cursor) {
    super(EMPTY_TYPE, EMPTY_MESSAGE, input, cursor);
    this.component = component;
  }

  public Component getComponent() {
    return this.component;
  }
}
