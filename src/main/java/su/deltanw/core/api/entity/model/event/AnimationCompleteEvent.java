package su.deltanw.core.api.entity.model.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.deltanw.core.api.entity.model.EntityModel;
import su.deltanw.core.api.entity.model.animation.AnimationDirection;

public class AnimationCompleteEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();

  private final EntityModel model;
  private final String animation;
  private final AnimationDirection direction;

  public AnimationCompleteEvent(EntityModel model, String animation, AnimationDirection direction) {
    this.model = model;
    this.animation = animation;
    this.direction = direction;
  }

  public EntityModel getModel() {
    return model;
  }

  public String getAnimation() {
    return animation;
  }

  public AnimationDirection getDirection() {
    return direction;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }
}
