package su.deltanw.core.impl.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.function.Predicate;

public abstract class BrigadierCommand implements Predicate<PlayerSource> {
  private String[] names;
  private String permission;

  public void setNames(String... names) {
    this.names = names;
  }

  public String[] getNames() {
    return this.names;
  }

  public String getPermission() {
    return this.permission;
  }

  public void setPermission(String permission) {
    this.permission = permission;
  }

  public abstract void buildCommand(LiteralArgumentBuilder<PlayerSource> builder);

  @Override
  public boolean test(PlayerSource ctx) {
    return this.getPermission() == null
           || ctx.hasPermission(this.getPermission());
  }
}
