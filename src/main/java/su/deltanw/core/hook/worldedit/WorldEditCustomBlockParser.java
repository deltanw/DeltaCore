package su.deltanw.core.hook.worldedit;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import su.deltanw.core.impl.block.CustomBlock;

public class WorldEditCustomBlockParser extends InputParser<BaseBlock> {

  public WorldEditCustomBlockParser() {
    super(WorldEdit.getInstance());
  }

  @Override
  public BaseBlock parseFromInput(String input, ParserContext context) throws InputParseException {
    if (input.equals("minecraft:note_block") || input.equals("note_block")) {
      return BukkitAdapter.adapt(Bukkit.createBlockData(Material.NOTE_BLOCK)).toBaseBlock();
    } else if (input.equals("minecraft:tripwire") || input.equals("tripwire")) {
      return BukkitAdapter.adapt(Bukkit.createBlockData(Material.TRIPWIRE)).toBaseBlock();
    }

    if (!input.startsWith("deltanw:") || input.endsWith(":")) {
      return null;
    }

    String id = input.split(":")[1].split("\\[")[0];
    CustomBlock customBlock = CustomBlock.get(new NamespacedKey("deltanw", id));
    if (id.equals(input) || customBlock == null) {
      return null;
    }

    return new WorldEditCustomBaseBlock(BukkitAdapter.adapt(customBlock.serversideBlock().createCraftBlockData()), customBlock);
  }
}
