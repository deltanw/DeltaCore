package su.deltanw.core.impl.syncher;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.protocol.game.ClientboundPingPacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import su.deltanw.core.Core;

public class PingSyncherNettyHandler extends ChannelInboundHandlerAdapter {

  private final Map<Integer, Runnable> pendingTasks = new ConcurrentHashMap<>();
  private final Core plugin;
  private long requestTime, responseTime;

  public PingSyncherNettyHandler(Core plugin) {
    this.plugin = plugin;
  }

  public boolean addTask(int id, Runnable runnable) {
    return this.pendingTasks.putIfAbsent(id, runnable) == null;
  }

  public boolean shouldDisconnect() {
    long time = System.currentTimeMillis();
    if (this.requestTime != 0) {
      long delay = time - this.requestTime;
      if (this.responseTime != 0) {
        delay -= time - this.responseTime;
      }

      // Unresponsive for 30s
      if (delay > 30_000L) {
        return true;
      }
    }

    this.requestTime = time;
    return false;
  }

  @Override
  public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
    // Check if ServerboundPongPacket is using id from 'pendingTasks'
    if (msg instanceof ServerboundPongPacket pong
        && this.pendingTasks.containsKey(pong.getId())) {
      this.responseTime = System.currentTimeMillis();
      this.runTasks(pong.getId());
      return;
    }

    ctx.fireChannelRead(msg);
  }

  // Run and remove all tasks until required task was not found.
  private void runTasks(int id) {
    Iterator<Entry<Integer, Runnable>> iterator = this.pendingTasks.entrySet().iterator();
    while (iterator.hasNext()) {
      Entry<Integer, Runnable> entry = iterator.next();
      iterator.remove();

      try {
        Bukkit.getScheduler().runTask(this.plugin, entry.getValue());
      } catch (Throwable throwable) {
        throwable.printStackTrace();
      }

      if (entry.getKey() == id) {
        break;
      }
    }
  }
}
