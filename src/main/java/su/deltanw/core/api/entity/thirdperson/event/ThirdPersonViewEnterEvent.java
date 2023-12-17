package su.deltanw.core.api.entity.thirdperson.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.deltanw.core.impl.entity.thirdperson.ThirdPersonViewController;

public class ThirdPersonViewEnterEvent extends Event implements Cancellable {

  private static final HandlerList HANDLERS = new HandlerList();

  private final ThirdPersonViewController controller;
  private boolean cancelled = false;

  public ThirdPersonViewEnterEvent(ThirdPersonViewController controller) {
    this.controller = controller;
  }

  public ThirdPersonViewController getController() {
    return controller;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancel) {
    cancelled = cancel;
  }
}
