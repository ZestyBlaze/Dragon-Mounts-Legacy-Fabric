package com.kay9.dragonmounts.abilities;

import com.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public class FrostWalkerAbility implements Ability {
    public static final FrostWalkerAbility INSTANCE = new FrostWalkerAbility();
    public static final Codec<FrostWalkerAbility> CODEC = Codec.unit(INSTANCE);

    @Override
    public void initialize(TameableDragon dragon) {
        dragon.setPathfindingMalus(BlockPathTypes.WATER, 0);
    }

    @Override
    public void close(TameableDragon dragon) {
        dragon.setPathfindingMalus(BlockPathTypes.WATER, BlockPathTypes.WATER.getMalus());
    }

    @Override
    public void onMove(TameableDragon dragon) {
        if (!dragon.level.isClientSide() && dragon.isAdult())
            FrostWalkerEnchantment.onEntityMoved(dragon, dragon.level, dragon.blockPosition(), (int) Math.max(3 * dragon.getScale(), 1));
    }

    @Override
    public String type() {
        return Ability.FROST_WALKER;
    }
}
