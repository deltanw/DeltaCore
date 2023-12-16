package su.deltanw.core.api.entity.model;

import com.destroystokyo.paper.profile.PlayerProfile;

public interface PlayerModel extends EntityModel {

  PlayerProfile getProfile();

  // TODO
  // void setProfile(PlayerProfile profile);
}
