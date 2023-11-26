package su.deltanw.core.api.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public interface ExecutableArgument<T> {
  void execute(CommandContext<CommandSource> context, CommandSource source, T argument) throws CommandSyntaxException;
}
