package com.kay9.dragonmounts;

import com.kay9.dragonmounts.data.providers.BlockTagProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class DMLDataGen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        fabricDataGenerator.addProvider(BlockTagProvider::new);
        //fabricDataGenerator.addProvider(DragonBreedProvider::new);
    }
}
