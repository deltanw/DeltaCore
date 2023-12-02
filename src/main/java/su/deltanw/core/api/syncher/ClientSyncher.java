package su.deltanw.core.api.syncher;

import org.bukkit.entity.Player;

public interface ClientSyncher {

  /**
   * Wait for client to confirm that previous packets was received and processed
   *
   * @param player synchronization target
   * @param runnable task to run after synchronization
   */
  void waitForClient(Player player, Runnable runnable);
}
