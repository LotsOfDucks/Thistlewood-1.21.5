package lod.thistlewood.block;

import lod.thistlewood.Thistlewood;
import lod.thistlewood.block.custom.BlackberryCropBlock;
import lod.thistlewood.block.custom.CreepingThistleBlock;
import lod.thistlewood.block.custom.DeadCreepingThistleBlock;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.function.Function;

public class ModBlocks {

    private static Block register(String name, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings settings, boolean shouldRegisterItem) {
        RegistryKey<Block> blockKey = keyOfBlock(name);
        Block block = blockFactory.apply(settings.registryKey(blockKey));

        if (shouldRegisterItem) {
           RegistryKey<Item> itemKey = keyOfItem(name);

            BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey));
            Registry.register(Registries.ITEM, itemKey, blockItem);
        }

        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    private static Block registerRare(String name, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings settings, boolean shouldRegisterItem) {
        RegistryKey<Block> blockKey = keyOfBlock(name);
        Block block = blockFactory.apply(settings.registryKey(blockKey));

        if (shouldRegisterItem) {
            RegistryKey<Item> itemKey = keyOfItem(name);

            BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey).rarity(Rarity.RARE));
            Registry.register(Registries.ITEM, itemKey, blockItem);
        }

        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    private static RegistryKey<Block> keyOfBlock(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Thistlewood.MOD_ID, name));
    }

    private static RegistryKey<Item> keyOfItem(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Thistlewood.MOD_ID, name));
    }

    public static final Block CREEPING_THISTLE = register(
            "creeping_thistle",
            CreepingThistleBlock::new,
            AbstractBlock.Settings.create().mapColor((state) -> state.get(CreepingThistleBlock.GROWINGVIS) ? MapColor.GREEN : MapColor.PALE_YELLOW).replaceable().noCollision().strength(0.8F).requiresTool().sounds(BlockSoundGroup.MANGROVE_ROOTS).burnable().pistonBehavior(PistonBehavior.DESTROY),
            true
    );

    public static final Block DEAD_CREEPING_THISTLE = register(
            "dead_creeping_thistle",
            DeadCreepingThistleBlock::new,
            AbstractBlock.Settings.create().mapColor(MapColor.PALE_YELLOW).replaceable().noCollision().strength(0.8F).requiresTool().sounds(BlockSoundGroup.MANGROVE_ROOTS).burnable().pistonBehavior(PistonBehavior.DESTROY),
            true
    );

    public static final Block BLACKBERRY_CROP = register(
            "blackberry_crop",
            BlackberryCropBlock::new,
            AbstractBlock.Settings.create().mapColor(MapColor.GREEN).offset(AbstractBlock.OffsetType.XYZ).noCollision().strength(0.8F).requiresTool().sounds(BlockSoundGroup.MANGROVE_ROOTS).burnable().pistonBehavior(PistonBehavior.DESTROY),
            false
    );

    public static void initialize() {
        FireBlock fireBlock = (FireBlock) Blocks.FIRE;
        fireBlock.registerFlammableBlock(ModBlocks.CREEPING_THISTLE, 30, 60);
        fireBlock.registerFlammableBlock(ModBlocks.DEAD_CREEPING_THISTLE, 30, 60);
        fireBlock.registerFlammableBlock(ModBlocks.BLACKBERRY_CROP, 30, 60);
    }
}
