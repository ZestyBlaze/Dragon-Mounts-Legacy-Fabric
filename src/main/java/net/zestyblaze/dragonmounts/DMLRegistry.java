package net.zestyblaze.dragonmounts;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.zestyblaze.dragonmounts.dragon.DMLEggBlock;
import net.zestyblaze.dragonmounts.dragon.DragonEgg;
import net.zestyblaze.dragonmounts.dragon.DragonSpawnEgg;
import net.zestyblaze.dragonmounts.dragon.TameableDragon;

public class DMLRegistry {
    public static final Block EGG_BLOCK = new DMLEggBlock();

    public static final BlockItem EGG_BLOCK_ITEM = new DMLEggBlock.Item();
    public static final Item SPAWN_EGG = new DragonSpawnEgg();

    public static final SoundEvent DRAGON_BREATHE_SOUND = new SoundEvent(new ResourceLocation(DragonMountsLegacy.MOD_ID, "entity.dragon.breathe"));
    public static final SoundEvent DRAGON_STEP_SOUND = new SoundEvent(new ResourceLocation(DragonMountsLegacy.MOD_ID, "entity.dragon.step"));
    public static final SoundEvent DRAGON_DEATH_SOUND = new SoundEvent(new ResourceLocation(DragonMountsLegacy.MOD_ID, "entity.dragon.death"));

    public static final EntityType<TameableDragon> DRAGON = Registry.register(Registry.ENTITY_TYPE, new ResourceLocation(DragonMountsLegacy.MOD_ID, "dragon"), EntityType.Builder.of(TameableDragon::new, MobCategory.CREATURE).sized(TameableDragon.BASE_WIDTH, TameableDragon.BASE_HEIGHT).clientTrackingRange(10).updateInterval(3).build("dragon"));
    public static final EntityType<DragonEgg> DRAGON_EGG = Registry.register(Registry.ENTITY_TYPE, new ResourceLocation(DragonMountsLegacy.MOD_ID, "dragon_egg"), EntityType.Builder.of((EntityType.EntityFactory<DragonEgg>)DragonEgg::new, MobCategory.MISC).sized(DragonEgg.WIDTH, DragonEgg.HEIGHT).clientTrackingRange(5).updateInterval(8).build("dragon_egg"));

    public static final BlockEntityType<DMLEggBlock.Entity> EGG_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation(DragonMountsLegacy.MOD_ID, "dragon_egg"), FabricBlockEntityTypeBuilder.create(DMLEggBlock.Entity::new, EGG_BLOCK).build(null));

    public static void registerBlocks() {
        Registry.register(Registry.BLOCK, new ResourceLocation(DragonMountsLegacy.MOD_ID, "dragon_egg"), EGG_BLOCK);
    }

    public static void registerItems() {
        Registry.register(Registry.ITEM, new ResourceLocation(DragonMountsLegacy.MOD_ID, "dragon_egg"), EGG_BLOCK_ITEM);
        Registry.register(Registry.ITEM, new ResourceLocation(DragonMountsLegacy.MOD_ID, "dragon_spawn_egg"), SPAWN_EGG);
    }

    public static void registerSounds() {
        Registry.register(Registry.SOUND_EVENT, new ResourceLocation(DragonMountsLegacy.MOD_ID, "entity.dragon.breathe"), DRAGON_BREATHE_SOUND);
        Registry.register(Registry.SOUND_EVENT, new ResourceLocation(DragonMountsLegacy.MOD_ID, "entity.dragon.step"), DRAGON_STEP_SOUND);
        Registry.register(Registry.SOUND_EVENT, new ResourceLocation(DragonMountsLegacy.MOD_ID, "entity.dragon.death"), DRAGON_DEATH_SOUND);
    }

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(DRAGON, TameableDragon.createAttributes());
    }
}
