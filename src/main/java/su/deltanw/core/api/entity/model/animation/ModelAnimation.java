package su.deltanw.core.api.entity.model.animation;

import org.bukkit.util.Vector;

import java.util.LinkedHashMap;
import java.util.Map;

public interface ModelAnimation {

  AnimationType getType();

  boolean isPlaying();

  void tick();

  Vector calculateTransform(int tick, LinkedHashMap<Double, PointInterpolation> transform);

  Vector getTransform();

  Vector getTransformAtTime(int time);

  void setDirection(AnimationDirection direction);

  Map<Short, Vector> calculateAllTransforms(double animationTime, LinkedHashMap<Double, PointInterpolation> t);

  void stop();

  void play();

  String name();
}
