package lod.thistlewood.item;

import lod.thistlewood.Thistlewood;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModPotions {
    //public static final RegistryEntry<Potion> HEALTH_BOOST_POTION = registerPotion("health_boost_potion",
    //        new Potion("health_boost_potion", new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 6000, 0)));

    private static RegistryEntry<Potion> registerPotion(String name, Potion potion) {
        return Registry.registerReference(Registries.POTION, Identifier.of(Thistlewood.MOD_ID, name), potion);
    }

    public static void registerPotions(){
    }
}
