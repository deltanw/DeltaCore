package su.deltanw.core.api.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public interface CustomArgumentType<T, Y> extends ArgumentType<T> {

  @Override
  default T parse(StringReader reader) throws CommandSyntaxException {
    return this.parse(this.toClientType().parse(reader));
  }

  T parse(Y value) throws CommandSyntaxException;

  ArgumentType<Y> toClientType();
}
