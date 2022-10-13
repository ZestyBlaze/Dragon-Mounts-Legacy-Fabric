package com.kay9.dragonmounts.util;

import com.kay9.dragonmounts.mixins.client.ModelManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;

public class DMLModelManager {
    private static final ModelManager modelManager = Minecraft.getInstance().getModelManager();

    public static BakedModel getModel(ResourceLocation modelLocation) {
        return ((ModelManagerAccessor)modelManager).getBakedRegistry().getOrDefault(modelLocation, ((ModelManagerAccessor)modelManager).getMissingModel());
    }
}
