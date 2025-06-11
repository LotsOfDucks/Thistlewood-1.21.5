package lod.thistlewood.entity.damage;

import lod.thistlewood.Thistlewood;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModDamageTypes {
    public static final RegistryKey<DamageType> FISSION = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(Thistlewood.MOD_ID, "fission"));
}
