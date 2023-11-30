package su.deltanw.core.api.commands.builder;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.function.Predicate;
import su.deltanw.core.api.commands.CommandSource;
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

  public ArgumentBuilder<T> executes(Command<CommandSource> command) {
    return (ArgumentBuilder<T>) super.executes(command);
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

    private CustomArgumentType<T, Y> customType;

    public CustomArgument(String name, ArgumentType type, Command command, Predicate requirement, CommandNode redirect,
        RedirectModifier modifier, boolean forks, SuggestionProvider customSuggestions) {
      super(name, type, command, requirement, redirect, modifier, forks,
          customSuggestions == null && type instanceof CustomArgumentType ? type::listSuggestions : customSuggestions);

      if (type instanceof CustomArgumentType<?,?> custom) {
        this.customType = (CustomArgumentType<T, Y>) custom;
      }
    }

    @Override
    public ArgumentType<Y> getType() {
      if (this.customType != null) {
        return this.customType.toClientType();
      }

      return super.getType();
    }

    @Override
    public void parse(StringReader reader,
        CommandContextBuilder<CommandSource> builder) throws CommandSyntaxException {
      int start = reader.getCursor();

      Object result;
      if (this.customType != null) {
        result = this.customType.parse(builder.getSource(), reader);
      } else {
        result = super.getType().parse(reader);
      }

      ParsedArgument<CommandSource, Object> parsed = new ParsedArgument<>(start, reader.getCursor(), result);

      builder.withArgument(this.getName(), parsed);
      builder.withNode(this, parsed.getRange());
    }

    @Override
    public boolean isValidInput(String input) {
      throw new UnsupportedOperationException("'ambiguities verification' is unsupported");
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
