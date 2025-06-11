package lod.thistlewood.item;

import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public final class ModItems {
    private ModItems() {
    }

    private static Function<Item.Settings, Item> createBlockItemWithUniqueName(Block block) {
        return (settings) -> new BlockItem(block, settings.useItemPrefixedTranslationKey());
    }

    //public static final Item CUSTOM_ITEM = register("custom_item", Item::new, new Item.Settings());

    public static Item register(String path, Function<Item.Settings, Item> factory, Item.Settings settings) {
        final RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of("thistlewood", path));
        return Items.register(registryKey, factory, settings);
    }
    public static Item registerTool(String path, Function<Item.Settings, Item> factory) {
        final RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of("thistlewood", path));
        return Items.register(registryKey, factory);
    }

    public static void initialize() {
    }
}
