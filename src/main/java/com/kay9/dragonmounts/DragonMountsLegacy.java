package com.kay9.dragonmounts;

import com.kay9.dragonmounts.client.DragonEggRenderer;
import com.kay9.dragonmounts.dragon.TameableDragon;
import com.kay9.dragonmounts.dragon.breed.BreedRegistry;
import eu.midnightdust.lib.config.MidnightConfig;
import io.github.fabricators_of_create.porting_lib.event.client.CameraSetupCallback;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public class DragonMountsLegacy implements ModInitializer {
	public static final String MOD_ID = "dragonmounts";
	public static final Logger LOG = LogManager.getLogger();

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}

	private static void cameraAngles(Camera camera)
	{
		if (Minecraft.getInstance().player.getVehicle() instanceof TameableDragon)
		{
			var distance = 0;
			var vertical = 0;
			switch (Minecraft.getInstance().options.getCameraType())
			{
				case THIRD_PERSON_FRONT -> distance = 6;
				case THIRD_PERSON_BACK -> {
					distance = 6;
					vertical = 3;
				}
			}
			camera.move(-distance, vertical, 0);
		}
	}

	@Override
	public void onInitialize() {
		MidnightConfig.init(MOD_ID, DMLConfig.class);
		BreedRegistry.DEFERRED_REGISTRY.register();
		DMLRegistry.register();

		CameraSetupCallback.EVENT.register((cameraInfo -> {
			cameraAngles(cameraInfo.camera);
			return true;
		}));
	}
}
