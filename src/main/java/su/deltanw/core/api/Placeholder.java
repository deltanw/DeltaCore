package su.deltanw.core.api;

import java.util.Objects;
import java.util.function.BiFunction;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;

public class Placeholder {

  private final String enclosing;
  private final String name;
  private final BiFunction<String, OfflinePlayer, Component> replacement;

  public Placeholder(String enclosing, String name, BiFunction<String, OfflinePlayer, Component> replacement) {
    this.enclosing = enclosing;
    this.name = name;
    this.replacement = replacement;
  }

  public Placeholder(String name, BiFunction<String, OfflinePlayer, Component> replacement) {
    this.enclosing = "%";
    this.name = name;
    this.replacement = replacement;
  }

  public String getEnclosing() {
    return enclosing;
  }

  public String getName() {
    return name;
  }

  public BiFunction<String, OfflinePlayer, Component> getReplacement() {
    return replacement;
  }

  @Override
  public String toString() {
    return enclosing + name + enclosing;
  }

  @Override
  public int hashCode() {
    return Objects.hash(enclosing, name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Placeholder that = (Placeholder) o;
    return Objects.equals(enclosing, that.enclosing) && Objects.equals(name, that.name);
  }
}
