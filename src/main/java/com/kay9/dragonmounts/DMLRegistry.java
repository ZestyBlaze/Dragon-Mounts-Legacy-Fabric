package com.kay9.dragonmounts;

import com.kay9.dragonmounts.dragon.DMLEggBlock;
import com.kay9.dragonmounts.dragon.DragonEgg;
import com.kay9.dragonmounts.dragon.DragonSpawnEgg;
import com.kay9.dragonmounts.dragon.TameableDragon;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class DMLRegistry {
    public static final Block EGG_BLOCK = new DMLEggBlock();

    public static final Item EGG_BLOCK_ITEM = new DMLEggBlock.Item();
    public static final Item SPAWN_EGG = new DragonSpawnEgg();

    public static final SoundEvent DRAGON_BREATHE_SOUND = new SoundEvent(new ResourceLocation(DragonMountsLegacy.MOD_ID, "entity.dragon.breathe"));
    public static final SoundEvent DRAGON_STEP_SOUND = new SoundEvent(new ResourceLocation(DragonMountsLegacy.MOD_ID, "entity.dragon.step"));
    public static final SoundEvent DRAGON_DEATH_SOUND = new SoundEvent(new ResourceLocation(DragonMountsLegacy.MOD_ID, "entity.dragon.death"));

    public static final EntityType<TameableDragon> DRAGON = FabricEntityTypeBuilder.create(MobCategory.CREATURE, TameableDragon::new).dimensions(EntityDimensions.scalable(TameableDragon.BASE_WIDTH, TameableDragon.BASE_HEIGHT)).build();
    public static final EntityType<DragonEgg> DRAGON_EGG = FabricEntityTypeBuilder.create(MobCategory.MISC, DragonEgg::new).dimensions(EntityDimensions.scalable(DragonEgg.WIDTH, DragonEgg.HEIGHT)).build();

    public static final BlockEntityType<DMLEggBlock.Entity> EGG_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(DMLEggBlock.Entity::new, EGG_BLOCK).build();

    public static void register() {
        Registry.register(Registry.BLOCK, new ResourceLocation(DragonMountsLegacy.MOD_ID, "dragon_egg"), EGG_BLOCK);
        Registry.register(Registry.ITEM, new ResourceLocation(DragonMountsLegacy.MOD_ID, "dragon_egg"), EGG_BLOCK_ITEM);
        Registry.register(Registry.ITEM, new ResourceLocation(DragonMountsLegacy.MOD_ID, "spawn_egg"), SPAWN_EGG);
        Registry.register(Registry.SOUND_EVENT, new ResourceLocation(DragonMountsLegacy.MOD_ID, "entity.dragon.breathe"), DRAGON_BREATHE_SOUND);
        Registry.register(Registry.SOUND_EVENT, new ResourceLocation(DragonMountsLegacy.MOD_ID, "entity.dragon.step"), DRAGON_STEP_SOUND);
        Registry.register(Registry.SOUND_EVENT, new ResourceLocation(DragonMountsLegacy.MOD_ID, "entity.dragon.death"), DRAGON_DEATH_SOUND);
        Registry.register(Registry.ENTITY_TYPE, new ResourceLocation(DragonMountsLegacy.MOD_ID, "dragon"), DRAGON);
        Registry.register(Registry.ENTITY_TYPE, new ResourceLocation(DragonMountsLegacy.MOD_ID, "dragon_egg"), DRAGON_EGG);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation(DragonMountsLegacy.MOD_ID, "dragon_egg"), EGG_BLOCK_ENTITY);
        FabricDefaultAttributeRegistry.register(DRAGON, TameableDragon.createAttributes());
    }
}
