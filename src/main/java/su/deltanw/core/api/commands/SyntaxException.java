package su.deltanw.core.api.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;

public class SyntaxException extends CommandSyntaxException {
  private static final CommandExceptionType EMPTY_TYPE = new CommandExceptionType() { };
  private static final Message EMPTY_MESSAGE = () -> "";

  private Component component;

  public SyntaxException(Component component) {
    super(EMPTY_TYPE, EMPTY_MESSAGE);
    this.component = component;
  }

  public Component getComponent() {
    return this.component;
  }
}
