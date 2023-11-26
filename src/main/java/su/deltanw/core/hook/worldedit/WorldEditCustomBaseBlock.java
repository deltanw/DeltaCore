package su.deltanw.core.hook.worldedit;

import com.fastasyncworldedit.core.queue.ITileInput;
import com.fastasyncworldedit.core.registry.state.PropertyKey;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import su.deltanw.core.impl.block.CustomBlock;

import java.util.Map;

public class WorldEditCustomBaseBlock extends BaseBlock {

  private final CustomBlock customBlock;

  protected WorldEditCustomBaseBlock(BlockState blockState, CustomBlock customBlock) {
    super(WorldEditHook.injectBlockState(blockState, customBlock),
        new CompoundTag(Map.of("delta__custom_block", new StringTag(customBlock.key().asString()))));
    this.customBlock = customBlock;
  }

  protected WorldEditCustomBaseBlock(BlockState blockState, CompoundTag nbt, CustomBlock customBlock) {
    super(WorldEditHook.injectBlockState(blockState, customBlock), nbt.createBuilder().putString("delta__custom_block", customBlock.key().asString()).build());
    this.customBlock = customBlock;
  }

  protected WorldEditCustomBaseBlock(BlockState blockState, LazyReference<CompoundBinaryTag> nbt, CustomBlock customBlock) {
    super(WorldEditHook.injectBlockState(blockState, customBlock), nbt);
    nbt.getValue().putString("delta__custom_block", customBlock.key().asString());
    this.customBlock = customBlock;
  }

  @Override
  public <V> BaseBlock with(Property<V> property, V value) {
    return WorldEditHook.injectBaseBlock(super.with(property, value), customBlock);
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
  public BaseBlock withPropertyId(int propertyId) {
    return WorldEditHook.injectBaseBlock(super.withPropertyId(propertyId), customBlock);
  }

  @Override
  public <V> BaseBlock with(PropertyKey property, V value) {
    return WorldEditHook.injectBaseBlock(super.with(property, value), customBlock);
  }

  @Override
  public BlockState toBlockState() {
    return WorldEditHook.injectBlockState(super.toBlockState(), customBlock);
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
  public BaseBlock applyBlock(BlockVector3 position) {
    return WorldEditHook.injectBaseBlock(super.applyBlock(position), customBlock);
  }

  @Override
  public BaseBlock toBaseBlock(ITileInput input, int x, int y, int z) {
    return WorldEditHook.injectBaseBlock(super.toBaseBlock(input, x, y, z), customBlock);
  }

  @Override
  public BaseBlock apply(BlockVector3 position) {
    return WorldEditHook.injectBaseBlock(super.apply(position), customBlock);
  }

  public CustomBlock getCustomBlock() {
    return customBlock;
  }
}
