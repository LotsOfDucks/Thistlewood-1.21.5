package lod.thistlewood.item;

import lod.thistlewood.Thistlewood;
import lod.thistlewood.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {

    public static final ItemGroup THISTLEWOOD_MISC_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(Thistlewood.MOD_ID, "thistlewood_misc"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModBlocks.CREEPING_THISTLE))
                    .displayName(Text.translatable("itemgroup.thistlewood.misc"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModBlocks.CREEPING_THISTLE);
                        entries.add(ModBlocks.DEAD_CREEPING_THISTLE);
                    }).build());

    public static void initialize() {
    }
}
