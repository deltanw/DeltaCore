package su.deltanw.core.api.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public interface ExecutableCommand extends Command<CommandSource> {
  @Override
  default int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
    this.execute(context, context.getSource());
    return 0;
  }

  void execute(CommandContext<CommandSource> context, CommandSource source) throws CommandSyntaxException;
}
