package su.deltanw.core.impl.entity.thirdperson;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.core.NonNullList;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.deltanw.core.Core;
import su.deltanw.core.api.entity.model.PlayerModel;
import su.deltanw.core.api.entity.model.factory.EntityModelFactory;
import su.deltanw.core.api.entity.thirdperson.ThirdPersonViewController;
import su.deltanw.core.api.entity.thirdperson.callback.ThirdPersonCommandCallback;
import su.deltanw.core.api.entity.thirdperson.callback.ThirdPersonPlayerCommand;
import su.deltanw.core.api.entity.thirdperson.event.ThirdPersonPlayerCommandEvent;
import su.deltanw.core.api.entity.thirdperson.event.ThirdPersonViewEnterEvent;
import su.deltanw.core.api.entity.thirdperson.event.ThirdPersonViewQuitEvent;

import java.util.*;
import java.util.stream.IntStream;

// TODO: Refactor
public class ThirdPersonViewControllerImpl implements ThirdPersonViewController {

  private static final List<Pair<EquipmentSlot, ItemStack>> EMPTY_EQUIPMENT =
      Arrays.stream(EquipmentSlot.values())
          .map(slot -> new Pair<>(slot, ItemStack.EMPTY))
          .toList();

  private static final NonNullList<ItemStack> EMPTY_INVENTORY =
      IntStream.rangeClosed(0, 45).mapToObj(i -> ItemStack.EMPTY)
          .collect(NonNullList::create, NonNullList::add, NonNullList::addAll);

  private static final List<SynchedEntityData.DataValue<?>> INVISIBLE_METADATA =
      List.of(new SynchedEntityData.DataValue<>(0, EntityDataSerializers.BYTE, (byte) 0x20));

  private final ThirdPersonViewHandler handler;
  private final Player player;
  protected final ServerPlayer viewer;
  private final PlayerModel model;
  private final Location origin;
  protected Location viewPoint;
  private boolean active = false;
  private ThirdPersonCommandCallback callback = null;

