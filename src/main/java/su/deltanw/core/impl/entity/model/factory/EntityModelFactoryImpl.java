package su.deltanw.core.impl.entity.model.factory;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.minecraft.world.item.ItemStack;
import org.bukkit.entity.Player;
import su.deltanw.core.api.entity.model.ModelEngine;
import su.deltanw.core.api.entity.model.factory.EntityModelFactory;
import su.deltanw.core.impl.entity.model.AbstractEntityModel;
import su.deltanw.core.impl.entity.model.concrete.PlayerModelImpl;
import su.deltanw.core.impl.entity.model.concrete.SimpleEntityModel;

public record EntityModelFactoryImpl(ModelEngine<ItemStack> modelEngine) implements EntityModelFactory<AbstractEntityModel, PlayerModelImpl> {

  @Override
  public AbstractEntityModel createEntityModelBase() {
    return new AbstractEntityModel(modelEngine) {

      @Override
      public String getId() {
        throw new IllegalStateException();
      }
    };
  }

  @Override
  public AbstractEntityModel createEntityModel(String id) {
    return new SimpleEntityModel(modelEngine, id);
  }

  @Override
  public PlayerModelImpl createPlayerModel(PlayerProfile profile) {
    return new PlayerModelImpl(modelEngine, profile, null);
  }

  @Override
  public PlayerModelImpl createPlayerModel(PlayerProfile profile, Component nameTag) {
    return new PlayerModelImpl(modelEngine, profile, nameTag);
  }

  @Override
  public PlayerModelImpl createPlayerModel(Player player, boolean showNameTag) {
    return new PlayerModelImpl(modelEngine, player.getPlayerProfile(), showNameTag ? player.playerListName() : null);
  }
}
