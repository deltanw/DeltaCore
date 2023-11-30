package su.deltanw.core.api.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import su.deltanw.core.api.commands.CommandSource;
import su.deltanw.core.impl.commands.CommandExceptions;

public class PlayerArgument implements CustomArgumentType<Player, String> {

  public static final PlayerArgument EVERYONE = new PlayerArgument((context, player) -> true);

  private BiFunction<CommandSource, Player, Boolean> allowedPlayer;

  protected PlayerArgument(BiFunction<CommandSource, Player, Boolean> allowedPlayer) {
    this.allowedPlayer = allowedPlayer;
  }

  public static PlayerArgument player(BiFunction<CommandSource, Player, Boolean> allowedPlayer) {
    return new PlayerArgument(allowedPlayer);
  }

  @Override
  public Player parse(CommandSource source, String playerName) throws CommandSyntaxException {
    Player player = Bukkit.getPlayerExact(playerName);
    if (player == null || !this.allowedPlayer.apply(source, player)) {
      throw CommandExceptions.INSTANCE.playerNotFound.create(playerName);
    }

    return player;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
    CommandSource source = (CommandSource) context.getSource();

    String remaining = builder.getRemaining();
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (this.allowedPlayer.apply(source, player)) {
        String playerName = player.getName();
        if (playerName.startsWith(remaining)) {
          builder.suggest(playerName);
        }
      }
    }

    return builder.buildFuture();
  }

  @Override
  public ArgumentType<String> toClientType() {
    return StringArgumentType.string();
  }
}
