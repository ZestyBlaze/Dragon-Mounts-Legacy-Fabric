package com.kay9.dragonmounts.dragon;

import com.kay9.dragonmounts.DMLRegistry;
import com.kay9.dragonmounts.dragon.breed.BreedRegistry;
import com.kay9.dragonmounts.dragon.breed.DragonBreed;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

public class DragonSpawnEgg extends SpawnEggItem
{
    public DragonSpawnEgg()
    {
        super(DMLRegistry.DRAGON, 0, 0, new FabricItemSettings().group(CreativeModeTab.TAB_MISC));
    }

    @Override
    public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> pItems)
    {
        if (allowedIn(pCategory))
        {
            for (DragonBreed breed : BreedRegistry.registry()) pItems.add(create(breed));
        }
    }

    public static ItemStack create(DragonBreed breed)
    {
        CompoundTag root = new CompoundTag();

        // entity tag
        CompoundTag entityTag = new CompoundTag();
        entityTag.putString(TameableDragon.NBT_BREED, breed.getRegistryName().toString());
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
    public Component getName(ItemStack stack)
    {
        String name;
        var tag = stack.getTagElement("ItemData");
        if (tag == null || (name = tag.getString("ItemName")).isEmpty())
            name = BreedRegistry.getFallback().getTranslationKey();
        return Component.translatable(getDescriptionId(), Component.translatable(name));
    }

    public static int getColor(ItemStack stack, int tintIndex)
    {
        int prim;
        int sec;
        var tag = stack.getTagElement("ItemData");
        if (tag != null)
        {
            prim = tag.getInt("PrimaryColor");
            sec = tag.getInt("SecondaryColor");
        }
        else
        {
            var fire = BreedRegistry.getFallback();
            prim = fire.primaryColor();
            sec = fire.secondaryColor();
        }

        return tintIndex == 0? prim : sec;
    }
}
