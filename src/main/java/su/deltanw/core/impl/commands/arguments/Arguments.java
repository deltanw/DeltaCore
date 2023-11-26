package su.deltanw.core.impl.commands.arguments;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.List;
import java.util.stream.Stream;
import org.bukkit.NamespacedKey;
import su.deltanw.core.impl.commands.CommandSource;

public class Arguments {
  public static LiteralArgumentBuilder<CommandSource> literal(String literal) {
    return LiteralArgumentBuilder.literal(literal);
  }

  public static <T> RequiredArgumentBuilder<CommandSource, T> argument(String name, ArgumentType<T> type) {
    return RequiredArgumentBuilder.argument(name, type);
  }

  public static LiteralArgumentBuilder<CommandSource> namespace(String name, Stream<NamespacedKey> keys, Command<CommandSource> command) {
    List<String> values = keys.map(NamespacedKey::toString).toList();

    return literal(name).then(argument(name, StringArgumentType.greedyString()).suggests(
        (context, builder) -> {
          String remaining = builder.getRemaining();
          for (String key : values) {
            if (key.startsWith(remaining)) {
              builder.suggest(key);
            }
          }
          return builder.buildFuture();
        }
    ).executes(command));
  }
}
