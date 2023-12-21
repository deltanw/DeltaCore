package su.deltanw.core.impl.entity.model.factory;

import su.deltanw.core.api.entity.model.EntityModel;
import su.deltanw.core.api.entity.model.factory.AnimationHandlerFactory;
import su.deltanw.core.impl.entity.model.animation.AnimationHandlerImpl;

public class AnimationHandlerFactoryImpl implements AnimationHandlerFactory<AnimationHandlerImpl> {

  @Override
  public AnimationHandlerImpl createAnimationHandler(EntityModel model) {
    return new AnimationHandlerImpl(model);
  }

  @Override
  public AnimationHandlerImpl createAnimationHandlerWithoutDefaults(EntityModel model) {
    return new AnimationHandlerImpl(model) {

      @Override
      protected void loadDefaultAnimations() {
      }
    };
  }
}
