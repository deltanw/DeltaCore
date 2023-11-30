package su.deltanw.core.impl.commands;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.elytrium.commons.config.Placeholders;
import net.kyori.adventure.text.Component;
import su.deltanw.core.Core;
import su.deltanw.core.api.commands.CommandException;
import su.deltanw.core.config.MessagesConfig;
import su.deltanw.core.config.MessagesConfig.Main;

public class CommandExceptions implements BuiltInExceptionProvider {

  public static final CommandExceptions INSTANCE = new CommandExceptions();

  public SimpleCommandExceptionType playersOnly;
  public DynamicCommandExceptionType playerNotFound;
  public Component commandFailed;

  // Brigadier messages
  public Dynamic2CommandExceptionType doubleTooLow;
  public Dynamic2CommandExceptionType doubleTooHigh;
  public Dynamic2CommandExceptionType floatTooLow;
  public Dynamic2CommandExceptionType floatTooHigh;
  public Dynamic2CommandExceptionType integerTooLow;
  public Dynamic2CommandExceptionType integerTooHigh;
  public Dynamic2CommandExceptionType longTooLow;
  public Dynamic2CommandExceptionType longTooHigh;
  public DynamicCommandExceptionType literalIncorrect;
  public SimpleCommandExceptionType readerExpectedStartOfQuote;
  public SimpleCommandExceptionType readerExpectedEndOfQuote;
  public DynamicCommandExceptionType readerInvalidEscape;
  public DynamicCommandExceptionType readerInvalidBool;
  public DynamicCommandExceptionType readerInvalidInt;
  public SimpleCommandExceptionType readerExpectedInt;
  public DynamicCommandExceptionType readerInvalidLong;
  public SimpleCommandExceptionType readerExpectedLong;
  public DynamicCommandExceptionType readerInvalidDouble;
  public SimpleCommandExceptionType readerExpectedDouble;
  public DynamicCommandExceptionType readerInvalidFloat;
  public SimpleCommandExceptionType readerExpectedFloat;
  public SimpleCommandExceptionType readerExpectedBool;
  public DynamicCommandExceptionType readerExpectedSymbol;
  public SimpleCommandExceptionType dispatcherUnknownCommand;
  public SimpleCommandExceptionType dispatcherUnknownArgument;
  public SimpleCommandExceptionType dispatcherExpectedArgumentSeparator;
  public DynamicCommandExceptionType dispatcherParseException;

