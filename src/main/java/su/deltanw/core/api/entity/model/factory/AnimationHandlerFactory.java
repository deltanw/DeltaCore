package su.deltanw.core.api.entity.model.factory;

import su.deltanw.core.api.entity.model.EntityModel;
import su.deltanw.core.api.entity.model.animation.AnimationHandler;

public interface AnimationHandlerFactory<T extends AnimationHandler> {

  T createAnimationHandler(EntityModel model);
}
