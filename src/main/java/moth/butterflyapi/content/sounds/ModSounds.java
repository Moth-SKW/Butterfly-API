package moth.butterflyapi.content.sounds;

import moth.butterflyapi.content.ContentBootstrap;
import moth.butterflyapi.content.blocks.Plushes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ModSounds {
    private static boolean initialized = false;

    public static SoundEvent MATCHSTICK;

    private ModSounds() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        MATCHSTICK = registerSimpleSound("matchstick");
    }

    public static SoundEvent plushHonk(String plushId) {
        return Plushes.registered(plushId).sound();
    }

    private static SoundEvent registerSimpleSound(String soundPath) {
        Identifier id = new Identifier(ContentBootstrap.MOD_ID, soundPath);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}
