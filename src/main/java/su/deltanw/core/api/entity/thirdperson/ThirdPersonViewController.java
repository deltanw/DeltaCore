package su.deltanw.core.api.entity.thirdperson;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import su.deltanw.core.api.entity.model.PlayerModel;

public interface ThirdPersonViewController {

  boolean enterView();

  void destroyView();

  void rotate(float yaw, float pitch);

  void move(double deltaX, double deltaY, double deltaZ);

  void move(double deltaX, double deltaY, double deltaZ, float yaw, float pitch);

  void moveTo(Location to);

  Player getPlayer();

  Location getViewPoint();

  PlayerModel getModel();

  boolean isActive();
}
