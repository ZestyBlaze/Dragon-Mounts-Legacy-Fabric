package com.kay9.dragonmounts.client;

import com.kay9.dragonmounts.dragon.DMLEggBlock;
import com.kay9.dragonmounts.dragon.TameableDragon;
import com.kay9.dragonmounts.dragon.breed.BreedRegistry;
import com.kay9.dragonmounts.dragon.breed.DragonBreed;
import com.kay9.dragonmounts.util.DMLModelManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class DragonEggRenderer extends BlockEntityWithoutLevelRenderer implements BlockEntityRenderer<DMLEggBlock.Entity>, BuiltinItemRendererRegistry.DynamicItemRenderer
{
    public static final DragonEggRenderer INSTANCE = new DragonEggRenderer();
    public static final Map<ResourceLocation, ResourceLocation> MODEL_CACHE = new HashMap<>(8);

    public DragonEggRenderer()
    {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void render(DMLEggBlock.Entity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource buffer, int pPackedLight, int pPackedOverlay)
    {
        renderEgg(pPoseStack, buffer.getBuffer(Sheets.translucentCullBlockSheet()), pPackedLight, pBlockEntity.getBreed(), false);
    }

    @Override
    public void render(ItemStack stack, ItemTransforms.TransformType mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        ResourceLocation breed;
        var tag = stack.getTagElement("BlockEntityTag");
        if (tag == null || (breed = ResourceLocation.tryParse(tag.getString(TameableDragon.NBT_BREED))) == null)
            breed = BreedRegistry.FIRE_BUILTIN.getId();
        renderEgg(matrices, vertexConsumers.getBuffer(Sheets.translucentItemSheet()), light, breed, false);
    }

    /**
     * Use this if you have a breed instance readily available.
     */
    public static void renderEgg(PoseStack ps, VertexConsumer consumer, int light, DragonBreed breed, boolean offset)
    {
        renderEgg(ps, consumer, light, breed.getRegistryName(), offset);
    }

    /**
     * Use this to avoid querying the breed registry every frame, if needed, as I'd imagine it's expensive...
     */
    public static void renderEgg(PoseStack ps, VertexConsumer consumer, int light, ResourceLocation breed, boolean offset)
    {
        ps.pushPose();
        if (offset) ps.translate(-0.5D, 0.0D, -0.5D);
        var model = DMLModelManager.getModel(MODEL_CACHE.get(breed));
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ps.last(), consumer, null, model, 1, 1, 1, light, OverlayTexture.NO_OVERLAY);
        ps.popPose();
    }

    /**
     * Here to provide a way to create a method reference factory to return the instance.
     * Still confused? I'm exploiting javas shitty class loading ways.
     *
     * Don't use this as a getter. Simply use the public field.
     */
    @SuppressWarnings("unused")
    @NotNull
    public static DragonEggRenderer instance()
    {
        return INSTANCE;
    }
}
