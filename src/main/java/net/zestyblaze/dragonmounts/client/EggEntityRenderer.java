package net.zestyblaze.dragonmounts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.zestyblaze.dragonmounts.DMLRegistry;
import net.zestyblaze.dragonmounts.dragon.DragonEgg;

public class EggEntityRenderer extends EntityRenderer<DragonEgg> {
    public EggEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(DragonEgg entity, float yaw, float partialTicks, PoseStack ps, MultiBufferSource buffer, int light) {
        BlockPos blockpos = entity.blockPosition();
        BlockState state = DMLRegistry.EGG_BLOCK.defaultBlockState();

        if (state != entity.level.getBlockState(blockpos)) {
            DragonEggRenderer.renderEgg(ps, buffer.getBuffer(Sheets.translucentCullBlockSheet()), light, entity.breed, true);
            super.render(entity, yaw, partialTicks, ps, buffer, light);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(DragonEgg entity) {
        return null;
    }
}
