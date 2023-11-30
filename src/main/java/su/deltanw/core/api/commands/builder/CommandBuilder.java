package su.deltanw.core.api.commands.builder;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import su.deltanw.core.api.commands.CommandSource;
import su.deltanw.core.api.commands.arguments.PlayerArgument;

public class CommandBuilder extends com.mojang.brigadier.builder.ArgumentBuilder<CommandSource, CommandBuilder> {

  private String name;

  protected CommandBuilder(String name) {
    this.name = name;
  }

  public static CommandBuilder of(String name) {
    return new CommandBuilder(name);
  }

  public CommandBuilder executes(Command<CommandSource> command) {
    return super.executes(command);
  }

  public CommandBuilder subCommand(String name, Consumer<CommandBuilder> builder) {
    CommandBuilder command = CommandBuilder.of(name);
    builder.accept(command);
    return this.then(command);
  }

  public <T> CommandBuilder argument(String name, ArgumentType<T> argumentType,
      Consumer<ArgumentBuilder<T>> argument) {
    ArgumentBuilder<T> arg = ArgumentBuilder.of(name, argumentType);
    argument.accept(arg);
    return this.then(arg);
  }

  public CommandBuilder stringArg(String name, StringArgumentType type,
      Consumer<ArgumentBuilder<String>> builder) {
    return this.argument(name, type, builder);
  }

  public CommandBuilder booleanArg(String name,
      Consumer<ArgumentBuilder<Boolean>> builder) {
    return this.argument(name, BoolArgumentType.bool(), builder);
  }

  public CommandBuilder intArg(String name, IntegerArgumentType type,
      Consumer<ArgumentBuilder<Integer>> builder) {
    return this.argument(name, type, builder);
  }

  public CommandBuilder intArg(String name,
      Consumer<ArgumentBuilder<Integer>> builder) {
    return this.argument(name, IntegerArgumentType.integer(), builder);
  }

  public CommandBuilder floatArg(String name, FloatArgumentType type,
      Consumer<ArgumentBuilder<Float>> builder) {
    return this.argument(name, type, builder);
  }

  public CommandBuilder floatArg(String name,
      Consumer<ArgumentBuilder<Float>> builder) {
    return this.argument(name, FloatArgumentType.floatArg(), builder);
  }

  public CommandBuilder doubleArg(String name, DoubleArgumentType type,
      Consumer<ArgumentBuilder<Double>> builder) {
    return this.argument(name, type, builder);
  }

  public CommandBuilder doubleArg(String name,
      Consumer<ArgumentBuilder<Double>> builder) {
    return this.argument(name, DoubleArgumentType.doubleArg(), builder);
  }

  public CommandBuilder longArg(String name, LongArgumentType type,
      Consumer<ArgumentBuilder<Long>> builder) {
    return this.argument(name, type, builder);
  }

  public CommandBuilder longArg(String name,
      Consumer<ArgumentBuilder<Long>> builder) {
    return this.argument(name, LongArgumentType.longArg(), builder);
  }

  public CommandBuilder playerArg(String name, Object2BooleanFunction<Player> allowedPlayer,
      Consumer<ArgumentBuilder<Player>> builder) {
    return this.argument(name, PlayerArgument.player(allowedPlayer), builder);
  }

  public CommandBuilder stringArrayArg(String name, StringArgumentType argumentType,
      List<String> suggested, Consumer<ArgumentBuilder<String>> builder) {
    return this.argument(name, argumentType, arg -> {
      arg.suggests((context, suggestions) -> {
        String remaining = suggestions.getRemaining();
        for (String key : suggested) {
          if (key.startsWith(remaining)) {
            suggestions.suggest(key);
          }
        }

        return suggestions.buildFuture();
      });

      builder.accept(arg);
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
