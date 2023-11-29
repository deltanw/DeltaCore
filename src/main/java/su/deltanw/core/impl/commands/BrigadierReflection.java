package su.deltanw.core.impl.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import su.deltanw.core.api.commands.CommandSource;

public final class BrigadierReflection {

  private static final MethodHandle MH_arguments;

  static {
    try {
      MH_arguments = MethodHandles.privateLookupIn(CommandContext.class, MethodHandles.lookup())
          .findGetter(CommandContext.class, "arguments", Map.class);
    } catch (Throwable e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private BrigadierReflection() {

  }

  public static Map<String, ParsedArgument<?, ?>> getArguments(CommandContext<CommandSource> context) {
    try {
      return (Map<String, ParsedArgument<?, ?>>) MH_arguments.invokeExact(context);
    } catch (Throwable e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static <T> void replaceArgument(CommandContext<CommandSource> context, String name, T arg) {
    Map<String, ParsedArgument<?, ?>> arguments = getArguments(context);

    ParsedArgument<?, ?> argument = arguments.get(name);
    if (argument != null) {
      StringRange range = argument.getRange();

      arguments.put(name, new ParsedArgument<>(range.getStart(), range.getEnd(), arg));
    }
  }

  public static <T> T getArgument(CommandContext<CommandSource> context, String name) {
    return (T) getArguments(context).get(name).getResult();
  }
}
