package su.deltanw.core.impl.entity.model;

import org.bukkit.util.Vector;
import org.joml.Matrix3d;
import org.joml.Quaterniond;

public class ModelMath {

  private static final float DEGREE = 0.017453292519943295F;
  private static final float RADIAN = 57.29577951308232F;

  public static Vector toRadians(Vector v) {
    return v.clone().multiply(DEGREE);
  }

  public static Vector toDegrees(Vector v) {
    return v.clone().multiply(RADIAN);
  }

  public static Quaterniond quaternion(Vector v) {
    v = toRadians(v);

    double cy = Math.cos(v.getZ() * 0.5);
    double sy = Math.sin(v.getZ() * 0.5);
    double cp = Math.cos(v.getY() * 0.5);
    double sp = Math.sin(v.getY() * 0.5);
    double cr = Math.cos(v.getX() * 0.5);
    double sr = Math.sin(v.getX() * 0.5);

    double x = sr * cp * cy - cr * sp * sy;
    double y = cr * sp * cy + sr * cp * sy;
    double z = cr * cp * sy - sr * sp * cy;
    double w = cr * cp * cy + sr * sp * sy;

    return new Quaterniond(x, y, z, w);
  }

  @SuppressWarnings("SuspiciousNameCombination")
  public static Vector rotate(Vector v, Vector r) {
    r = toRadians(r);

    double rX = r.getX();
    double rY = r.getY();
    double rZ = r.getZ();

    double cosX = Math.cos(rX);
    double sinX = Math.sin(rX);
    double cosY = Math.cos(rY);
    double sinY = Math.sin(rY);
    double cosZ = Math.cos(rZ);
    double sinZ = Math.sin(rZ);

    Matrix3d rmatX = new Matrix3d(1, 0, 0, 0, cosX, sinX, 0, -sinX, cosX);
    Matrix3d rmatY = new Matrix3d(cosY, 0, -sinY, 0, 1, 0, sinY, 0, cosY);
    Matrix3d rmatZ = new Matrix3d(cosZ, sinZ, 0, -sinZ, cosZ, 0, 0, 0, 1);

    return Vector.fromJOML(v.toVector3d().mul(rmatZ.mul(rmatY).mul(rmatX)));
  }

  public static Vector toEuler(Quaterniond q) {
    double t0 = (q.x + q.z) * (q.x - q.z);
    double t1 = (q.w + q.y) * (q.w - q.y);
    double xx = 0.5 * (t0 + t1);
    double xy = q.x * q.y + q.w * q.z;
    double xz = q.w * q.y - q.x * q.z;
    double t = xx * xx + xy * xy;
    double yz = 2.0 * (q.y * q.z + q.w * q.x);

    double vz = Math.atan2(xy, xx);
    double vy = Math.atan(xz / Math.sqrt(t));
    double vx = t != 0
        ? Math.atan2(yz, t1 - t0)
        : 2.0 * Math.atan2(q.x, q.w) - Math.signum(xz) * vz;

    return toDegrees(new Vector(vx, vy, vz));
  }

  private static Vector threeAxisRot(double r11, double r12, double r21, double r31, double r32) {
    double x = Math.atan2(r31, r32);
    double y = Math.asin(r21);
    double z = Math.atan2(r11, r12);
    return new Vector(x, z, y);
  }

  public static Vector toEulerYZX(Quaterniond q) {
    return toDegrees(
        threeAxisRot(
            -2 * (q.x * q.z - q.w * q.y),
            q.w * q.w + q.x * q.x - q.y * q.y - q.z * q.z,
            2 * (q.x * q.y + q.w * q.z),
            -2 * (q.y * q.z - q.w * q.x),
            q.w * q.w - q.x * q.x + q.y * q.y - q.z * q.z
        )
    );
  }
}
