package net.zestyblaze.dragonmounts.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.zestyblaze.dragonmounts.DragonMountsLegacy;
import net.zestyblaze.dragonmounts.dragon.DragonBreed;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BreedManager extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {
    private static Map<ResourceLocation, DragonBreed> registry = Collections.emptyMap();
    private static DragonBreed fallback = DragonBreed.FIRE;

    public static final BreedManager INSTANCE = new BreedManager();
    public static final Codec<DragonBreed> CODEC = Codec.STRING.xmap(BreedManager::read, b -> b.id().toString());

    protected BreedManager() {
        super(new GsonBuilder().create(), "dragon_breeds");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        populate(r -> elements.forEach((key, json) ->
                DragonBreed.CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(m -> DragonMountsLegacy.LOG.warn("Failed to load DragonBreed with id '{}': {}", key, m))
                        .ifPresent(r)));
        DragonMountsLegacy.LOG.info("Loaded {} Dragon Breeds", registry.size());
    }

    public static void populate(Consumer<Consumer<DragonBreed>> registryFunc) {
        var builder = new HashMap<ResourceLocation, DragonBreed>();
        registryFunc.accept(d -> builder.put(d.id(), d));
        fallback = builder.computeIfAbsent(DragonBreed.FIRE.id(), i -> DragonBreed.FIRE); // Ensure one entry exists in the registry, for safety purposes.
        registry = ImmutableMap.copyOf(builder);
    }

    @Nonnull
    public static DragonBreed getFallback() {
        return fallback;
    }

    @Nullable
    public static DragonBreed getNullable(ResourceLocation breed) {
        return registry.get(breed);
    }

    public static DragonBreed get(ResourceLocation breed) {
        DragonBreed result = getNullable(breed);
        return result != null? result : getFallback(); // fallback to fire if a breed ends up missing (due to removal of a type, or other extravagent error I can't think of)
    }

    public static DragonBreed read(String breed) {
        return get(ResourceLocation.tryParse(breed));
    }

    public static Collection<DragonBreed> getBreeds() {
        return registry.values();
    }

    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation(DragonMountsLegacy.MOD_ID, "breed_manager");
    }
}
