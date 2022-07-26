package net.zestyblaze.dragonmounts.dragon;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.zestyblaze.dragonmounts.DMLRegistry;
import net.zestyblaze.dragonmounts.data.BreedManager;

public class DragonSpawnEgg extends SpawnEggItem {
    public DragonSpawnEgg() {
        super(DMLRegistry.DRAGON, 0, 0, new FabricItemSettings().group(CreativeModeTab.TAB_MISC));
    }

    @Override
    public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> pItems) {
        if (allowedIn(pCategory)) {
            for (DragonBreed breed : BreedManager.getBreeds()) pItems.add(create(breed));
        }
    }

    public static ItemStack create(DragonBreed breed) {
        CompoundTag root = new CompoundTag();

        // entity tag
        CompoundTag entityTag = new CompoundTag();
        entityTag.putString(TameableDragon.NBT_BREED, breed.id().toString());
        root.put(EntityType.ENTITY_TAG, entityTag);

        // name & colors
        // storing these in the stack nbt is more performant than getting the breed everytime
        CompoundTag itemDataTag = new CompoundTag();
        itemDataTag.putString("ItemName", breed.getTranslationKey());
        itemDataTag.putInt("PrimaryColor", breed.primaryColor());
        itemDataTag.putInt("SecondaryColor", breed.secondaryColor());
        root.put("ItemData", itemDataTag);

        ItemStack stack = new ItemStack(DMLRegistry.SPAWN_EGG);
        stack.setTag(root);
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        String name = BreedManager.getFallback().getTranslationKey();
        CompoundTag tag = stack.getTagElement("ItemData");
        if (tag != null) name = tag.getString("ItemName");
        return Component.translatable(getDescriptionId(), Component.translatable(name));
    }

    public static int getColor(ItemStack stack, int tintIndex) {
        CompoundTag tag = stack.getTagElement("ItemData");
        if (tag != null) return tintIndex == 0? tag.getInt("PrimaryColor") : tag.getInt("SecondaryColor");
        return tintIndex == 0? BreedManager.getFallback().primaryColor() : BreedManager.getFallback().secondaryColor();
    }
}
