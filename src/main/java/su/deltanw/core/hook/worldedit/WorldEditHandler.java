package su.deltanw.core.hook.worldedit;

import com.jeff_media.customblockdata.CustomBlockData;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataType;
import su.deltanw.core.Core;
import su.deltanw.core.impl.block.CustomBlock;

import java.util.Arrays;
import java.util.Objects;

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

      private CustomBlock getCustomBlock(int x, int y, int z) {
        Block block = world.getBlockAt(x, y, z);
        CustomBlockData blockData = new CustomBlockData(block, plugin);
        String key = blockData.get(Objects.requireNonNull(NamespacedKey.fromString("deltanw:custom_block")), PersistentDataType.STRING);
        if (key != null) {
          NamespacedKey namespacedKey = NamespacedKey.fromString(key);
          return CustomBlock.get(namespacedKey);
        } else {
          return null;
        }
      }

      private BlockState injectBlockState(BlockState state, int x, int y, int z) {
        if (state instanceof WorldEditCustomBlockState) {
          return state;
        }

        CustomBlock customBlock = getCustomBlock(x, y, z);
        if (customBlock != null) {
          return WorldEditHook.injectBlockState(state, customBlock);
        } else {
          return state;
        }
      }

      private BaseBlock injectBaseBlock(BaseBlock block, int x, int y, int z) {
        if (block instanceof WorldEditCustomBaseBlock) {
          return block;
        }

        CustomBlock customBlock = getCustomBlock(x, y, z);
        if (customBlock != null) {
          return WorldEditHook.injectBaseBlock(block, customBlock);
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
        CustomBlock customBlock = null;
        if (block instanceof WorldEditCustomBlockState customBlockState) {
          customBlock = customBlockState.getCustomBlock();
        }
        if (customBlock == null && block.toBaseBlock() instanceof WorldEditCustomBaseBlock customBaseBlock) {
          customBlock = customBaseBlock.getCustomBlock();
        }
        if (customBlock == null) {
          BaseBlock baseBlock = block.toBaseBlock();
          CompoundBinaryTag binaryTag = baseBlock.getNbt();
          if (binaryTag != null) {
            String customBlockKey = binaryTag.getString("delta__custom_block");
            if (!customBlockKey.isEmpty()) {
              customBlock = CustomBlock.get(NamespacedKey.fromString(customBlockKey));
            }
          }
        }
        Location loc = new Location(world, x, y, z);
        if (customBlock != null) {
          CustomBlock targetCustomBlock = customBlock;
          // TODO: Does PDC support asynchronous operations? Can we just add block to PDC without placing it?
          Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> targetCustomBlock.place(plugin, loc));
        } else {
          CustomBlockData blockData = new CustomBlockData(loc.getBlock(), plugin);
          blockData.remove(Objects.requireNonNull(NamespacedKey.fromString("deltanw:custom_block")));
        }

        return getExtent().setBlock(x, y, z, block);
      }

      @Override
      public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
        return this.setBlock(location.getX(), location.getY(), location.getZ(), block);
      }

      @Override
      public boolean setTile(int x, int y, int z, CompoundTag tile) throws WorldEditException {
        String customBlockKey = tile.getString("delta__custom_block");
        if (!customBlockKey.isEmpty()) {
          CustomBlock customBlock = CustomBlock.get(NamespacedKey.fromString(customBlockKey));
          Location loc = new Location(world, x, y, z);
          Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> customBlock.place(plugin, loc));
        }
        return super.setTile(x, y, z, tile);
      }
    });
  }

}
