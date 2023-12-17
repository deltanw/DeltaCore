package su.deltanw.core.api.entity.thirdperson.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.deltanw.core.api.entity.thirdperson.ThirdPersonViewController;
import su.deltanw.core.api.entity.thirdperson.callback.ThirdPersonPlayerCommand;

public class ThirdPersonPlayerCommandEvent extends Event implements Cancellable {

  private static final HandlerList HANDLERS = new HandlerList();

  private final ThirdPersonViewController controller;
  private final ThirdPersonPlayerCommand command;
  private boolean cancelled = false;

  public ThirdPersonPlayerCommandEvent(ThirdPersonViewController controller, ThirdPersonPlayerCommand command) {
    this.controller = controller;
    this.command = command;
  }

  public ThirdPersonViewController getController() {
    return controller;
  }

  public ThirdPersonPlayerCommand getCommand() {
    return command;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancel) {
    cancelled = cancel;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }
}
