package su.deltanw.core.impl.entity.model.animation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import su.deltanw.core.Core;
import su.deltanw.core.api.entity.model.EntityModel;
import su.deltanw.core.api.entity.model.animation.AnimationDirection;
import su.deltanw.core.api.entity.model.animation.AnimationHandler;
import su.deltanw.core.api.entity.model.animation.AnimationType;
import su.deltanw.core.api.entity.model.animation.ModelAnimation;
import su.deltanw.core.api.entity.model.bone.ModelBone;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class AnimationHandlerImpl implements AnimationHandler {

  private final Map<String, Integer> animationTimes = new HashMap<>();
  private final Map<String, Integer> animationPriorities = new HashMap<>();
  private final Map<String, AnimationDirection> direction = new HashMap<>();
  private final LinkedHashMap<String, Set<ModelAnimation>> animations = new LinkedHashMap<>();
  private final Map<String, Consumer<Void>> callbacks = new ConcurrentHashMap<>();
  private final Map<String, Integer> callbackTimers = new ConcurrentHashMap<>();
  private final TreeMap<Integer, String> repeating = new TreeMap<>();
  private final EntityModel model;
  private final BukkitTask task;
  private String playingOnce = null;

  public AnimationHandlerImpl(EntityModel model) {
    this.model = model;
    loadDefaultAnimations();
    this.task = Bukkit.getScheduler().runTaskTimer(Core.getPlugin(Core.class), this::tick, 0, 1);
  }

  protected void loadDefaultAnimations() {
    JsonObject loadedAnimations = model.getEngine().getModelLoader().loadAnimations(model.getId());
    int i = 0;
    for (Map.Entry<String, JsonElement> animation : loadedAnimations.get("animations").getAsJsonObject().entrySet()) {
      registerAnimation(animation.getKey(), animation.getValue(), i);
      --i;
    }
  }

  @Override
  public void registerAnimation(String name, JsonElement animation, int priority) {
    JsonElement animationLength = animation.getAsJsonObject().get("animation_length");
    double length = animationLength == null ? 0 : animationLength.getAsDouble();

    Set<ModelAnimation> animationSet = new HashSet<>();
    for (Map.Entry<String, JsonElement> boneEntry : animation.getAsJsonObject().get("bones").getAsJsonObject().entrySet()) {
      String boneName = boneEntry.getKey();
      ModelBone bone = model.getPart(boneName);
      if (bone == null) {
        continue;
      }

      JsonElement animationRotation = boneEntry.getValue().getAsJsonObject().get("rotation");
      JsonElement animationPosition = boneEntry.getValue().getAsJsonObject().get("position");

      if (animationRotation != null) {
        ModelAnimation boneAnimation = new ModelAnimationImpl(model.getEngine(), model.getId(), name, bone, animationRotation, AnimationType.ROTATION, length);
        animationSet.add(boneAnimation);
      }

      if (animationPosition != null) {
        ModelAnimation boneAnimation = new ModelAnimationImpl(model.getEngine(), model.getId(), name, bone, animationPosition, AnimationType.TRANSLATION, length);
        animationSet.add(boneAnimation);
      }
    }

    animationTimes.put(name, (int) (length * 20));
    animationPriorities.put(name, priority);
    animations.put(name, animationSet);
  }

  @Override
  public void playRepeat(String animation) {
    playRepeat(animation, AnimationDirection.FORWARD);
  }

  @Override
  public void playRepeat(String animation, AnimationDirection direction) {
    Integer priority = animationPriorities().get(animation);
    if (priority == null) {
      throw new IllegalArgumentException(animation);
    }

    if (repeating.containsKey(priority) && this.direction.get(animation) == direction) {
      return;
    }

    this.direction.put(animation, direction);
    this.repeating.put(priority, animation);
    var top = repeating.firstEntry();

    if (top != null && animation.equals(top.getValue())) {
      repeating.values().forEach(v -> {
        if (!v.equals(animation)) {
          animations.get(v).forEach(ModelAnimation::stop);
        }
      });
      animations.get(animation).forEach(a -> a.setDirection(direction));
      if (playingOnce == null) {
        animations.get(animation).forEach(ModelAnimation::play);
      }
    }
  }

  @Override
  public void stopRepeat(String animation) {
    Integer priority = animationPriorities().get(animation);
    if (priority == null) {
      throw new IllegalArgumentException(animation);
    }

    animations.get(animation).forEach(ModelAnimation::stop);
    var currentTop = repeating.firstEntry();
    repeating.remove(priority);
    var firstEntry = repeating.firstEntry();
    if (playingOnce == null && firstEntry != null && currentTop != null && !firstEntry.getKey().equals(currentTop.getKey())) {
      animations.get(firstEntry.getValue()).forEach(ModelAnimation::play);
    }
  }

  @Override
  public void playOnce(String animation, Consumer<Void> callback) {
    playOnce(animation, AnimationDirection.FORWARD, callback);
  }

  @Override
  public void playOnce(String animation, AnimationDirection direction, Consumer<Void> callback) {
    if (animationPriorities().get(animation) == null) {
      throw new IllegalArgumentException(animation);
    }

    AnimationDirection currentDirection = this.direction.get(animation);
    this.direction.put(animation, direction);

    if (callbacks.containsKey(animation)) {
      callbacks.get(animation).accept(null);
    }

    int callbackTimer = callbackTimers.getOrDefault(animation, 0);

    if (animation.equals(playingOnce) && direction == AnimationDirection.PAUSE && callbackTimer > 0) {
      playingOnce = animation;
      animations.get(animation).forEach(a -> a.setDirection(direction));
      callbacks.put(animation, callback);
    } else if (animation.equals(playingOnce) && currentDirection != direction) {
      playingOnce = animation;
      animations.get(animation).forEach(a -> a.setDirection(direction));
      callbacks.put(animation, callback);
      if (currentDirection != AnimationDirection.PAUSE) {
        callbackTimers.put(animation, animationTimes.get(animation) - callbackTimer + 1);
      }
    } else if (direction != AnimationDirection.PAUSE) {
      if (playingOnce != null) {
        animations.get(playingOnce).forEach(ModelAnimation::stop);
      }

      playingOnce = animation;
      callbacks.put(animation, callback);
      callbackTimers.put(animation, animationTimes.get(animation));
      animations.get(animation).forEach(a -> a.setDirection(direction));
      animations.get(animation).forEach(ModelAnimation::play);

      repeating.values().forEach(v -> {
        if (!v.equals(animation)) {
          animations.get(v).forEach(ModelAnimation::stop);
        }
      });
    }
  }

  private void tick() {
    for (Map.Entry<String, Integer> entry : callbackTimers.entrySet()) {
      if (entry.getValue() <= 0) {
        if (playingOnce != null && playingOnce.equals(entry.getKey())) {
          Map.Entry<Integer, String> firstEntry = repeating.firstEntry();
          if (firstEntry != null) {
            animations.get(firstEntry.getValue()).forEach(ModelAnimation::play);
          }
          playingOnce = null;
        }
        Bukkit.getScheduler().runTask(Core.getPlugin(Core.class), () ->
            model.triggerAnimationEnd(entry.getKey(), direction.get(entry.getKey())));
        animations.get(entry.getKey()).forEach(ModelAnimation::stop);
        callbackTimers.remove(entry.getKey());
        Consumer<Void> callback = callbacks.remove(entry.getKey());
        if (callback != null) {
          callback.accept(null);
        }
      } else {
        if (direction.get(entry.getKey()) != AnimationDirection.PAUSE) {
          callbackTimers.put(entry.getKey(), entry.getValue() - 1);
        }
      }
    }

    if (callbacks.size() + repeating.size() == 0) {
      return;
    }

    model.display();

    animations.forEach((key, animations) -> animations.forEach(ModelAnimation::tick));
  }

  @Override
  public void destroy() {
    task.cancel();
  }

  @Override
  public String getPlaying() {
    if (playingOnce != null) {
      return playingOnce;
    }

    var playing = repeating.firstEntry();
    return playing != null ? playing.getValue() : null;
  }

  @Override
  public Map<String, Integer> animationPriorities() {
    return animationPriorities;
  }
}
