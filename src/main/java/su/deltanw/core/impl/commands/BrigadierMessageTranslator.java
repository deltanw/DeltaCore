package su.deltanw.core.impl.commands;

import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class BrigadierMessageTranslator {
  private final Map<CommandExceptionType, UnaryOperator<CommandSyntaxException>> wrappedSyntax = new HashMap<>();

  public void addSyntax(CommandExceptionType type, UnaryOperator<CommandSyntaxException> syntax) {
    this.wrappedSyntax.put(type, syntax);
  }

  public CommandSyntaxException translateSyntax(CommandSyntaxException exception) {
    UnaryOperator<CommandSyntaxException> operator = this.wrappedSyntax.get(exception.getType());
    if (operator != null) {
      return operator.apply(exception);
    }

    return exception;
  }

  public void translateDefault() {
    // FIXME: translation
  }
}
