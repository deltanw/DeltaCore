package su.deltanw.core.api.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import su.deltanw.core.impl.commands.CommandExceptions;

public class PlayerArgument implements CustomArgumentType<Player, String> {

  private Object2BooleanFunction<Player> allowedPlayer;

  public PlayerArgument(Object2BooleanFunction<Player> allowedPlayer) {
    this.allowedPlayer = allowedPlayer;
  }

  @Override
  public Player parse(String playerName) throws CommandSyntaxException {
    Player player = Bukkit.getPlayerExact(playerName);
    if (player == null || !this.allowedPlayer.apply(player)) {
      throw CommandExceptions.INSTANCE.playerNotFound.create(playerName);
    }

    return player;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
    String remaining = builder.getRemaining();
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (this.allowedPlayer.apply(player)) {
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
