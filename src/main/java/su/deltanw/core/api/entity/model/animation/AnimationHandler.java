package su.deltanw.core.api.entity.model.animation;

import com.google.gson.JsonElement;

import java.util.Map;
import java.util.function.Consumer;

public interface AnimationHandler {

  void registerAnimation(String name, JsonElement animation, int priority);

  void playRepeat(String animation);

  void playRepeat(String animation, AnimationDirection direction);

  void stopRepeat(String animation);

  void playOnce(String animation, Consumer<Void> callback);

  void playOnce(String animation, AnimationDirection direction, Consumer<Void> callback);

  void destroy();

  String getPlaying();

  Map<String, Integer> animationPriorities();
}
