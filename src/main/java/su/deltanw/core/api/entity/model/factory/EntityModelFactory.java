package su.deltanw.core.api.entity.model.factory;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import su.deltanw.core.api.entity.model.EntityModel;
import su.deltanw.core.api.entity.model.PlayerModel;

public interface EntityModelFactory<T extends EntityModel, P extends PlayerModel> {

  /**
   * Creates an entity model base for overlaying.
   * @return New entity model base.
   */
  T createEntityModelBase();

  T createEntityModel(String id);

  P createPlayerModel(PlayerProfile profile);

  P createPlayerModel(PlayerProfile profile, Component nameTag);

  P createPlayerModel(Player player, boolean showNameTag);

  default P createPlayerModel(Player player) {
    return createPlayerModel(player, true);
  }
}
