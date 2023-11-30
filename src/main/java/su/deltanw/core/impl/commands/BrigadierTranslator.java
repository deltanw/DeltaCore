package su.deltanw.core.impl.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

public class BrigadierTranslator {

  private final Map<CommandExceptionType, UnaryOperator<CommandSyntaxException>> translations = new HashMap<>();

  public CommandSyntaxException translate(CommandSyntaxException syntax) {
    UnaryOperator<CommandSyntaxException> operator = this.translations.get(syntax.getType());
    if (operator != null) {
      return operator.apply(syntax);
    }

    return syntax;
  }

  public void mapTypes(BuiltInExceptionProvider current, BuiltInExceptionProvider mapping) {
    this.mapType(current.doubleTooLow(), mapping::doubleTooLow);
    this.mapType(current.doubleTooHigh(), mapping::doubleTooHigh);
    this.mapType(current.floatTooLow(), mapping::floatTooLow);
    this.mapType(current.floatTooHigh(), mapping::floatTooHigh);
    this.mapType(current.integerTooLow(), mapping::integerTooLow);
    this.mapType(current.integerTooHigh(), mapping::integerTooHigh);
    this.mapType(current.longTooLow(), mapping::longTooLow);
    this.mapType(current.longTooHigh(), mapping::longTooHigh);
    this.mapType(current.literalIncorrect(), mapping::literalIncorrect);
    this.mapType(current.readerExpectedStartOfQuote(), mapping::readerExpectedStartOfQuote);
    this.mapType(current.readerExpectedEndOfQuote(), mapping::readerExpectedEndOfQuote);
    this.mapType(current.readerInvalidEscape(), mapping::readerInvalidEscape);
    this.mapType(current.readerInvalidBool(), mapping::readerInvalidBool);
    this.mapType(current.readerInvalidInt(), mapping::readerInvalidInt);
    this.mapType(current.readerExpectedInt(), mapping::readerExpectedInt);
    this.mapType(current.readerInvalidLong(), mapping::readerInvalidLong);
    this.mapType(current.readerInvalidLong(), mapping::readerInvalidLong);
    this.mapType(current.readerExpectedLong(), mapping::readerExpectedLong);
    this.mapType(current.readerInvalidDouble(), mapping::readerInvalidDouble);
    this.mapType(current.readerExpectedDouble(), mapping::readerExpectedDouble);
    this.mapType(current.readerInvalidFloat(), mapping::readerInvalidFloat);
    this.mapType(current.readerExpectedFloat(), mapping::readerExpectedFloat);
    this.mapType(current.readerExpectedBool(), mapping::readerExpectedBool);
    this.mapType(current.readerExpectedSymbol(), mapping::readerExpectedSymbol);
    this.mapType(current.dispatcherUnknownCommand(), mapping::dispatcherUnknownCommand);
    this.mapType(current.dispatcherUnknownArgument(), mapping::dispatcherUnknownArgument);
    this.mapType(current.readerExpectedSymbol(), mapping::readerExpectedSymbol);
    this.mapType(current.dispatcherExpectedArgumentSeparator(), mapping::dispatcherExpectedArgumentSeparator);
    this.mapType(current.dispatcherParseException(), mapping::dispatcherParseException);
  }

  public void mapType(Dynamic2CommandExceptionType current, Supplier<Dynamic2CommandExceptionType> mapping) {
    this.addSyntax(current, exception -> {
      Dynamic2CommandExceptionType type = mapping.get();
      if (exception.getRawMessage() instanceof Component component
          && component.getContents() instanceof TranslatableContents contents) {
        return type.createWithContext(this.parseReader(exception), contents.getArgs()[0], contents.getArgs()[1]);
      }

      return exception;
    });
  }

  public void mapType(DynamicCommandExceptionType current, Supplier<DynamicCommandExceptionType> mapping) {
    this.addSyntax(current, exception -> {
      DynamicCommandExceptionType type = mapping.get();
      if (exception.getRawMessage() instanceof Component component
          && component.getContents() instanceof TranslatableContents contents) {
        return type.createWithContext(this.parseReader(exception), contents.getArgs()[0]);
      }

      return exception;
    });
  }

  public void mapType(SimpleCommandExceptionType current, Supplier<SimpleCommandExceptionType> mapping) {
    this.addSyntax(current, exception -> {
      return mapping.get().createWithContext(this.parseReader(exception));
    });
  }

  private StringReader parseReader(CommandSyntaxException syntax) {
    StringReader reader = new StringReader(syntax.getInput());
    reader.setCursor(syntax.getCursor());
    return reader;
  }

  public void addSyntax(CommandExceptionType current, UnaryOperator<CommandSyntaxException> mapper) {
    this.translations.put(current, mapper);
  }
}
