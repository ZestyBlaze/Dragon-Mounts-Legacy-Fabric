package net.zestyblaze.dragonmounts;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.zestyblaze.dragonmounts.dragon.DMLEggBlock;
import net.zestyblaze.dragonmounts.dragon.TameableDragon;
import net.zestyblaze.dragonmounts.network.UpdateBreedsPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DragonMountsLegacy implements ModInitializer {
	public static final String MOD_ID = "dragonmounts";
	public static final Logger LOG = LogManager.getLogger();

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}

	private static void attemptVanillaEggReplacement() {
		UseBlockCallback.EVENT.register(((player, world, hand, hitResult) -> {
			if(DMLEggBlock.overrideVanillaDragonEgg(world, hitResult.getBlockPos(), player)) {
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.FAIL;
		}));
	}

	@SuppressWarnings("ConstantConditions")
	private static void cameraAngles(Camera camera) {
		if (Minecraft.getInstance().player.getVehicle() instanceof TameableDragon) {
			var distance = 0;
			var vertical = 0;
			switch (Minecraft.getInstance().options.getCameraType()) {
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
		DMLRegistry.registerBlocks();
		DMLRegistry.registerItems();
		DMLRegistry.registerSounds();
		DMLRegistry.registerAttributes();
		attemptVanillaEggReplacement();

		UpdateBreedsPacket.init();
	}
}
