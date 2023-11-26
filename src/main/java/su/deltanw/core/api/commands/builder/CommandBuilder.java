package su.deltanw.core.api.commands.builder;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import java.util.function.Consumer;
import su.deltanw.core.api.commands.CommandSource;
import su.deltanw.core.api.commands.ExecutableArgument;
import su.deltanw.core.api.commands.ExecutableCommand;

public class CommandBuilder extends com.mojang.brigadier.builder.ArgumentBuilder<CommandSource, CommandBuilder> {
  private String name;

  protected CommandBuilder(String name) {
    this.name = name;
  }

  public static CommandBuilder of(String literal) {
    return new CommandBuilder(literal);
  }

  public CommandBuilder executes(ExecutableCommand executable) {
    return this.executes(context -> {
      executable.execute(context, context.getSource());
      return 0;
    });
  }

  public CommandBuilder argument(String literal, Consumer<CommandBuilder> command) {
    CommandBuilder argument = CommandBuilder.of(literal);
    command.accept(argument);
    return this.then(argument);
  }

  public <T> CommandBuilder argument(String name, ArgumentType<T> argumentType,
      Consumer<ArgumentBuilder<T>> builder, ExecutableArgument<T> consumer) {
    return this.argument(name, argumentType, command -> {
      command.executes(context -> {
        consumer.execute(context, context.getSource(), null);
        return 0;
      });
    }, argument -> {
      argument.executes(context -> {
        consumer.execute(context, context.getSource(), (T) context.getArgument(name, Object.class));
        return 0;
      });

      builder.accept(argument);
    });
  }

  public <T> CommandBuilder argument(String name, ArgumentType<T> argumentType,
      Consumer<CommandBuilder> command, Consumer<ArgumentBuilder<T>> argument) {
    return this.argument(name, builder -> {
      ArgumentBuilder<T> arg = ArgumentBuilder.of(name, argumentType);
      argument.accept(arg);
      command.accept(builder.then(arg));
    });
  }

  public CommandBuilder voidArgument(String name, ExecutableCommand command, Consumer<CommandBuilder> builder) {
    return this.argument(name, argument -> {
      argument.executes(command);
      builder.accept(argument);
    });
  }

  public CommandBuilder stringArgument(String name, StringArgumentType type,
      ExecutableArgument<String> consumer, Consumer<ArgumentBuilder<String>> builder) {
    return this.argument(name, type, builder, consumer);
  }

  public CommandBuilder booleanArgument(String name,
      ExecutableArgument<Boolean> consumer, Consumer<ArgumentBuilder<Boolean>> builder) {
    return this.argument(name, BoolArgumentType.bool(), builder, consumer);
  }

  public CommandBuilder integerArgument(String name, IntegerArgumentType type,
      ExecutableArgument<Integer> consumer, Consumer<ArgumentBuilder<Integer>> builder) {
    return this.argument(name, type, builder, consumer);
  }

  public CommandBuilder integerArgument(String name,
      ExecutableArgument<Integer> consumer, Consumer<ArgumentBuilder<Integer>> builder) {
    return this.argument(name, IntegerArgumentType.integer(), builder, consumer);
  }

  public CommandBuilder floatArgument(String name, FloatArgumentType type,
      ExecutableArgument<Float> consumer, Consumer<ArgumentBuilder<Float>> builder) {
    return this.argument(name, type, builder, consumer);
  }

  public CommandBuilder floatArgument(String name,
      ExecutableArgument<Float> consumer, Consumer<ArgumentBuilder<Float>> builder) {
    return this.argument(name, FloatArgumentType.floatArg(), builder, consumer);
  }

  public CommandBuilder doubleArgument(String name, DoubleArgumentType type,
      ExecutableArgument<Double> consumer, Consumer<ArgumentBuilder<Double>> builder) {
    return this.argument(name, type, builder, consumer);
  }

  public CommandBuilder doubleArgument(String name,
      ExecutableArgument<Double> consumer, Consumer<ArgumentBuilder<Double>> builder) {
    return this.argument(name, DoubleArgumentType.doubleArg(), builder, consumer);
  }

  public CommandBuilder longArgument(String name, LongArgumentType type,
      ExecutableArgument<Long> consumer, Consumer<ArgumentBuilder<Long>> builder) {
    return this.argument(name, type, builder, consumer);
  }

  public CommandBuilder longArgument(String name,
      ExecutableArgument<Long> consumer, Consumer<ArgumentBuilder<Long>> builder) {
    return this.argument(name, LongArgumentType.longArg(), builder, consumer);
  }

  public CommandBuilder stringArrayArgument(String name, StringArgumentType argumentType, List<String> keys,
      ExecutableArgument<String> executable, Consumer<ArgumentBuilder<String>> builder) {
    return this.argument(name, argumentType, command -> {
      command.executes(context -> {
        executable.execute(context, context.getSource(), null);
        return 0;
      });
    }, argument -> {
      argument.suggests((context, suggestions) -> {
        String remaining = suggestions.getRemaining();
        for (String key : keys) {
          if (key.startsWith(remaining)) {
            suggestions.suggest(key);
          }
        }

        return suggestions.buildFuture();
      });

      argument.executes(context -> {
        executable.execute(context, context.getSource(), context.getArgument(name, String.class));
        return 0;
      });

      builder.accept(argument);
    });
  }

  @Override
  protected CommandBuilder getThis() {
    return this;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public CommandNode<CommandSource> build() {
    LiteralCommandNode<CommandSource> result = new LiteralCommandNode<>(
        getName(), getCommand(), getRequirement(), getRedirect(), getRedirectModifier(), isFork());

    for (CommandNode<CommandSource> argument : getArguments()) {
      result.addChild(argument);
    }

    return result;
  }
}