  public void reload() {
    MessagesConfig messages = MessagesConfig.INSTANCE;
    Main brigadier = messages.MAIN;

    this.playersOnly = deserializeStatic(brigadier.PLAYERS_ONLY);
    this.playerNotFound = deserializeDynamic(brigadier.PLAYER_NOT_FOUND);
    this.commandFailed = Core.getSerializer().deserialize(brigadier.COMMAND_FAILED);

    this.doubleTooLow = deserializeDynamicDouble(brigadier.DOUBLE_TOO_LOW);
    this.doubleTooHigh = deserializeDynamicDouble(brigadier.DOUBLE_TOO_HIGH);

    this.floatTooLow = deserializeDynamicDouble(brigadier.FLOAT_TOO_LOW);
    this.floatTooHigh = deserializeDynamicDouble(brigadier.FLOAT_TOO_HIGH);

    this.integerTooLow = deserializeDynamicDouble(brigadier.INTEGER_TOO_LOW);
    this.integerTooHigh = deserializeDynamicDouble(brigadier.INTEGER_TOO_HIGH);

    this.longTooLow = deserializeDynamicDouble(brigadier.LONG_TOO_LOW);
    this.longTooHigh = deserializeDynamicDouble(brigadier.LONG_TOO_HIGH);

    this.literalIncorrect = deserializeDynamic(brigadier.LITERAL_INCORRECT);

    this.readerExpectedStartOfQuote = deserializeStatic(brigadier.READER_EXPECTED_START_OF_QUOTE);
    this.readerExpectedEndOfQuote = deserializeStatic(brigadier.READER_EXPECTED_END_OF_QUOTE);

    this.readerInvalidEscape = deserializeDynamic(brigadier.READER_INVALID_ESCAPE);
    this.readerExpectedSymbol = deserializeDynamic(brigadier.READER_EXPECTED_SYMBOL);

    this.readerInvalidBool = deserializeDynamic(brigadier.READER_INVALID_BOOL);
    this.readerInvalidInt = deserializeDynamic(brigadier.READER_INVALID_INT);
    this.readerInvalidLong = deserializeDynamic(brigadier.READER_INVALID_LONG);
    this.readerInvalidFloat = deserializeDynamic(brigadier.READER_INVALID_FLOAT);
    this.readerInvalidDouble = deserializeDynamic(brigadier.READER_INVALID_DOUBLE);

    this.readerExpectedBool = deserializeStatic(brigadier.READER_EXPECTED_BOOL);
    this.readerExpectedInt = deserializeStatic(brigadier.READER_EXPECTED_INT);
    this.readerExpectedLong = deserializeStatic(brigadier.READER_EXPECTED_LONG);
    this.readerExpectedFloat = deserializeStatic(brigadier.READER_EXPECTED_FLOAT);
    this.readerExpectedDouble = deserializeStatic(brigadier.READER_EXPECTED_DOUBLE);

    this.dispatcherUnknownCommand = deserializeStatic(brigadier.DISPATCHER_UNKNOWN_COMMAND);
    this.dispatcherUnknownArgument = deserializeStatic(brigadier.DISPATCHER_UNKNOWN_ARGUMENT);
    this.dispatcherExpectedArgumentSeparator = deserializeStatic(brigadier.DISPATCHER_EXPECTED_ARGUMENT_SEPARATOR);
    this.dispatcherParseException = deserializeDynamic(brigadier.DISPATCHER_PARSE_EXCEPTION);
  }

  public static SimpleCommandExceptionType deserializeStatic(String message) {
    CommandException constant = new CommandException(Core.getSerializer().deserialize(message));
    return new SimpleCommandExceptionType(null) {
      @Override
      public CommandSyntaxException create() {
        return constant;
      }

      @Override
      public CommandSyntaxException createWithContext(ImmutableStringReader reader) {
        return new CommandException(constant.getComponent(), reader.getString(), reader.getCursor());
      }
    };
  }

  public static DynamicCommandExceptionType deserializeDynamic(String message) {
    if (!hasPlaceholders(message)) {
      CommandException constant = new CommandException(Core.getSerializer().deserialize(message));
      return new DynamicCommandExceptionType(null) {
        @Override
        public CommandSyntaxException create(Object arg) {
          return constant;
        }

        @Override
        public CommandSyntaxException createWithContext(ImmutableStringReader reader, Object arg) {
          return new CommandException(constant.getComponent(), reader.getString(), reader.getCursor());
        }
      };
    }

    return new DynamicCommandExceptionType(null) {
      @Override
      public CommandSyntaxException create(Object arg) {
        return new CommandException(Core.getSerializer().deserialize(Placeholders.replace(message, arg)));
      }

      @Override
      public CommandSyntaxException createWithContext(ImmutableStringReader reader, Object arg) {
        return new CommandException(Core.getSerializer().deserialize(Placeholders.replace(message, arg)), reader.getString(), reader.getCursor());
      }
    };
  }

  public static Dynamic2CommandExceptionType deserializeDynamicDouble(String message) {
    if (!hasPlaceholders(message)) {
      CommandException constant = new CommandException(Core.getSerializer().deserialize(message));
      return new Dynamic2CommandExceptionType(null) {
        @Override
        public CommandSyntaxException create(Object a, Object b) {
          return constant;
        }

        @Override
        public CommandSyntaxException createWithContext(ImmutableStringReader reader, Object a, Object b) {
          return new CommandException(constant.getComponent(), reader.getString(), reader.getCursor());
        }
      };
    }

    return new Dynamic2CommandExceptionType(null) {
      @Override
      public CommandSyntaxException create(Object a, Object b) {
        return new CommandException(Core.getSerializer().deserialize(Placeholders.replace(message, a, b)));
      }

      @Override
      public CommandSyntaxException createWithContext(ImmutableStringReader reader, Object a, Object b) {
        return new CommandException(Core.getSerializer().deserialize(Placeholders.replace(message, a, b)), reader.getString(), reader.getCursor());
      }
    };
  }

