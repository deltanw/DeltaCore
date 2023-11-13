package su.deltanw.core.impl.injection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import su.deltanw.core.api.injection.ChannelInjector;
import su.deltanw.core.api.injection.InjectionException;
import su.deltanw.core.api.injection.Injector;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class InjectorImpl implements Injector {

  private final class InjectedList extends ArrayList<ChannelFuture> {

    private InjectedList(List<? extends ChannelFuture> originalList) {
      super(originalList);
    }

    @Override
    public boolean add(ChannelFuture channelFuture) {
      inject(channelFuture.channel());
      return super.add(channelFuture);
    }

    @Override
    public void add(int index, ChannelFuture element) {
      inject(element.channel());
      super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends ChannelFuture> c) {
      c.forEach(channelFuture -> inject(channelFuture.channel()));
      return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends ChannelFuture> c) {
      c.forEach(channelFuture -> inject(channelFuture.channel()));
      return super.addAll(index, c);
    }
  }

  private static Field findField(Class<?> cls, Predicate<Field> predicate) throws NoSuchMethodException {
    for (Field field : cls.getDeclaredFields()) {
      if (predicate.test(field)) {
        field.setAccessible(true);
        return field;
      }
    }

    Class<?> superclass = cls.getSuperclass();
    if (superclass != null) {
      return findField(superclass, predicate);
    } else {
      throw new NoSuchMethodException("in class " + cls.getName());
    }
  }

  private final List<ChannelInjector> injectors = Collections.synchronizedList(new ArrayList<>());
  private final ChannelInjectionHandler injectionHandler = new ChannelInjectionHandler(injectors);
  private final Field openChannelsField;
  private final Object connection;
  private List<? extends ChannelFuture> openChannels;

  @SuppressWarnings("unchecked")
  public InjectorImpl() {
    try {
      Server server = Bukkit.getServer();
      Field consoleField = server.getClass().getDeclaredField("console");
      consoleField.setAccessible(true);
      Object minecraftServer = consoleField.get(server);
      Field connectionField = findField(minecraftServer.getClass(),
          field -> field.getType().getSimpleName().equals("ServerConnection"));
      connection = connectionField.get(minecraftServer);

      for (Field field : connection.getClass().getDeclaredFields()) {
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType type)) {
          continue;
        }

        if (type.getRawType() != List.class) {
          continue;
        }

        Type firstParameter = type.getActualTypeArguments()[0];
        if (!firstParameter.getTypeName().endsWith("ChannelFuture")) {
          continue;
        }

        field.setAccessible(true);
        openChannelsField = field;
        openChannels = (List<? extends ChannelFuture>) field.get(connection);
        return;
      }

      throw new InjectionException("Couldn't inject.");
    } catch (ReflectiveOperationException e) {
      throw new InjectionException(e);
    }
  }

  public void inject() {
    ensureNotInjected();
    openChannels = Collections.synchronizedList(new InjectedList(openChannels));
    openChannels.forEach(channelFuture -> inject(channelFuture.channel()));

    try {
      openChannelsField.set(connection, openChannels);
    } catch (ReflectiveOperationException e) {
      throw new InjectionException(e);
    }
  }

  private void inject(Channel channel) {
    channel.pipeline().addFirst(injectionHandler);
  }

  @Override
  public void addInjector(ChannelInjector injector) {
    ensureNotInjected();
    injectors.add(injector);
  }

  @Override
  public void removeInjector(ChannelInjector injector) {
    ensureNotInjected();
    injectors.remove(injector);
  }

  @Override
  public List<ChannelInjector> getInjectors() {
    return injectors;
  }

  @Override
  public List<? extends ChannelFuture> getOpenChannels() {
    return openChannels;
  }

  private void ensureNotInjected() {
    if (openChannels instanceof InjectedList) {
      throw new InjectionException("Already injected.");
    }
  }
}
