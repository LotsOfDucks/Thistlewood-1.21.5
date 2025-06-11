package lod.thistlewood;

import lod.thistlewood.block.ModBlocks;
import lod.thistlewood.entity.ModBlockEntityTypes;
import lod.thistlewood.item.*;
import lod.thistlewood.sound.ModSounds;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Thistlewood implements ModInitializer {
	public static final String MOD_ID = "thistlewood";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItemGroups.initialize();
		ModItems.initialize();
		ModBlocks.initialize();
		ModBlockEntityTypes.initialize();
		ModSounds.initialize();
		ModFoodComponents.initialize();
		ModPotions.registerPotions();
	}
}