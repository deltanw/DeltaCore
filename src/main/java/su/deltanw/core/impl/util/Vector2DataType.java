package su.deltanw.core.impl.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.nio.ByteBuffer;

public class Vector2DataType implements PersistentDataType<byte[], Vector2f> {

  public static Vector2DataType INSTANCE = new Vector2DataType();

  @Override
  public @NotNull Class<byte[]> getPrimitiveType() {
    return byte[].class;
  }

  @Override
  public @NotNull Class<Vector2f> getComplexType() {
    return Vector2f.class;
  }

  @Override
  public byte @NotNull [] toPrimitive(@NotNull Vector2f complex, @NotNull PersistentDataAdapterContext context) {
    ByteBuffer buffer = ByteBuffer.wrap(new byte[8]);
    buffer.putFloat(complex.x);
    buffer.putFloat(complex.y);
    return buffer.array();
  }

  @Override
  public @NotNull Vector2f fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
    ByteBuffer buffer = ByteBuffer.wrap(primitive);
    float x = buffer.getFloat();
    float y = buffer.getFloat();
    return new Vector2f(x, y);
  }
}
