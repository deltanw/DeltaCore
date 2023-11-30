package su.deltanw.core.api.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import su.deltanw.core.api.commands.CommandSource;

public interface CustomArgumentType<T, Y> extends ArgumentType<T> {

  @Override
  default T parse(StringReader reader) {
    throw new IllegalStateException("argument is parsed in the wrong way");
  }

  default T parse(CommandSource source, StringReader reader) throws CommandSyntaxException {
    return this.parse(source, this.toClientType().parse(reader));
  }

  T parse(CommandSource source, Y value) throws CommandSyntaxException;

  ArgumentType<Y> toClientType();
}
