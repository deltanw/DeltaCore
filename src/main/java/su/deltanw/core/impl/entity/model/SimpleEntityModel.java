package su.deltanw.core.impl.entity.model;

import net.minecraft.world.item.ItemStack;
import su.deltanw.core.api.entity.model.ModelEngine;

public class SimpleEntityModel extends AbstractEntityModel {

  private final String id;

  public SimpleEntityModel(ModelEngine<ItemStack> modelEngine, String id) {
    super(modelEngine);
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }
}
