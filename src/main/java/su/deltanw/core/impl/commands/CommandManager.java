package su.deltanw.core.impl.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class CommandManager {
  private CommandDispatcher<PlayerSource> dispatcher = new CommandDispatcher<>();

  public void register(BrigadierCommand command) {
    for (String name : command.getNames()) {
      LiteralArgumentBuilder<PlayerSource> builder =
          LiteralArgumentBuilder.<PlayerSource>literal(name).requires(command);

      command.buildCommand(builder);

      this.dispatcher.register(builder);
    }
  }

  public CommandDispatcher<PlayerSource> getDispatcher() {
    return this.dispatcher;
  }
}
