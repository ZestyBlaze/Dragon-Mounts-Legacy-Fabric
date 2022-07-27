package net.zestyblaze.dragonmounts.client;

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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.zestyblaze.dragonmounts.data.BreedManager;
import net.zestyblaze.dragonmounts.dragon.DMLEggBlock;
import net.zestyblaze.dragonmounts.dragon.DragonBreed;
import net.zestyblaze.dragonmounts.dragon.TameableDragon;
import net.zestyblaze.dragonmounts.utils.DMLModelManager;

import java.util.HashMap;
import java.util.Map;

public class DragonEggRenderer extends BlockEntityWithoutLevelRenderer implements BlockEntityRenderer<DMLEggBlock.Entity>, BuiltinItemRendererRegistry.DynamicItemRenderer {
    public static final DragonEggRenderer INSTANCE = new DragonEggRenderer();
    public static final Map<ResourceLocation, ResourceLocation> MODEL_CACHE = new HashMap<net.minecraft.resources.ResourceLocation, net.minecraft.resources.ResourceLocation>(8);

    public DragonEggRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void render(ItemStack stack, ItemTransforms.TransformType mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        DragonBreed breed = BreedManager.getFallback();
        CompoundTag tag = stack.getTagElement("BlockEntityTag");
        if (tag != null) breed = BreedManager.read(tag.getString(TameableDragon.NBT_BREED));
        renderEgg(matrices, vertexConsumers.getBuffer(Sheets.translucentItemSheet()), light, breed, false);
    }

    @Override
    public void render(DMLEggBlock.Entity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        renderEgg(poseStack, bufferSource.getBuffer(Sheets.translucentCullBlockSheet()), packedLight, blockEntity.getBreed(), false);
    }

    public static void renderEgg(PoseStack ps, VertexConsumer consumer, int light, DragonBreed breed, boolean offset) {
        ps.pushPose();
        if (offset) ps.translate(-0.5D, 0.0D, -0.5D);
        var model = DMLModelManager.getModel(MODEL_CACHE.get(breed.id()));
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ps.last(), consumer, null, model, 1, 1, 1, light, OverlayTexture.NO_OVERLAY);
        ps.popPose();
    }

    public static DragonEggRenderer instance() {
        return INSTANCE;
    }
}
