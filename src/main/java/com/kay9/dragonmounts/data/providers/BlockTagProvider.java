package com.kay9.dragonmounts.data.providers;

import com.kay9.dragonmounts.DragonMountsLegacy;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.Registry;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class BlockTagProvider extends FabricTagProvider<Block> {
    public BlockTagProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator, Registry.BLOCK);
    }

    public static final TagKey<Block> FIRE_DRAGON_HABITAT_BLOCKS = TagKey.create(Registry.BLOCK_REGISTRY, DragonMountsLegacy.id("fire_dragon_habitat_blocks"));
    public static final TagKey<Block> FOREST_DRAGON_HABITAT_BLOCKS = TagKey.create(Registry.BLOCK_REGISTRY, DragonMountsLegacy.id("forest_dragon_habitat_blocks"));
    public static final TagKey<Block> ICE_DRAGON_HABITAT_BLOCKS = TagKey.create(Registry.BLOCK_REGISTRY, DragonMountsLegacy.id("ice_dragon_habitat_blocks"));
    public static final TagKey<Block> NETHER_DRAGON_HABITAT_BLOCKS = TagKey.create(Registry.BLOCK_REGISTRY, DragonMountsLegacy.id("nether_dragon_habitat_blocks"));

    @Override
    protected void generateTags() {
        getOrCreateTagBuilder(FIRE_DRAGON_HABITAT_BLOCKS).add(Blocks.FIRE, Blocks.LAVA, Blocks.MAGMA_BLOCK, Blocks.CAMPFIRE);
        getOrCreateTagBuilder(FOREST_DRAGON_HABITAT_BLOCKS).addTags(BlockTags.LEAVES, BlockTags.SAPLINGS, BlockTags.FLOWERS).add(Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET, Blocks.VINE);
        getOrCreateTagBuilder(ICE_DRAGON_HABITAT_BLOCKS).addTags(BlockTags.ICE, BlockTags.SNOW);
        getOrCreateTagBuilder(NETHER_DRAGON_HABITAT_BLOCKS).addOptionalTag(BlockTags.SOUL_FIRE_BASE_BLOCKS).add(Blocks.BLACKSTONE, Blocks.BASALT, Blocks.SOUL_FIRE, Blocks.SOUL_CAMPFIRE);
    }
}
