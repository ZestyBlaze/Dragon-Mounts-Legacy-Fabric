package net.zestyblaze.dragonmounts.mixins;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.zestyblaze.dragonmounts.DMLRegistry;
import net.zestyblaze.dragonmounts.dragon.DragonEgg;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Shadow private ClientLevel level;

    @Inject(method = "handleAddEntity", at = @At("TAIL"))
    private void handleSpellSpawn(ClientboundAddEntityPacket packet, CallbackInfo ci) {
        EntityType<?> entityType = packet.getType();
        Entity entity = null;

        if(entityType == DMLRegistry.DRAGON_EGG) {
            entity = new DragonEgg(level);
        }

        if(entity != null) {
            double x = packet.getX();
            double y = packet.getY();
            double z = packet.getZ();
            float yaw = (byte) Mth.floor(packet.getYRot() * 256.0F / 360.0F);
            float pitch = (byte) Mth.floor(packet.getXRot() * 256.0F / 360.0F);
            float headYaw = (byte) (entity.getYHeadRot() * 256.0F / 360.0F);
            entity.setId(packet.getId());
            entity.setUUID(packet.getUUID());
            entity.syncPacketPositionCodec(x, y, z);
            entity.absMoveTo(x, y, z, (yaw * 360) / 256.0F, (pitch * 360) / 256.0F);
            entity.setYHeadRot((headYaw * 360) / 256.0F);
            entity.setYBodyRot((headYaw * 360) / 256.0F);
            level.putNonPlayerEntity(packet.getId(), entity);
        }
    }
}
