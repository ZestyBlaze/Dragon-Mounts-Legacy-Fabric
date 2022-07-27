package net.zestyblaze.dragonmounts.utils;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class DMLBlockTags {
    public static TagKey<Block> create(ResourceLocation name) {
        return TagKey.create(Registry.BLOCK_REGISTRY, name);
    }
}
