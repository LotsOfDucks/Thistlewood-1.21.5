package lod.thistlewood.sound;

import lod.thistlewood.Thistlewood;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {

    private static SoundEvent registerSoundEvents(String name) {
        Identifier id = Identifier.of(Thistlewood.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void initialize() {}
}

