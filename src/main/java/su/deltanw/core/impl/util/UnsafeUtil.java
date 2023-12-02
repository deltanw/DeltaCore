package su.deltanw.core.impl.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import sun.misc.Unsafe;

public final class UnsafeUtil {

  public static final Unsafe UNSAFE;

  static {
    try {
      Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      UNSAFE = (Unsafe) unsafeField.get(null);
    } catch (Throwable throwable) {
      throw new ExceptionInInitializerError(throwable);
    }
  }

  public static long fieldOffset(Field field) {
    if (Modifier.isStatic(field.getModifiers())) {
      return UNSAFE.staticFieldOffset(field);
    } else {
      return UNSAFE.objectFieldOffset(field);
    }
  }

  private UnsafeUtil() {

  }
}
