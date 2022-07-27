package net.zestyblaze.dragonmounts.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.zestyblaze.dragonmounts.mixins.ModelManagerAccessor;

public class DMLModelManager {
    private static final ModelManager modelManager = Minecraft.getInstance().getModelManager();

    public static BakedModel getModel(ResourceLocation modelLocation) {
        return ((ModelManagerAccessor)modelManager).getBakedRegistry().getOrDefault(modelLocation, ((ModelManagerAccessor)modelManager).getMissingModel());
    }
}
