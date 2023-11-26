package su.deltanw.core.hook.worldedit;

import com.fastasyncworldedit.core.extent.filter.block.FilterBlock;
import com.fastasyncworldedit.core.queue.Filter;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.ITileInput;
import com.fastasyncworldedit.core.registry.state.PropertyKey;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.OutputExtent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import org.jetbrains.annotations.NotNull;
import su.deltanw.core.impl.block.CustomBlock;

import java.lang.reflect.Field;

public class WorldEditCustomBlockState extends BlockState {

  private final CustomBlock customBlock;

  public WorldEditCustomBlockState(BlockType blockType, int internalId, int ordinal, CustomBlock customBlock) {
    super(blockType, internalId, ordinal);
    this.customBlock = customBlock;
  }

  public WorldEditCustomBlockState(BlockType blockType, int internalId, int ordinal, @NotNull CompoundTag tile, CustomBlock customBlock) {
    super(blockType, internalId, ordinal, tile);
    this.customBlock = customBlock;
  }

  @Override
  public BlockState withPropertyId(int propertyId) {
    return WorldEditHook.injectBlockState(super.withPropertyId(propertyId), customBlock);
  }

  @Override
  public BaseBlock applyBlock(BlockVector3 position) {
    return WorldEditHook.injectBaseBlock(super.applyBlock(position), customBlock);
  }

  @Override
  public <V> BlockState with(Property<V> property, V value) {
    return WorldEditHook.injectBlockState(super.with(property, value), customBlock);
  }

  @Override
  public <V> BlockState with(PropertyKey property, V value) {
    return WorldEditHook.injectBlockState(super.with(property, value), customBlock);
  }

  @Override
  public <V> BlockState withProperties(BlockState other) {
    return WorldEditHook.injectBlockState(super.withProperties(other), customBlock);
  }

  @Override
  public BlockState toImmutableState() {
    return WorldEditHook.injectBlockState(super.toImmutableState(), customBlock);
  }

  @Override
  public BaseBlock toBaseBlock() {
    return WorldEditHook.injectBaseBlock(super.toBaseBlock(), customBlock);
  }

  @Override
  public BaseBlock toBaseBlock(LazyReference<CompoundBinaryTag> compoundTag) {
    return WorldEditHook.injectBaseBlock(super.toBaseBlock(compoundTag), customBlock);
  }

  @Override
  public BaseBlock toBaseBlock(ITileInput input, int x, int y, int z) {
    return WorldEditHook.injectBaseBlock(super.toBaseBlock(input, x, y, z), customBlock);
  }

  @Override
  public BaseBlock toBaseBlock(CompoundTag compoundTag) {
    return WorldEditHook.injectBaseBlock(super.toBaseBlock(compoundTag), customBlock);
  }

  @Override
  public BaseBlock toBaseBlock(CompoundBinaryTag compoundTag) {
    return WorldEditHook.injectBaseBlock(super.toBaseBlock(compoundTag), customBlock);
  }

  @Override
  public BaseBlock apply(BlockVector3 position) {
    return WorldEditHook.injectBaseBlock(super.apply(position), customBlock);
  }

  public CustomBlock getCustomBlock() {
    return customBlock;
  }
}
