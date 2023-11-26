package su.deltanw.core.api.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public interface ExecutableCommand {
  void execute(CommandContext<CommandSource> context, CommandSource source) throws CommandSyntaxException;
}
