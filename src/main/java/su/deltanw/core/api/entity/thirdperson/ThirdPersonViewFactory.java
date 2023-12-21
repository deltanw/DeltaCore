package su.deltanw.core.api.entity.thirdperson;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import su.deltanw.core.api.entity.model.factory.EntityModelFactory;

public interface ThirdPersonViewFactory {

  ThirdPersonViewController fixedView(EntityModelFactory<?, ?> modelFactory, Player player, Location viewPoint);

  ThirdPersonPointViewController steerableTargetedView(EntityModelFactory<?, ?> modelFactory, Player player, Location viewPoint, Location target);
}
