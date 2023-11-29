package su.deltanw.core.api.commands.builder;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.function.Predicate;
import su.deltanw.core.api.commands.CommandSource;
import su.deltanw.core.api.commands.ExecutableCommand;
import su.deltanw.core.api.commands.arguments.CustomArgumentType;

public class ArgumentBuilder<T> extends CommandBuilder {

  private final ArgumentType<T> type;
  private SuggestionProvider<CommandSource> suggestionsProvider = null;

  private ArgumentBuilder(String name, ArgumentType<T> type) {
    super(name);
    this.type = type;
  }

  public static <T> ArgumentBuilder<T> of(String name, ArgumentType<T> type) {
    return new ArgumentBuilder<>(name, type);
  }

  public ArgumentBuilder<T> executes(ExecutableCommand executable) {
    return (ArgumentBuilder<T>) super.executes(executable);
  }

  public ArgumentBuilder<T> suggests(SuggestionProvider<CommandSource> provider) {
    this.suggestionsProvider = provider;
    return getThis();
  }

  public SuggestionProvider<CommandSource> getSuggestionsProvider() {
    return this.suggestionsProvider;
  }

  @Override
  protected ArgumentBuilder<T> getThis() {
    return this;
  }

  public ArgumentType<T> getType() {
    return this.type;
  }

  @Override
  public CustomArgument<T, ?> build() {
    CustomArgument<T, ?> result = new CustomArgument<>(getName(), getType(),
        getCommand(), getRequirement(), getRedirect(),
        getRedirectModifier(), isFork(), getSuggestionsProvider());

    for (CommandNode<CommandSource> argument : getArguments()) {
      result.addChild(argument);
    }

    return result;
  }

  public static class CustomArgument<T, Y> extends ArgumentCommandNode<CommandSource, Y> {

    public CustomArgument(String name, ArgumentType type, Command command, Predicate requirement, CommandNode redirect,
        RedirectModifier modifier, boolean forks, SuggestionProvider customSuggestions) {
      super(name, type, command, requirement, redirect, modifier, forks,
          customSuggestions == null && type instanceof CustomArgumentType ? type::listSuggestions : customSuggestions);
    }

    @Override
    public ArgumentType<Y> getType() {
      if (super.getType() instanceof CustomArgumentType<?, ?> argument) {
        return (ArgumentType<Y>) argument.toClientType();
      }

      return super.getType();
    }

    @Override
    public RequiredArgumentBuilder<CommandSource, Y> createBuilder() {
      RequiredArgumentBuilder<CommandSource, Y> builder = RequiredArgumentBuilder.argument(getName(), getType());
      builder.requires(getRequirement());
      builder.forward(getRedirect(), getRedirectModifier(), isFork());
      builder.suggests(getCustomSuggestions());
      if (getCommand() != null) {
        builder.executes(getCommand());
      }
      return builder;
    }
  }
}
