package com.kay9.dragonmounts.data.providers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.kay9.dragonmounts.DragonMountsLegacy;
import com.kay9.dragonmounts.abilities.FrostWalkerAbility;
import com.kay9.dragonmounts.abilities.GreenToesAbility;
import com.kay9.dragonmounts.abilities.SnowStepperAbility;
import com.kay9.dragonmounts.dragon.TameableDragon;
import com.kay9.dragonmounts.dragon.breed.BreedRegistry;
import com.kay9.dragonmounts.dragon.breed.DragonBreed;
import com.kay9.dragonmounts.habitats.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.io.IOException;
import java.util.Optional;

public class DragonBreedProvider implements DataProvider
{
    private static final RegistryOps<JsonElement> OPS = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.builtinCopy());

    static final DragonBreed AETHER = DragonBreed.builtIn("aether", DragonMountsLegacy.id("aether"),
            0x718AA9,
            0xE6E6E6,
            Optional.empty(),
            DragonBreed.ModelProperties.STANDARD,
            ImmutableMap.of(Attributes.FLYING_SPEED, TameableDragon.BASE_SPEED_FLYING * 1.45),
            list(),
            list(new HeightHabitat(3, false, 200)),
            set(),
            Optional.empty());

    static final DragonBreed END = DragonBreed.builtIn("end", DragonMountsLegacy.id("end"),
            0x161616,
            0xff63e8,
            Optional.of(ParticleTypes.PORTAL),
            DragonBreed.ModelProperties.STANDARD,
            ImmutableMap.of(Attributes.MAX_HEALTH, TameableDragon.BASE_HEALTH * 1.25),
            list(), // teleport ability?
            list(DragonBreathHabitat.INSTANCE),
            set("dragonBreath"),
            Optional.empty());

    static final DragonBreed FOREST = DragonBreed.builtIn("forest", DragonMountsLegacy.id("forest"),
            0x054a00,
            0x0a9600,
            Optional.of(ParticleTypes.HAPPY_VILLAGER),
            DragonBreed.ModelProperties.STANDARD,
            ImmutableMap.of(),
            list(GreenToesAbility.INSTANCE),
            list(new NearbyBlocksHabitat(0.5f, tagSet(BlockTagProvider.FOREST_DRAGON_HABITAT_BLOCKS)), new BiomeHabitat(2, tagSet(BiomeTags.IS_JUNGLE))),
            set(),
            Optional.empty());

    static final DragonBreed GHOST = DragonBreed.builtIn("ghost", DragonMountsLegacy.id("ghost"),
            0xc4c4c4,
            0xc2f8ff,
            Optional.empty(),
            new DragonBreed.ModelProperties(true, false, true),
            ImmutableMap.of(),
            list(),
            list(new PickyHabitat(list(new HeightHabitat(1, true, 0), new LightHabitat(2, true, 3)))),
            set("drown"),
            Optional.of(SoundEvents.SKELETON_AMBIENT));

    static final DragonBreed ICE = DragonBreed.builtIn("ice", DragonMountsLegacy.id("ice"),
            0xffffff,
            0x00E1FF,
            Optional.of(ParticleTypes.SNOWFLAKE),
            DragonBreed.ModelProperties.STANDARD,
            ImmutableMap.of(),
            list(FrostWalkerAbility.INSTANCE, SnowStepperAbility.INSTANCE),
            list(new NearbyBlocksHabitat(0.5f, tagSet(BlockTagProvider.ICE_DRAGON_HABITAT_BLOCKS))),
            set("drown", "freeze"),
            Optional.empty());

    static final DragonBreed NETHER = DragonBreed.builtIn("nether", DragonMountsLegacy.id("nether"),
            0x912400,
            0x2e0b00,
            Optional.of(ParticleTypes.SOUL_FIRE_FLAME),
            DragonBreed.ModelProperties.STANDARD,
            ImmutableMap.of(Attributes.ARMOR, 8d),
            list(),
            list(new NearbyBlocksHabitat(1f, tagSet(BlockTagProvider.NETHER_DRAGON_HABITAT_BLOCKS)), new BiomeHabitat(3, tagSet(BiomeTags.IS_NETHER))),
            set("inFire", "onFire", "lava", "hotFloor"),
            Optional.empty());

    static final DragonBreed WATER = DragonBreed.builtIn("water", DragonMountsLegacy.id("water"),
            0x0062ff,
            0x5999ff,
            Optional.of(ParticleTypes.DRIPPING_WATER),
            new DragonBreed.ModelProperties(true, true, false),
            ImmutableMap.of(),
            list(),
            list(new FluidHabitat(1f, tagSet(FluidTags.WATER))),
            set("drown"),
            Optional.empty());

    private final DataGenerator generator;
    private CachedOutput cache;

    public DragonBreedProvider(DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void run(CachedOutput cache) throws IOException
    {
        this.cache = cache;

        for (DragonBreed dragonBreed : new DragonBreed[]{AETHER, END, BreedRegistry.FIRE_BUILTIN.get(), FOREST, GHOST, ICE, NETHER, WATER})
            encode(dragonBreed);
    }

    protected void encode(DragonBreed breed) throws IOException
    {
        String packName = breed.getRegistryName().getNamespace();
        String type = breed.getRegistryName().getPath();

        var json = DragonBreed.CODEC.encodeStart(OPS, breed).getOrThrow(false, DragonMountsLegacy.LOG::error);
        var regKey = BreedRegistry.REGISTRY.get().registryKeySet().toString();
        var path = String.join("/", "data", packName, regKey, regKey, type + ".json");

        DataProvider.saveStable(cache, json, generator.getOutputFolder().resolve(path));
    }

    @Override
    public String getName()
    {
        return "Dragon Breeds";
    }

    protected static <T> ImmutableList<T> list(T... objs)
    {
        return ImmutableList.copyOf(objs);
    }

    protected static <T> ImmutableSet<T> set(T... objs)
    {
        return ImmutableSet.copyOf(objs);
    }

    protected static <T> TagKey<T> tagSet(TagKey<T> key)
    {
        return OPS.registry(key.registry()).orElseThrow().getOrCreateTag(key).key();
    }
}