  private static boolean hasPlaceholders(String message) {
    if (!Placeholders.hasPlaceholders(message)) {
      return false;
    }

    // Test if message has defined placeholders
    String[] placeholders = Placeholders.getPlaceholders(message);
    if (placeholders == null || placeholders.length == 0) {
      return false;
    }

    // Test if there is any placeholders
    return !Placeholders.replace(message, "").equals(message);
  }

  @Override
  public Dynamic2CommandExceptionType doubleTooLow() {
    return this.doubleTooLow;
  }

  @Override
  public Dynamic2CommandExceptionType doubleTooHigh() {
    return this.doubleTooHigh;
  }

  @Override
  public Dynamic2CommandExceptionType floatTooLow() {
    return this.floatTooLow;
  }

  @Override
  public Dynamic2CommandExceptionType floatTooHigh() {
    return this.floatTooHigh;
  }

  @Override
  public Dynamic2CommandExceptionType integerTooLow() {
    return this.integerTooLow;
  }

  @Override
  public Dynamic2CommandExceptionType integerTooHigh() {
    return this.integerTooHigh;
  }

  @Override
  public Dynamic2CommandExceptionType longTooLow() {
    return this.longTooLow;
  }

  @Override
  public Dynamic2CommandExceptionType longTooHigh() {
    return this.longTooHigh;
  }

  @Override
  public DynamicCommandExceptionType literalIncorrect() {
    return this.literalIncorrect;
  }

  @Override
  public SimpleCommandExceptionType readerExpectedStartOfQuote() {
    return this.readerExpectedStartOfQuote;
  }

  @Override
  public SimpleCommandExceptionType readerExpectedEndOfQuote() {
    return this.readerExpectedEndOfQuote;
  }

  @Override
  public DynamicCommandExceptionType readerInvalidEscape() {
    return this.readerInvalidEscape;
  }

  @Override
  public DynamicCommandExceptionType readerInvalidBool() {
    return this.readerInvalidBool;
  }

  @Override
  public DynamicCommandExceptionType readerInvalidInt() {
    return this.readerInvalidInt;
  }

  @Override
  public SimpleCommandExceptionType readerExpectedInt() {
    return this.readerExpectedInt;
  }

  @Override
  public DynamicCommandExceptionType readerInvalidLong() {
    return this.readerInvalidLong;
  }

  @Override
  public SimpleCommandExceptionType readerExpectedLong() {
    return this.readerExpectedLong;
  }

  @Override
  public DynamicCommandExceptionType readerInvalidDouble() {
    return this.readerInvalidDouble;
  }

  @Override
  public SimpleCommandExceptionType readerExpectedDouble() {
    return this.readerExpectedDouble;
  }

  @Override
  public DynamicCommandExceptionType readerInvalidFloat() {
    return this.readerInvalidFloat;
  }

  @Override
  public SimpleCommandExceptionType readerExpectedFloat() {
    return this.readerExpectedFloat;
  }

  @Override
  public SimpleCommandExceptionType readerExpectedBool() {
    return this.readerExpectedBool;
  }

  @Override
  public DynamicCommandExceptionType readerExpectedSymbol() {
    return this.readerExpectedSymbol;
  }

  @Override
  public SimpleCommandExceptionType dispatcherUnknownCommand() {
    return this.dispatcherUnknownCommand;
  }

  @Override
  public SimpleCommandExceptionType dispatcherUnknownArgument() {
    return this.dispatcherUnknownArgument;
  }

  @Override
  public SimpleCommandExceptionType dispatcherExpectedArgumentSeparator() {
    return this.dispatcherExpectedArgumentSeparator;
  }

  @Override
  public DynamicCommandExceptionType dispatcherParseException() {
    return this.dispatcherParseException;
  }
}
