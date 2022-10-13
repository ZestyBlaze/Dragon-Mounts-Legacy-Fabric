package com.kay9.dragonmounts.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class DMLTagCreator {
    public static TagKey<Item> createItemTag(ResourceLocation name) {
        return TagKey.create(Registry.ITEM_REGISTRY, name);
    }

    public static TagKey<Block> createBlockTag(ResourceLocation name) {
        return TagKey.create(Registry.BLOCK_REGISTRY, name);
    }
}
