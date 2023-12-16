package su.deltanw.core.impl.entity.model.factory;

import net.minecraft.world.item.ItemStack;
import su.deltanw.core.api.entity.model.ModelEngine;
import su.deltanw.core.api.entity.model.ModelLoader;
import su.deltanw.core.api.entity.model.factory.EntityModelFactory;
import su.deltanw.core.api.entity.model.factory.ModelEngineFactory;
import su.deltanw.core.impl.entity.model.AbstractEntityModel;
import su.deltanw.core.impl.entity.model.DefaultModelEngine;
import su.deltanw.core.impl.entity.model.concrete.PlayerModelImpl;

import java.util.function.Function;

public class ModelEngineFactoryImpl implements ModelEngineFactory<ItemStack> {

  @Override
  public ModelEngine<ItemStack> createModelEngine() {
    return new DefaultModelEngine();
  }

  @Override
  public ModelEngine<ItemStack> createModelEngine(ModelLoader loader) {
    return new DefaultModelEngine(loader);
  }

  @Override
  public ModelEngine<ItemStack> createModelEngine(Function<ModelEngine<ItemStack>, ModelLoader> loaderConstructor) {
    return new DefaultModelEngine(loaderConstructor);
  }

  @Override
  public EntityModelFactory<AbstractEntityModel, PlayerModelImpl> createModelFactory(ModelEngine<ItemStack> engine) {
    return new EntityModelFactoryImpl(engine);
  }
}
