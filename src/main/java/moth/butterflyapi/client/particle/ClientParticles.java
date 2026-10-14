package moth.butterflyapi.client.particle;

import moth.butterflyapi.particle.ParticleDefinitions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public final class ClientParticles {
    private ClientParticles() {
    }

    public static boolean spawn(Identifier definition, Vec3 position) {
        return ClientParticleManager.spawn(definition, position);
    }

    public static boolean spawn(Identifier definition, double x, double y, double z) {
        return spawn(definition, new Vec3(x, y, z));
    }

    public static boolean isDefined(Identifier definition) {
        return ParticleDefinitions.contains(definition);
    }

    public static int activeEmitterCount() {
        return ClientParticleManager.activeEmitterCount();
    }

    public static int activeParticleCount() {
        return ClientParticleManager.activeParticleCount();
    }

    public static void clear() {
        ClientParticleManager.clear();
    }
}
