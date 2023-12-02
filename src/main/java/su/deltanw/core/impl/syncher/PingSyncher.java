package su.deltanw.core.impl.syncher;

import io.netty.channel.Channel;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundPingPacket;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import su.deltanw.core.Core;
import su.deltanw.core.api.syncher.ClientSyncher;

public class PingSyncher implements ClientSyncher {

  // These ids most likely will not intersect with anticheats
  private static final int MIN_ID = 120_000_000;
  private static final int MAX_ID = 140_000_000;

  private long seed = System.nanoTime();

  @Override
  public void waitForClient(Player player, Runnable runnable) {
    Channel channel = ((CraftPlayer) player).getHandle().connection.connection.channel;
    PingSyncherNettyHandler syncher = channel.pipeline().get(PingSyncherNettyHandler.class);

    // Client was unresponsive for too long
    if (syncher.shouldDisconnect()) {
      JavaPlugin.getPlugin(Core.class).getLogger().warning(
        player.getName() + " was not responding for too long");
      player.kick(Component.translatable("disconnect.timeout"));
      return;
    }

    // Randomize to make sure that cheats can't guess further ids
    int id;
    do {
      id = this.nextId();
    } while (!syncher.addTask(id, runnable));

    // Already know that player can receive packets from PLAY state
    // Also it's wakes eventloop to send this packet (and queued packets) immediately
    channel.writeAndFlush(new ClientboundPingPacket(id));
  }

  // Modified version of Random::nextInt
  private int nextId() {
    long newSeed;
    this.seed = newSeed = (this.seed * 25214903917L + 11L) & 0xFFFFFFFFFFFFL;
    return MIN_ID + (int) ((((int) (newSeed >>> 16)) & 0xFFFFFFFFL) * (MAX_ID - MIN_ID) >>> 32);
  }
}
