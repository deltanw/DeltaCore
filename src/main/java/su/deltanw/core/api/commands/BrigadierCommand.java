package su.deltanw.core.api.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import su.deltanw.core.api.commands.builder.CommandBuilder;

public abstract class BrigadierCommand extends CommandBuilder implements Predicate<CommandSource> {

  private final List<String> aliases = new ArrayList<>();
  private String permission;

  public BrigadierCommand(String name) {
    super(name);

    this.requires(this);
  }

  public List<String> getAliases() {
    return this.aliases;
  }

  public BrigadierCommand addAliases(String... aliases) {
    this.aliases.addAll(List.of(aliases));
    return this;
  }

  public String getPermission() {
    return this.permission;
  }

  public void setPermission(String permission) {
    this.permission = permission;
  }

  @Override
  public boolean test(CommandSource ctx) {
    return this.getPermission() == null || ctx.hasPermission(this.getPermission());
  }
}
