package com.kay9.dragonmounts.dragon.breed;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.kay9.dragonmounts.DragonMountsLegacy;
import com.kay9.dragonmounts.habitats.FluidHabitat;
import com.kay9.dragonmounts.habitats.NearbyBlocksHabitat;
import com.kay9.dragonmounts.util.DMLTagCreator;
import com.mojang.serialization.Codec;
import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import io.github.fabricators_of_create.porting_lib.util.ServerLifecycleHooks;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;

import java.util.Optional;
import java.util.function.Supplier;

public class BreedRegistry {
    private static final ResourceKey<Registry<DragonBreed>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonMountsLegacy.id("dragon_breeds"));
    public static final LazyRegistrar<DragonBreed> DEFERRED_REGISTRY = LazyRegistrar.create(REGISTRY_KEY, DragonMountsLegacy.MOD_ID);
    public static final RegistryObject<DragonBreed> FIRE_BUILTIN = DEFERRED_REGISTRY.register("fire", () -> DragonBreed.builtInUnnamed(
            "fire",
            0x912400,
            0xff9819,
            Optional.of(ParticleTypes.FLAME),
            new DragonBreed.ModelProperties(false, false, false),
            ImmutableMap.of(),
            ImmutableList.of(),
            ImmutableList.of(
                    new NearbyBlocksHabitat(1, DMLTagCreator.createBlockTag(DragonMountsLegacy.id("fire_dragon_habitat_blocks"))),
                    new FluidHabitat(3, FluidTags.LAVA)),
            ImmutableSet.of("onFire", "inFire", "lava", "hotFloor"),
            Optional.empty()));
    public static final Supplier<Registry<DragonBreed>> REGISTRY = DEFERRED_REGISTRY.makeRegistry();
    public static final Codec<DragonBreed> CODEC = ResourceLocation.CODEC.xmap(BreedRegistry::get, DragonBreed::getRegistryName)
            .promotePartial(err -> DragonMountsLegacy.LOG.error("Unknown Dragon Breed Type: {}", err));

    public static DragonBreed get(String byString)
    {
        return get(new ResourceLocation(byString));
    }

    public static DragonBreed get(ResourceLocation byId)
    {
        var breed = registry().get(byId);
        if (breed == null) breed = getFallback(); // guard for if/when the registry is not defaulted...
        return breed;
    }

    public static DragonBreed getFallback()
    {
        return get(FIRE_BUILTIN.getId());
    }

    // the game instance isn't available in datagen
    public static Registry<DragonBreed> registry()
    {
        return (switch(FabricLoader.getInstance().getEnvironmentType())
                {
                    case CLIENT:
                    {
                        if (Minecraft.getInstance() != null && Minecraft.getInstance().level != null)
                            yield Minecraft.getInstance().level.registryAccess();
                    }
                    case SERVER:
                    {
                        if (ServerLifecycleHooks.getCurrentServer() != null)
                            yield ServerLifecycleHooks.getCurrentServer().registryAccess();
                    }
                    yield BuiltinRegistries.ACCESS;
                }
        ).registryOrThrow(REGISTRY_KEY);
    }

}
