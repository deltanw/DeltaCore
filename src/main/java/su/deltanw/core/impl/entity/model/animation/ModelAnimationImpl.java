package su.deltanw.core.impl.entity.model.animation;

import com.google.gson.JsonElement;
import org.bukkit.util.Vector;
import su.deltanw.core.api.entity.model.ModelEngine;
import su.deltanw.core.api.entity.model.animation.AnimationDirection;
import su.deltanw.core.api.entity.model.animation.AnimationType;
import su.deltanw.core.api.entity.model.animation.ModelAnimation;
import su.deltanw.core.api.entity.model.animation.PointInterpolation;
import su.deltanw.core.api.entity.model.bone.ModelBone;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModelAnimationImpl implements ModelAnimation {

  private static final Vector ROTATION_MUL = new Vector(-1, -1, 1);
  private static final Vector TRANSLATION_MUL = new Vector(-1, 1, 1);

  private final AnimationType type;
  private final Map<Short, Vector> interpolationCache;
  private final int length;
  private final String name;
  private AnimationDirection direction = AnimationDirection.FORWARD;
  private boolean playing = false;
  private short tick = 0;

  public ModelAnimationImpl(ModelEngine<?> engine, String modelName, String animationName, ModelBone bone,
                            JsonElement keyframes, AnimationType animationType, double length) {
    this.type = animationType;
    this.length = (int) (length * 20);
    this.name = animationName;
    Map<Short, Vector> cache = null;
    if (type == AnimationType.ROTATION) {
      cache = engine.getModelLoader().getCacheRotation(modelName, bone.getName() + "/" + animationName);
    } else if (type == AnimationType.TRANSLATION) {
      cache = engine.getModelLoader().getCacheTranslation(modelName, bone.getName() + "/" + animationName);
    }

    if (cache == null) {
      LinkedHashMap<Double, PointInterpolation> transform = new LinkedHashMap<>();

      try {
        for (Map.Entry<String, JsonElement> entry : keyframes.getAsJsonObject().entrySet()) {
          try {
            double time = Double.parseDouble(entry.getKey());
            Vector point = engine.getPos(entry.getValue()).orElseGet(Vector::new);
            transform.put(time, new PointInterpolation(point, "linear"));
          } catch (IllegalStateException e) {
            double time = Double.parseDouble(entry.getKey());
            Vector point = engine.getPos(entry.getValue().getAsJsonObject().get("post")).orElseGet(Vector::new);
            String lerp = entry.getValue().getAsJsonObject().get("lerp_mode").getAsString();
            transform.put(time, new PointInterpolation(point, lerp));
          }
        }
      } catch (IllegalStateException e) {
        Vector point = engine.getPos(keyframes).orElseGet(Vector::new);
        transform.put(0.0, new PointInterpolation(point, "linear"));
      }

      cache = calculateAllTransforms(length, transform);
      if (type == AnimationType.ROTATION) {
        engine.getModelLoader().addRotationCache(modelName, bone.getName() + "/" + animationName, cache);
      } else if (type == AnimationType.TRANSLATION) {
        engine.getModelLoader().addTranslationCache(modelName, bone.getName() + "/" + animationName, cache);
      }
    }

    interpolationCache = cache;
    bone.addAnimation(this);
  }

  @Override
  public AnimationType getType() {
    return type;
  }

  @Override
  public boolean isPlaying() {
    return playing;
  }

  @Override
  public void tick() {
    if (playing) {
      if (direction == AnimationDirection.FORWARD) {
        ++tick;
        if (tick > length) {
          tick = 0;
        }
      } else if (direction == AnimationDirection.BACKWARD) {
        --tick;
        if (tick < 0) {
          tick = (short) length;
        }
      }
    }
  }

  @Override
  public Vector calculateTransform(int tick, LinkedHashMap<Double, PointInterpolation> transform) {
    double toInterpolate = tick * 50.0 / 1000;
    if (type == AnimationType.ROTATION) {
      return Interpolator.interpolateRotation(toInterpolate, transform, length).multiply(ROTATION_MUL);
    } else if (type == AnimationType.TRANSLATION) {
      return Interpolator.interpolateTranslation(toInterpolate, transform, length).multiply(TRANSLATION_MUL);
    } else {
      throw new IllegalStateException();
    }
  }

  @Override
  public Vector getTransform() {
    if (!playing) {
      return new Vector();
    }

    return interpolationCache.getOrDefault(tick, new Vector()).clone();
  }

  @Override
  public Vector getTransformAtTime(int time) {
    return interpolationCache.getOrDefault((short) time, new Vector()).clone();
  }

  @Override
  public void setDirection(AnimationDirection direction) {
    this.direction = direction;
  }

  @Override
  public Map<Short, Vector> calculateAllTransforms(double animationTime, LinkedHashMap<Double, PointInterpolation> t) {
    Map<Short, Vector> transform = new HashMap<>();
    int ticks = (int) (animationTime * 20);

    for (int i = 0; i <= ticks; i++) {
      transform.put((short) i, calculateTransform(i, t));
    }

    return transform;
  }

  @Override
  public void stop() {
    tick = 0;
    playing = false;
    direction = AnimationDirection.FORWARD;
  }

  @Override
  public void play() {
    if (direction == AnimationDirection.FORWARD) {
      tick = 0;
    } else if (direction == AnimationDirection.BACKWARD) {
      tick = (short) (length - 1);
    }
    playing = true;
  }

  @Override
  public String name() {
    return name;
  }
}
