package com.kay9.dragonmounts;

import com.kay9.dragonmounts.client.DragonEggRenderer;
import com.kay9.dragonmounts.client.DragonModel;
import com.kay9.dragonmounts.client.DragonRenderer;
import com.kay9.dragonmounts.client.EggEntityRenderer;
import com.kay9.dragonmounts.dragon.DragonSpawnEgg;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class DMLClient implements ClientModInitializer {
    public static final BooleanSupplier FLIGHT_DESCENT_KEY = keymap("flight_descent", GLFW.GLFW_KEY_Z, "key.categories.movement");

    private static BooleanSupplier keymap(String name, int defaultMapping, String category) {
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && Minecraft.getInstance() != null) {
            var keymap = new KeyMapping(String.format("key.%s.%s", DragonMountsLegacy.MOD_ID, name), defaultMapping, category);
            KeyBindingHelper.registerKeyBinding(keymap);
            return keymap::isDown;
        }
        return () -> {
            throw new RuntimeException("Cannot invoke '" + name + "' key mapping if Minecraft is null");
        };
    }

    private static void defineBlockModels(Consumer<ResourceLocation> registry) {
        var dir = "models/block/dragon_eggs";
        var length = "models/".length();
        var suffixLength = ".json".length();
        for (var rl : Minecraft.getInstance().getResourceManager().listResources(dir, f -> f.getPath().endsWith(".json")).keySet()) {
            var path = rl.getPath();
            path = path.substring(length, path.length() - suffixLength);
            var model = new ResourceLocation(rl.getNamespace(), path);
            String id = path.substring("block/dragon_eggs/".length(), path.length() - "_dragon_egg".length());

            registry.accept(model);
            DragonEggRenderer.MODEL_CACHE.put(new ResourceLocation(rl.getNamespace(), id), model);
        }
    }

    @Override
    public void onInitializeClient() {
        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> defineBlockModels(out));
        EntityRendererRegistry.register(DMLRegistry.DRAGON, DragonRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(DragonRenderer.LAYER_LOCATION, DragonModel::createBodyLayer);
        EntityRendererRegistry.register(DMLRegistry.DRAGON_EGG, EggEntityRenderer::new);
        BuiltinItemRendererRegistry.INSTANCE.register(DMLRegistry.EGG_BLOCK, DragonEggRenderer.instance());
        ColorProviderRegistry.ITEM.register(DragonSpawnEgg::getColor, DMLRegistry.SPAWN_EGG);
    }
}
