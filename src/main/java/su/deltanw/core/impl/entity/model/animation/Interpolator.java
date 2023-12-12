package su.deltanw.core.impl.entity.model.animation;

import org.bukkit.util.Vector;
import org.joml.Quaterniond;
import su.deltanw.core.api.entity.model.animation.PointInterpolation;
import su.deltanw.core.impl.entity.model.ModelMath;

import java.util.LinkedHashMap;

public class Interpolator {

  private record StartEnd(PointInterpolation s, PointInterpolation e, double st, double et) {

  }

  private static StartEnd getStartEnd(double time, LinkedHashMap<Double, PointInterpolation> transform, double animationTime) {
    if (transform.size() == 0) {
      return new StartEnd(new PointInterpolation(new Vector(), "linear"), new PointInterpolation(new Vector(), "linear"), 0, 0);
    }

    PointInterpolation lastPoint = transform.get(transform.keySet().iterator().next());
    double lastTime = 0;

    for (Double keyTime : transform.keySet()) {
      if (keyTime > time) {
        return new StartEnd(lastPoint, transform.get(keyTime), lastTime, keyTime);
      }

      lastPoint = transform.get(keyTime);
      lastTime = keyTime;
    }

    return new StartEnd(lastPoint, lastPoint, lastTime, animationTime);
  }

  public static Vector interpolateRotation(double time, LinkedHashMap<Double, PointInterpolation> transform, double animationTime) {
    StartEnd points = getStartEnd(time, transform, animationTime);

    double timeDiff = points.et - points.st;
    if (timeDiff == 0) {
      return points.s.p().clone();
    }

    double timePercent = (time - points.st) / timeDiff;

    if (points.s.lerp().equals("linear")) {
      Vector ps = points.s.p();
      Vector pe = points.e.p();
      return Vector.fromJOML(ps.toVector3d().lerp(pe.toVector3d(), timePercent));
    } else {
      Quaterniond qa = ModelMath.quaternion(points.s.p().clone().multiply(0.2));
      Quaterniond qb = ModelMath.quaternion(points.e.p().clone().multiply(0.2));
      return ModelMath.toEuler(qa.slerp(qb, timePercent)).multiply(5);
    }
  }

  public static Vector interpolateTranslation(double time, LinkedHashMap<Double, PointInterpolation> transform, double animationTime) {
    StartEnd points = getStartEnd(time, transform, animationTime);

    double timeDiff = points.et - points.st;
    if (timeDiff == 0) {
      return points.s.p().clone();
    }

    double timePercent = (time - points.st) / timeDiff;
    Vector ps = points.s.p();
    Vector pe = points.e.p();
    return Vector.fromJOML(ps.toVector3d().lerp(pe.toVector3d(), timePercent));
  }
}
