package su.deltanw.core.hook.worldedit;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import su.deltanw.core.impl.block.CustomBlock;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WorldEditListener implements Listener {

  @EventHandler
  public void onTabComplete(AsyncTabCompleteEvent event) {
    List<String> args = Arrays.stream(event.getBuffer().split(" ")).toList();
    if (!event.getBuffer().startsWith("//") || args.isEmpty()) {
      return;
    }

    List<String> ids = CustomBlock.getAll().stream()
        .map(CustomBlock::key).map(NamespacedKey::asString)
        .filter(id -> id.startsWith(args.get(args.size() - 1)))
        .collect(Collectors.toList());
    ids.addAll(event.getCompletions());
    event.setCompletions(ids);
  }
}
