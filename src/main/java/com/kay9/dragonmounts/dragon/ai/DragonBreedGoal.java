package com.kay9.dragonmounts.dragon.ai;

import com.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.level.GameRules;

import java.util.List;

public class DragonBreedGoal extends BreedGoal {
    private final TameableDragon dragon;

    public DragonBreedGoal(TameableDragon animal) {
        super(animal, 1);
        this.dragon = animal;
    }

    @Override
    public boolean canUse() {
        if (!dragon.isAdult()) return false;
        if (!dragon.isInLove()) return false;
        else return (partner = getNearbyMate()) != null;
    }

    public TameableDragon getNearbyMate() {
        List<TameableDragon> list = level.getEntitiesOfClass(TameableDragon.class, dragon.getBoundingBox().inflate(8d));
        double dist = Double.MAX_VALUE;
        TameableDragon closest = null;

        for (TameableDragon entity : list)
        {
            if (dragon.canMate(entity) && dragon.distanceToSqr(entity) < dist)
            {
                closest = entity;
                dist = dragon.distanceToSqr(entity);
            }
        }

        return closest;
    }

    @Override
    protected void breed() {
        // Reset the "inLove" state for the animals
        animal.setAge(6000);
        partner.setAge(6000);

        animal.resetLove();
        partner.resetLove();
        dragon.spawnChildFromBreeding((ServerLevel) level, partner);
        level.broadcastEntityEvent(this.animal, (byte) 18);
        if (level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))
            level.addFreshEntity(new ExperienceOrb(level, animal.getX(), animal.getY(), animal.getZ(), animal.getRandom().nextInt(7) + 1));
    }
}
