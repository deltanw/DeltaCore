package su.deltanw.core.impl.biome;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Lifecycle;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import io.papermc.paper.adventure.PaperAdventure;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeBuilder;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.BiomeSpecialEffects.GrassColorModifier;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.CraftRegistry;
import su.deltanw.core.Core;
import su.deltanw.core.config.BiomesConfig.SerializedCustomBiome;
import su.deltanw.core.config.BiomesConfig.SerializedCustomBiome.MOB_SPAWN.SPAWN_COST;
import su.deltanw.core.config.BiomesConfig.SerializedCustomBiome.MOB_SPAWN.SPAWNERS;
import su.deltanw.core.config.BiomesConfig.SerializedCustomBiome.EFFECTS;
import su.deltanw.core.config.BiomesConfig.SerializedCustomBiome.EFFECTS.AMBIENT;
import su.deltanw.core.impl.util.UnsafeUtil;

public record CustomBiome(NamespacedKey key, Biome biome) {

  private static final Map<NamespacedKey, CustomBiome> BIOME_REGISTRY = new HashMap<>();
  private static final long SPAWN_ENTITY_TYPE_OFFSET;

  static {
    SPAWN_ENTITY_TYPE_OFFSET = UnsafeUtil.fieldOffset(SpawnerData.class.getDeclaredFields()[0]);

    // Unfrozing registry makes it able to register new items
    Field frozen = MappedRegistry.class.getDeclaredFields()[10];
    frozen.setAccessible(true);
    try {
      frozen.setBoolean(CraftRegistry.getMinecraftRegistry(Registries.BIOME), false);
    } catch (IllegalAccessException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static List<CustomBiome> getAll() {
    return BIOME_REGISTRY.values().stream().toList();
  }

  public static CustomBiome get(NamespacedKey key) {
    return BIOME_REGISTRY.get(key);
  }

  public static void registerFromConfig(String biomeKey, SerializedCustomBiome biome) {
    ResourceLocation key = new ResourceLocation(biomeKey);
    MappedRegistry<Biome> biomes = (MappedRegistry<Biome>) CraftRegistry.getMinecraftRegistry(Registries.BIOME);
    if (biomes.containsKey(key)) {
      Core.getPlugin(Core.class).getLogger()
          .severe("Failed to add biome '" + biomeKey + "' as it is already exists!");
      return;
    }

    Biome nmsBiome = buildFromConfig(biome).build();

    biomes.register(ResourceKey.create(Registries.BIOME, key), nmsBiome, Lifecycle.stable());

    NamespacedKey namespacedKey = NamespacedKey.fromString(biomeKey);
    BIOME_REGISTRY.put(namespacedKey, new CustomBiome(namespacedKey, nmsBiome));

    // Hook into WorldEdit
    BiomeTypes.register(new BiomeType(biomeKey));
  }

  private static BiomeBuilder buildFromConfig(SerializedCustomBiome biome) {
    EFFECTS effects = biome.EFFECTS;
    AMBIENT ambient = effects.AMBIENT;

    ParticleOptions particleOptions = parseParticle(ambient.PARTICLE.PARTICLE);

    AmbientMoodSettings moodSettings = new AmbientMoodSettings(
        Holder.direct(SoundEvent.createVariableRangeEvent(new ResourceLocation(ambient.MOOD.SOUND))),
        ambient.MOOD.TICK_DELAY,
        ambient.MOOD.BLOCK_SEARCH_EXTENT,
        ambient.MOOD.SOUND_POSITION_OFFSET
    );

    AmbientAdditionsSettings additionsSettings = new AmbientAdditionsSettings(
        Holder.direct(SoundEvent.createVariableRangeEvent(new ResourceLocation(ambient.ADDITIONS.SOUND))),
        ambient.ADDITIONS.TICK_CHANCE
    );

    GrassColorModifier modifier = switch (effects.GRASS_COLOR_MODIFIER) {
      case "none" -> GrassColorModifier.NONE;
      case "dark_forest" -> GrassColorModifier.DARK_FOREST;
      case "swamp" -> GrassColorModifier.SWAMP;
      default -> throw new IllegalStateException("Unexpected value: " + effects.GRASS_COLOR_MODIFIER);
    };

    BiomeSpecialEffects.Builder specialEffects = new BiomeSpecialEffects.Builder()
        .ambientAdditionsSound(additionsSettings)
        .ambientLoopSound(Holder.direct(SoundEvent.createVariableRangeEvent(new ResourceLocation(ambient.SOUND))))
        .ambientMoodSound(moodSettings)
        .fogColor(effects.FOG_COLOR)
        .waterColor(effects.WATER_COLOR)
        .waterFogColor(effects.WATER_FOG_COLOR)
        .skyColor(effects.SKY_COLOR)
        .foliageColorOverride(effects.FOLIAGE_COLOR)
        .grassColorOverride(effects.GRASS_COLOR)
        .grassColorModifier(modifier);

    if (particleOptions != null) {
      specialEffects.ambientParticle(new AmbientParticleSettings(
          particleOptions, (float) ambient.PARTICLE.PROBABILITY));
    }

    MobSpawnSettings.Builder mobBuilder = new MobSpawnSettings.Builder()
        .creatureGenerationProbability((float) biome.MOB_SPAWN.CREATURE_GENERATION_PROBABILITY);

    for (SPAWN_COST spawnCost : biome.MOB_SPAWN.SPAWN_COST) {
      EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(spawnCost.ENTITY_TYPE));

      mobBuilder.addMobCharge(entityType, spawnCost.ENERGY_BUDGET, spawnCost.CHARGE);
    }

    for (SPAWNERS spawner : biome.MOB_SPAWN.SPAWNERS) {
      EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(spawner.ENTITY_TYPE));

      MobSpawnSettings.SpawnerData data = new MobSpawnSettings.SpawnerData(
          entityType, spawner.WEIGHT, spawner.MIN_COUNT, spawner.MAX_COUNT
      );

      // Spawner replaces "MISC" entities with pig.
      UnsafeUtil.UNSAFE.putObject(data, SPAWN_ENTITY_TYPE_OFFSET, entityType);

      mobBuilder.addSpawn(MobCategory.valueOf(spawner.MOB_CATEGORY), data);
    }

    return new BiomeBuilder()
        .hasPrecipitation(biome.PRECIPITATION)
        .temperature((float) biome.TEMPERATURE)
        .downfall((float) biome.DOWNFALL)
        .specialEffects(specialEffects.build())
        .mobSpawnSettings(mobBuilder.build())

        // TODO: generation settings
        .generationSettings(new BiomeGenerationSettings.PlainBuilder().build());
  }

  private static ParticleOptions parseParticle(String description) {
    if (description.equals("NONE")) {
      return null;
    }

    try {
      return ParticleArgument.readParticle(new StringReader(description), BuiltInRegistries.PARTICLE_TYPE.asLookup());
    } catch (CommandSyntaxException e) {
      Core.getPlugin(Core.class).getLogger()
          .severe("Failed to parse particle: " + description + ": " + PaperAdventure.asPlain(e.componentMessage(), Locale.US));
      return null;
    }
  }
}
