package su.deltanw.core.api.commands.builder;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import su.deltanw.core.api.commands.CommandSource;
import su.deltanw.core.api.commands.ExecutableCommand;

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

  public ArgumentCommandNode<CommandSource, T> build() {
    ArgumentCommandNode<CommandSource, T> result = new ArgumentCommandNode<>(
        getName(), getType(), getCommand(), getRequirement(), getRedirect(), getRedirectModifier(), isFork(), getSuggestionsProvider());

    for (CommandNode<CommandSource> argument : getArguments()) {
      result.addChild(argument);
    }

    return result;
  }
}
