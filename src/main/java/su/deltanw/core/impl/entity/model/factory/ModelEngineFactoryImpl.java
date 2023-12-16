package su.deltanw.core.impl.entity.model.factory;

import net.minecraft.world.item.ItemStack;
import su.deltanw.core.api.entity.model.ModelEngine;
import su.deltanw.core.api.entity.model.ModelLoader;
import su.deltanw.core.api.entity.model.factory.EntityModelFactory;
import su.deltanw.core.api.entity.model.factory.ModelEngineFactory;
import su.deltanw.core.impl.entity.model.AbstractEntityModel;
import su.deltanw.core.impl.entity.model.ModelEngineImpl;
import su.deltanw.core.impl.entity.model.PlayerModelImpl;

import java.util.function.Function;

public class ModelEngineFactoryImpl implements ModelEngineFactory<ItemStack> {

  @Override
  public ModelEngine<ItemStack> createModelEngine() {
    return new ModelEngineImpl();
  }

  @Override
  public ModelEngine<ItemStack> createModelEngine(ModelLoader loader) {
    return new ModelEngineImpl(loader);
  }

  @Override
  public ModelEngine<ItemStack> createModelEngine(Function<ModelEngine<ItemStack>, ModelLoader> loaderConstructor) {
    return new ModelEngineImpl(loaderConstructor);
  }

  @Override
  public EntityModelFactory<AbstractEntityModel, PlayerModelImpl> createModelFactory(ModelEngine<ItemStack> engine) {
    return new EntityModelFactoryImpl(engine);
  }
}
