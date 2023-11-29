package su.deltanw.core.api.commands.builder;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import su.deltanw.core.api.commands.CommandSource;
import su.deltanw.core.api.commands.ExecutableCommand;
import su.deltanw.core.api.commands.arguments.PlayerArgument;

public class CommandBuilder extends com.mojang.brigadier.builder.ArgumentBuilder<CommandSource, CommandBuilder> {

  private String name;

  protected CommandBuilder(String name) {
    this.name = name;
  }

  public static CommandBuilder of(String literal) {
    return new CommandBuilder(literal);
  }

  public CommandBuilder executes(ExecutableCommand executable) {
    return super.executes(executable);
  }

  public CommandBuilder subcommand(String literal, Consumer<CommandBuilder> builder) {
    CommandBuilder command = CommandBuilder.of(literal);
    builder.accept(command);
    return this.then(command);
  }

  public CommandBuilder subcommand(String name, ExecutableCommand command,
      Consumer<CommandBuilder> builder) {
    return this.subcommand(name, argument -> {
      argument.executes(command);
      builder.accept(argument);
    });
  }

  public <T> CommandBuilder argument(String name, ArgumentType<T> argumentType,
      ExecutableCommand command, Consumer<ArgumentBuilder<T>> builder) {
    return this.argument(name, argumentType, argument -> {
      argument.executes(context -> {
        command.execute(context, context.getSource());
        return 0;
      });

      builder.accept(argument);
    });
  }

  public <T> CommandBuilder argument(String name, ArgumentType<T> argumentType,
      Consumer<ArgumentBuilder<T>> argument) {
    ArgumentBuilder<T> arg = ArgumentBuilder.of(name, argumentType);
    argument.accept(arg);
    return this.then(arg);
  }

  public CommandBuilder stringArgument(String name, StringArgumentType type,
      ExecutableCommand command, Consumer<ArgumentBuilder<String>> builder) {
    return this.argument(name, type, command, builder);
  }

  public CommandBuilder booleanArgument(String name,
      ExecutableCommand command, Consumer<ArgumentBuilder<Boolean>> builder) {
    return this.argument(name, BoolArgumentType.bool(), command, builder);
  }

  public CommandBuilder integerArgument(String name, IntegerArgumentType type,
      ExecutableCommand command, Consumer<ArgumentBuilder<Integer>> builder) {
    return this.argument(name, type, command, builder);
  }

  public CommandBuilder integerArgument(String name,
      ExecutableCommand command, Consumer<ArgumentBuilder<Integer>> builder) {
    return this.argument(name, IntegerArgumentType.integer(), command, builder);
  }

  public CommandBuilder floatArgument(String name, FloatArgumentType type,
      ExecutableCommand command, Consumer<ArgumentBuilder<Float>> builder) {
    return this.argument(name, type, command, builder);
  }

  public CommandBuilder floatArgument(String name,
      ExecutableCommand command, Consumer<ArgumentBuilder<Float>> builder) {
    return this.argument(name, FloatArgumentType.floatArg(), command, builder);
  }

  public CommandBuilder doubleArgument(String name, DoubleArgumentType type,
      ExecutableCommand command, Consumer<ArgumentBuilder<Double>> builder) {
    return this.argument(name, type, command, builder);
  }

  public CommandBuilder doubleArgument(String name,
      ExecutableCommand command, Consumer<ArgumentBuilder<Double>> builder) {
    return this.argument(name, DoubleArgumentType.doubleArg(), command, builder);
  }

  public CommandBuilder longArgument(String name, LongArgumentType type,
      ExecutableCommand command, Consumer<ArgumentBuilder<Long>> builder) {
    return this.argument(name, type, command, builder);
  }

  public CommandBuilder longArgument(String name,
      ExecutableCommand command, Consumer<ArgumentBuilder<Long>> builder) {
    return this.argument(name, LongArgumentType.longArg(), command, builder);
  }

  public CommandBuilder playerArgument(String name, Object2BooleanFunction<Player> allowedPlayer,
      ExecutableCommand executable, Consumer<ArgumentBuilder<Player>> builder) {
    return this.argument(name, new PlayerArgument(allowedPlayer), argument -> {
      argument.executes(executable);
      builder.accept(argument);
    });
  }

  public CommandBuilder stringArrayArgument(String name, StringArgumentType argumentType, List<String> suggested,
      ExecutableCommand command, Consumer<ArgumentBuilder<String>> builder) {
    return this.stringArrayArgument(name, argumentType, (context, suggestions) -> {
      String remaining = suggestions.getRemaining();
      for (String key : suggested) {
        if (key.startsWith(remaining)) {
          suggestions.suggest(key);
        }
      }
    }, command, builder);
  }

  public CommandBuilder stringArrayArgument(String name, StringArgumentType argumentType,
      BiConsumer<CommandContext<CommandSource>, SuggestionsBuilder> suggests,
      ExecutableCommand command, Consumer<ArgumentBuilder<String>> builder) {
    return this.argument(name, argumentType, argument -> {
      argument.suggests((context, suggestions) -> {
        suggests.accept(context, suggestions);
        return suggestions.buildFuture();
      });

      argument.executes(context -> {
        command.execute(context, context.getSource());
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
