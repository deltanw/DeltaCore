package su.deltanw.core.impl.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

public class Vector3DataType implements PersistentDataType<byte[], Vector3f> {

  public static final Vector3DataType INSTANCE = new Vector3DataType();

  @Override
  public @NotNull Class<byte[]> getPrimitiveType() {
    return byte[].class;
  }

  @Override
  public @NotNull Class<Vector3f> getComplexType() {
    return Vector3f.class;
  }

  @Override
  public byte @NotNull [] toPrimitive(@NotNull Vector3f complex, @NotNull PersistentDataAdapterContext context) {
    ByteBuffer buffer = ByteBuffer.wrap(new byte[12]);
    buffer.putFloat(complex.x);
    buffer.putFloat(complex.y);
    buffer.putFloat(complex.z);
    return buffer.array();
  }

  @Override
  public @NotNull Vector3f fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
    ByteBuffer buffer = ByteBuffer.wrap(primitive);
    float x = buffer.getFloat();
    float y = buffer.getFloat();
    float z = buffer.getFloat();
    return new Vector3f(x, y, z);
  }
}
