package su.deltanw.core.api.entity.thirdperson;

import org.bukkit.Location;

public interface ThirdPersonPointViewController extends ThirdPersonViewController {

  void calculateDirection();

  void setTarget(Location target);
}
