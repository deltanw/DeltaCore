package su.deltanw.core.impl.entity.model;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.util.Vector;
import su.deltanw.core.api.entity.model.ModelEngine;
import su.deltanw.core.api.entity.model.PlayerModel;
import su.deltanw.core.api.entity.model.bone.ModelBone;
import su.deltanw.core.impl.entity.model.bone.BoneEntity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerModelImpl extends AbstractEntityModel implements PlayerModel {

  protected static final Gson GSON =
      new GsonBuilder()
          .setPrettyPrinting()
          .disableHtmlEscaping()
          .create();

  private static final Map<String, Vector> BONE_OFFSETS = Map.ofEntries(
      Map.entry("_head", new Vector(0.0,5.616,0.0)),
      Map.entry("body", new Vector(0.0,5.616,0.0)),
      Map.entry("right_arm", new Vector(1.17,5.148,0.0)),
      Map.entry("left_arm", new Vector(-1.17,5.148,0.0)),
      Map.entry("right_leg", new Vector(0.4446,2.808,0.0)),
      Map.entry("left_leg", new Vector(-0.4446,2.808,0.0))
  );

  private static final Map<String, Vector> BONE_OFFSETS_SLIM = Map.ofEntries(
      Map.entry("_head", new Vector(0.0,5.616,0.0)),
      Map.entry("body", new Vector(0.0,5.616,0.0)),
      Map.entry("right_arm", new Vector(1.02375,5.148,0.0)),
      Map.entry("left_arm", new Vector(-1.02375,5.148,0.0)),
      Map.entry("right_leg", new Vector(0.4446,2.808,0.0)),
      Map.entry("left_leg", new Vector(-0.4446,2.808,0.0))
  );

  private static final Map<String, Vector> BONE_DIFFS = Map.ofEntries(
      Map.entry("_head", new Vector(8.936,8.0,8.936)),
      Map.entry("body", new Vector(8.936,10.808,8.468)),
      Map.entry("right_arm", new Vector(8.234,10.34,8.468)),
      Map.entry("left_arm", new Vector(8.702,10.34,8.468)),
      Map.entry("right_leg", new Vector(8.4446,10.808,8.468)),
      Map.entry("left_leg", new Vector(8.4914,10.808,8.468))
  );

  private static final String[] ORDER = {
      "_head",
      "body",
      "right_arm",
      "left_arm",
      "right_leg",
      "left_leg",
  };

  private static final JsonObject MODEL_DATA;
  private static final JsonObject MODEL_DATA_SLIM;

  private static JsonObject loadModelData(String path) throws IOException {
    try (InputStream stream = PlayerModelImpl.class.getResourceAsStream(path)) {
      if (stream == null) {
        throw new FileNotFoundException();
      }
      return GSON.fromJson(new InputStreamReader(stream), JsonObject.class);
    }
  }

  static {
    try {
      MODEL_DATA = loadModelData("/model/player.json");
      MODEL_DATA_SLIM = loadModelData("/model/player_slim.json");
    } catch (IOException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private final List<ModelBone> bones = new ArrayList<>();
  private PlayerProfile profile;
  private boolean isSlim;

  public PlayerModelImpl(ModelEngine<ItemStack> modelEngine, PlayerProfile profile) {
    super(modelEngine);
    setProfile(profile, false);
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public void spawn(Location location, float scale) {
    setPosition(location.toVector().toLocation(location.getWorld()));
    setGlobalRotation(location.getYaw());

    loadBones(location.getWorld(), isSlim ? MODEL_DATA_SLIM : MODEL_DATA, 1);

    for (String boneName : ORDER) {
      ModelBone bone = parts.get(boneName);
      bones.add(bone);
      if (bone != null) {
        bone.spawn(bone.calculatePosition());
      }
    }

    display();
    display();
    display();

    updateProfile();
  }

  protected void updateProfile() {
    for (int i = 0; i < ORDER.length; ++i) {
      var item = new org.bukkit.inventory.ItemStack(Material.PLAYER_HEAD);
      SkullMeta meta = (SkullMeta) item.getItemMeta();
      meta.setPlayerProfile(profile);
      meta.setCustomModelData(i + (isSlim ? 7 : 1));
      item.setItemMeta(meta);
      ItemStack itemStack = CraftItemStack.asNMSCopy(item);

      BoneEntity boneEntity = bones.get(i).getEntity();
      if (!(boneEntity.getEntity() instanceof LivingEntity living)) {
        throw new IllegalStateException(boneEntity.getEntity().getClass().getName());
      }

      living.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
      boneEntity.updateEquipment();
    }
  }

  @Override
  public Vector getDiff(String bone) {
    return BONE_DIFFS.get(bone);
  }

  @Override
  public void setScale(float scale) {
  }

  @Override
  public Vector getOffset(String bone) {
    return (isSlim ? BONE_OFFSETS_SLIM : BONE_OFFSETS).get(bone);
  }

  @Override
  public PlayerProfile getProfile() {
    return profile;
  }

  protected void setProfile(PlayerProfile profile, boolean updateProfile) {
    this.profile = profile;
    this.isSlim = profile.getTextures().getSkinModel() == PlayerTextures.SkinModel.SLIM;
    if (updateProfile) {
      updateProfile();
    }
  }

  @Override
  public void setProfile(PlayerProfile profile) {
    setProfile(profile, true);
  }
}
