package su.deltanw.core.api.util;

import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class EntityUtil {

  private static final NamespacedKey PRIVATE_ENTITY_ID_KEY = Objects.requireNonNull(NamespacedKey.fromString("deltanw:private_eid"));

  /**
   * @deprecated Use {@link net.minecraft.world.entity.Entity#nextEntityId()} whenever possible
   */
  public static int allocatePrivateEntityId(World world) {
    int current = world.getPersistentDataContainer().getOrDefault(PRIVATE_ENTITY_ID_KEY, PersistentDataType.INTEGER, -100000);
    world.getPersistentDataContainer().set(PRIVATE_ENTITY_ID_KEY, PersistentDataType.INTEGER, --current);
    return current;
  }
}
