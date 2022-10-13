package com.kay9.dragonmounts.abilities;

import com.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.core.BlockPos;

public abstract class FootprintAbility implements Ability {
    @Override
    public void onMove(TameableDragon dragon) {
        if (dragon.level.isClientSide || !dragon.isAdult() || !dragon.isOnGround()) return;

        var chance = getFootprintChance(dragon);
        if (chance == 0) return;

        for (int i = 0; i < 4; i++)
        {
            // place only if randomly selected
            if (dragon.getRandom().nextFloat() > chance) continue;

            // get footprint position
            double bx = dragon.getX() + (i % 2 * 2 - 1) * 0.25f;
            double by = dragon.getY() + 0.5;
            double bz = dragon.getZ() + (i / 2f % 2 * 2 - 1) * 0.25f;
            var pos = new BlockPos(bx, by, bz);

            placeFootprint(dragon, pos);
        }
    }

    protected float getFootprintChance(TameableDragon dragon) {
        return 0.05f;
    }

    protected abstract void placeFootprint(TameableDragon dragon, BlockPos pos);
}
