package su.deltanw.core.impl.entity.thirdperson;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import su.deltanw.core.api.entity.model.factory.EntityModelFactory;
import su.deltanw.core.api.entity.model.factory.ModelEngineFactory;
import su.deltanw.core.api.entity.thirdperson.ThirdPersonPointViewController;
import su.deltanw.core.api.entity.thirdperson.ThirdPersonViewController;
import su.deltanw.core.api.entity.thirdperson.ThirdPersonViewFactory;

public class ThirdPersonViewFactoryImpl implements ThirdPersonViewFactory {

  private final ThirdPersonNettyHandler nettyHandler;

  public ThirdPersonViewFactoryImpl(ThirdPersonNettyHandler nettyHandler) {
    this.nettyHandler = nettyHandler;
  }

  @Override
  public ThirdPersonViewController fixedView(EntityModelFactory<?, ?> modelFactory, Player player, Location viewPoint) {
    return new ThirdPersonViewControllerImpl(modelFactory, nettyHandler, player, viewPoint);
  }

  @Override
  public ThirdPersonPointViewController steerableTargetedView(EntityModelFactory<?, ?> modelFactory, Player player, Location viewPoint, Location target) {
    return new SteerableThirdPersonPointViewControllerImpl(modelFactory, nettyHandler, player, viewPoint, target);
  }
}