  public ThirdPersonViewControllerImpl(EntityModelFactory<?, ?> factory,
                                       ThirdPersonViewHandler handler,
                                       Player player, Location viewPoint) {
    this.handler = handler;
    this.player = player;
    this.viewPoint = viewPoint.clone();
    this.model = factory.createPlayerModel(player);

    ServerLevel level = ((CraftWorld) player.getWorld()).getHandle();
    this.viewer = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "_"));
    this.viewer.setInvisible(true);
    this.viewer.setNoGravity(true);
    this.viewer.setSilent(true);
    this.viewer.passengers = ImmutableList.of(((CraftPlayer) player).getHandle());
    this.origin = player.getLocation();

    updateViewerPosition();
  }

  @Override
  public void setCallback(ThirdPersonCommandCallback callback) {
    this.callback = callback;
  }

  @Override
  public ThirdPersonCommandCallback getCallback() {
    return callback;
  }

  protected byte convertAngle(float angle) {
    return (byte) Math.floor(angle * 256.0F / 360.0F);
  }

  protected short convertDelta(double delta) {
    return (short) Math.round(delta * 4096.0);
  }

  protected void updateViewerPosition() {
    viewer.setPos(viewPoint.getX(), viewPoint.getY() - 1.6, viewPoint.getZ());
    viewer.setRot(viewPoint.getYaw(), viewPoint.getPitch());
  }

  protected void inject(Channel channel) {
    channel.pipeline().addBefore("packet_handler", "thirdperson_tracker",
        new ChannelInboundHandlerAdapter() {

          private static final Set<Class<? extends Packet<?>>> BLOCKED_PACKETS =
              Set.of(
                  ServerboundInteractPacket.class,
                  ServerboundContainerClickPacket.class,
                  ServerboundContainerButtonClickPacket.class,
                  ServerboundContainerClosePacket.class,
                  ServerboundEditBookPacket.class,
                  ServerboundSetCarriedItemPacket.class,
                  ServerboundSetBeaconPacket.class,
                  ServerboundSignUpdatePacket.class,
                  ServerboundSetStructureBlockPacket.class,
                  ServerboundSetJigsawBlockPacket.class,
                  ServerboundSetCreativeModeSlotPacket.class,
                  ServerboundSelectTradePacket.class,
                  ServerboundSetCommandBlockPacket.class,
                  ServerboundSetCommandMinecartPacket.class,
                  ServerboundUseItemPacket.class,
                  ServerboundUseItemOnPacket.class,
                  ServerboundPickItemPacket.class,
                  ServerboundPlaceRecipePacket.class,
                  ServerboundRenameItemPacket.class,
                  ServerboundPlayerActionPacket.class,
                  ServerboundSwingPacket.class,
                  ServerboundPlayerAbilitiesPacket.class
              );

          @Override
          public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
            if (BLOCKED_PACKETS.contains(msg.getClass())) {
              return;
            }

            if (msg instanceof ServerboundPlayerInputPacket packet) {
              if (packet.isShiftKeyDown()) {
                doCallback(ThirdPersonPlayerCommand.SHIFT_KEY);
              }
              if (packet.isJumping()) {
                doCallback(ThirdPersonPlayerCommand.JUMP);
              }
            }

            super.channelRead(ctx, msg);
          }

          @Override
          public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
            destroyView();
            super.channelInactive(ctx);
          }
        });
  }

  private void doCallback(ThirdPersonPlayerCommand command) {
    Bukkit.getScheduler().runTask(Core.getPlugin(Core.class), () -> {
      ThirdPersonPlayerCommandEvent event = new ThirdPersonPlayerCommandEvent(ThirdPersonViewControllerImpl.this, command);
      Bukkit.getPluginManager().callEvent(event);
      if (!event.isCancelled() && callback != null) {
        callback.execute(command);
      }
    });
  }

  protected void deject(Channel channel) {
    if (channel.isActive()) {
      channel.pipeline().remove("thirdperson_tracker");
    }
  }

  @Override
  public boolean enterView() {
    ThirdPersonViewEnterEvent event = new ThirdPersonViewEnterEvent(this);
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      return false;
    }

    ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
    Object[] viewers = serverPlayer.getPlayersInTrackRange().getBackingSet();

    Packet<?> equipmentPacket = new ClientboundSetEquipmentPacket(serverPlayer.getId(), EMPTY_EQUIPMENT);
    Packet<?> metadataPacket = new ClientboundSetEntityDataPacket(serverPlayer.getId(), INVISIBLE_METADATA);
    model.addViewer(serverPlayer.getBukkitEntity());
    if (viewers != null) {
      for (Object object : viewers) {
        if (object instanceof ServerPlayer modelViewer) {
          model.addViewer(modelViewer.getBukkitEntity());
          modelViewer.connection.send(equipmentPacket);
          modelViewer.connection.send(metadataPacket);
        }
      }
    }

    model.spawn(origin);

    ServerGamePacketListenerImpl connection = serverPlayer.connection;
    connection.send(metadataPacket, PacketSendListener.thenRun(() -> handler.lockPlayer(this)));
    connection.send(new ClientboundPlayerInfoUpdatePacket(
        EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER), Collections.singleton(viewer)));
    connection.send(new ClientboundAddPlayerPacket(viewer));
    connection.send(new ClientboundRotateHeadPacket(viewer, convertAngle(viewPoint.getYaw())));
    connection.send(new ClientboundSetCameraPacket(viewer));
    List<SynchedEntityData.DataValue<?>> metadata = viewer.getEntityData().getNonDefaultValues();
    if (metadata != null && !metadata.isEmpty()) {
      connection.send(new ClientboundSetEntityDataPacket(viewer.getId(), metadata));
    }
    connection.send(new ClientboundSetPassengersPacket(viewer));
    connection.send(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(viewer.getUUID())));
    active = true;

    int stateId = serverPlayer.inventoryMenu.incrementStateId();
    connection.send(new ClientboundContainerSetContentPacket(0, stateId, EMPTY_INVENTORY, ItemStack.EMPTY), null);

    inject(connection.connection.channel);
    return true;
  }

  private List<SynchedEntityData.DataValue<?>> packMetadata(ServerPlayer serverPlayer) {
    List<SynchedEntityData.DataValue<?>> values = serverPlayer.getEntityData().getNonDefaultValues();
    boolean hasFlags = false;
    if (values != null) {
      for (SynchedEntityData.DataValue<?> value : values) {
        if (value.id() == 0) {
          hasFlags = true;
          break;
        }
      }
    } else {
      values = new ArrayList<>();
    }

    if (!hasFlags) {
      values.add(new SynchedEntityData.DataValue<>(0, EntityDataSerializers.BYTE, (byte) 0));
    }

    return values;
  }

  @Override
  public void destroyView() {
    if (!active) {
      return;
    }

    handler.unlockPlayer(player);
    model.destroy();
    ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
    ServerGamePacketListenerImpl connection = serverPlayer.connection;

    Object[] viewers = serverPlayer.getPlayersInTrackRange().getBackingSet();

    List<Pair<EquipmentSlot, ItemStack>> equipment = new ArrayList<>();
    EquipmentSlot[] equipmentSlots = EquipmentSlot.values();

    for (EquipmentSlot equipmentSlot : equipmentSlots) {
      ItemStack item = serverPlayer.getItemBySlot(equipmentSlot);
      equipment.add(Pair.of(equipmentSlot, item));
    }

    Packet<?> equipmentPacket = new ClientboundSetEquipmentPacket(serverPlayer.getId(), equipment);
    Packet<?> metadataPacket = new ClientboundSetEntityDataPacket(serverPlayer.getId(), packMetadata(serverPlayer));

    if (viewers != null) {
      for (Object object : viewers) {
        if (object instanceof ServerPlayer modelViewer) {
          modelViewer.connection.send(equipmentPacket);
          modelViewer.connection.send(metadataPacket);
        }
      }
    }

    deject(connection.connection.channel);

    connection.send(new ClientboundSetCameraPacket(serverPlayer));
    connection.send(new ClientboundRemoveEntitiesPacket(viewer.getId()));

    int stateId = serverPlayer.inventoryMenu.incrementStateId();
    connection.send(new ClientboundContainerSetContentPacket(
        0, stateId, serverPlayer.inventoryMenu.remoteSlots, serverPlayer.inventoryMenu.getCarried()));

    player.teleport(origin);

    active = false;
    Bukkit.getPluginManager().callEvent(new ThirdPersonViewQuitEvent(this));
  }

  protected void rotateClientside(float yaw, float pitch) {
    ServerPlayerConnection connection = ((CraftPlayer) player).getHandle().connection;
    connection.send(new ClientboundMoveEntityPacket.Rot(
        viewer.getId(), convertAngle(yaw), convertAngle(pitch), false));
    connection.send(new ClientboundRotateHeadPacket(viewer, convertAngle(yaw)));
  }

  protected void moveClientside(double deltaX, double deltaY, double deltaZ) {
    ServerPlayerConnection connection = ((CraftPlayer) player).getHandle().connection;
    connection.send(new ClientboundMoveEntityPacket.Pos(
        viewer.getId(), convertDelta(deltaX), convertDelta(deltaY), convertDelta(deltaZ), false));
  }

  protected void moveClientside(double deltaX, double deltaY, double deltaZ, float yaw, float pitch) {
    ServerPlayerConnection connection = ((CraftPlayer) player).getHandle().connection;
    connection.send(new ClientboundMoveEntityPacket.PosRot(
        viewer.getId(),
        convertDelta(deltaX), convertDelta(deltaY), convertDelta(deltaZ),
        convertAngle(yaw), convertAngle(pitch),
        false
    ));
    connection.send(new ClientboundRotateHeadPacket(viewer, convertAngle(yaw)));
  }

  @Override
  public void rotate(float yaw, float pitch) {
    viewPoint.setYaw(yaw);
    viewPoint.setPitch(pitch);
    updateViewerPosition();

    if (active) {
      rotateClientside(yaw, pitch);
    }
  }

  @Override
  public void move(double deltaX, double deltaY, double deltaZ) {
    viewPoint.add(deltaX, deltaY, deltaZ);
    updateViewerPosition();

    if (active) {
      moveClientside(deltaX, deltaY, deltaZ);
    }
  }

  @Override
  public void move(double deltaX, double deltaY, double deltaZ, float yaw, float pitch) {
    viewPoint.add(deltaX, deltaY, deltaZ);
    viewPoint.setYaw(yaw);
    viewPoint.setPitch(pitch);
    updateViewerPosition();

    if (active) {
      moveClientside(deltaX, deltaY, deltaZ, yaw, pitch);
    }
  }

  @Override
  public void moveTo(Location to) {
    Location delta = to.clone().subtract(viewPoint);
    move(delta.getX(), delta.getY(), delta.getZ(), to.getYaw(), to.getPitch());
  }

  @Override
  public Player getPlayer() {
    return player;
  }

  @Override
  public Location getViewPoint() {
    return viewPoint;
  }

  @Override
  public PlayerModel getModel() {
    return model;
  }

  @Override
  public boolean isActive() {
    return active;
  }

  public List<Pair<EquipmentSlot, ItemStack>> getEquipment() {
    return EMPTY_EQUIPMENT;
  }

  public NonNullList<ItemStack> getInventory() {
    return EMPTY_INVENTORY;
  }

  public List<SynchedEntityData.DataValue<?>> getMetadata() {
    return INVISIBLE_METADATA;
  }
}
