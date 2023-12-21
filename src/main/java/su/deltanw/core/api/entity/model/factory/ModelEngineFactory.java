package su.deltanw.core.api.entity.model.factory;

import su.deltanw.core.api.entity.model.EntityModel;
import su.deltanw.core.api.entity.model.ModelEngine;
import su.deltanw.core.api.entity.model.ModelLoader;
import su.deltanw.core.api.entity.model.PlayerModel;

import java.util.function.Function;

public interface ModelEngineFactory<I> {

  ModelEngine<I> createModelEngine();

  ModelEngine<I> createModelEngine(ModelLoader loader);

  ModelEngine<I> createModelEngine(Function<ModelEngine<I>, ModelLoader> loaderConstructor);

  EntityModelFactory<? extends EntityModel, ? extends PlayerModel> createModelFactory(ModelEngine<I> engine);
}
