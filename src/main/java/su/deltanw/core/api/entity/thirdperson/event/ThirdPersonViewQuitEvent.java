package su.deltanw.core.api.entity.thirdperson.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.deltanw.core.api.entity.thirdperson.ThirdPersonViewController;

public class ThirdPersonViewQuitEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();

  private final ThirdPersonViewController controller;

  public ThirdPersonViewQuitEvent(ThirdPersonViewController controller) {
    this.controller = controller;
  }

  public ThirdPersonViewController getController() {
    return controller;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }
}
