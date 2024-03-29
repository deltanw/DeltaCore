package su.deltanw.core.hook.worldedit;

import com.jeff_media.customblockdata.CustomBlockData;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import su.deltanw.core.Core;
import su.deltanw.core.impl.block.CustomBlock;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class WorldEditHandler {

  private final Core plugin;

  public WorldEditHandler(Core plugin) {
    this.plugin = plugin;
  }

  @Subscribe
  public void onEditSession(EditSessionEvent event) {
    if (event.getWorld() == null) {
      return;
    }

    World world = Bukkit.getWorld(event.getWorld().getName());
    if (world == null) {
      return;
    }

    event.setExtent(new AbstractDelegateExtent(event.getExtent()) {

      private final Queue<Pair<Location, CustomBlock>> customBlockQueue = new ConcurrentLinkedQueue<>();
      private final Queue<Location> removeBlockQueue = new ConcurrentLinkedQueue<>();

      private CustomBlock getCustomBlock(int x, int y, int z) {
        Block block = world.getBlockAt(x, y, z);
        CustomBlockData blockData = new CustomBlockData(block, plugin);
        String key = blockData.get(CustomBlock.BLOCK_PDC_KEY, PersistentDataType.STRING);
        if (key != null) {
          NamespacedKey namespacedKey = NamespacedKey.fromString(key);
          return CustomBlock.get(namespacedKey);
        } else {
          return null;
        }
      }

      private BlockState injectBlockState(BlockState state, int x, int y, int z) {
        CustomBlock customBlock = getCustomBlock(x, y, z);
        if (customBlock != null) {
          return BukkitAdapter.adapt(customBlock.clientsideBlock().createCraftBlockData());
        } else {
          return state;
        }
      }

      private BaseBlock injectBaseBlock(BaseBlock block, int x, int y, int z) {
        CustomBlock customBlock = getCustomBlock(x, y, z);
        if (customBlock != null) {
          if (block.getNbtReference() != null) {
            return BukkitAdapter.adapt(customBlock.clientsideBlock().createCraftBlockData()).toBaseBlock(block.getNbtReference());
          } else {
            return BukkitAdapter.adapt(customBlock.clientsideBlock().createCraftBlockData()).toBaseBlock();
          }
        } else {
          return block;
        }
      }

      public BlockState getBlock(BlockVector3 position) {
        return injectBlockState(getExtent().getBlock(position), position.getX(), position.getY(), position.getZ());
      }

      @Override
      public BlockState getBlock(int x, int y, int z) {
        return injectBlockState(getExtent().getBlock(x, y, z), x, y, z);
      }

      @Override
      public BaseBlock getFullBlock(BlockVector3 position) {
        return injectBaseBlock(getExtent().getFullBlock(position), position.getX(), position.getY(), position.getZ());
      }

      @Override
      public BaseBlock getFullBlock(int x, int y, int z) {
        return injectBaseBlock(getExtent().getFullBlock(x, y, z), x, y, z);
      }

      @Override
      public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
        CustomBlock customBlock = CustomBlock.getByState(block.getAsString());
        Location loc = new Location(world, x, y, z);
        if (customBlock != null) {
          customBlockQueue.add(Pair.of(loc, customBlock));
        } else {
          removeBlockQueue.add(loc);
        }

        return getExtent().setBlock(x, y, z, block);
      }

      @Override
      public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
        return this.setBlock(location.getX(), location.getY(), location.getZ(), block);
      }

      private <T> void processQueue(Queue<T> queue, Consumer<T> consumer) {
        for (int i = 0; i < 16 /* TODO: Make this value configurable? */; i++) {
          T value = queue.poll();
          if (value == null) {
            return;
          }

          consumer.accept(value);
        }

        if (!queue.isEmpty()) {
          Bukkit.getScheduler().runTask(plugin, () -> processQueue(queue, consumer));
        }
      }

      private void processRemoveQueue() {
        processQueue(removeBlockQueue, location ->
          new CustomBlockData(location.getBlock(), plugin).remove(CustomBlock.BLOCK_PDC_KEY));
      }

      private void processCustomBlockQueue() {
        processQueue(customBlockQueue, blockData -> blockData.right().place(plugin, blockData.left()));
      }

      @Nullable
      @Override
      public Operation commit() {
        Bukkit.getScheduler().runTask(plugin, () -> {
          processRemoveQueue();
          processCustomBlockQueue();
        });
        return super.commit();
      }
    });
  }

}
