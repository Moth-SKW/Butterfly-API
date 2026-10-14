package moth.butterflyapi.client.particle;

import moth.butterflyapi.bootstrap.Bootstrap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
//? if <1.21.11 {
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
//?} else {
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
//?}
import moth.butterflyapi.ButterflyApi;
import net.minecraft.server.packs.PackType;

@Environment(EnvType.CLIENT)
public final class ClientParticleBootstrap {
    private static final Bootstrap BOOTSTRAP = Bootstrap.create();

    private ClientParticleBootstrap() {
    }

    public static void initialize() {
        BOOTSTRAP.run(
                ClientParticleBootstrap::registerResourceReload,
                ClientParticleBootstrap::registerLifecycleHooks
        );
    }

    private static void registerResourceReload() {
//? if <1.21.11 {
/*
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener (new ParticleDefinitionReloadListener());
*///?} else {
        ResourceLoader.get(PackType.CLIENT_RESOURCES)
                .registerReloader(ButterflyApi.id("particle_definitions"), new ParticleDefinitionReloadListener());
//?}
    }

    private static void registerLifecycleHooks() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientParticleManager::tick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (client.isSameThread()) {
                ClientParticleManager.clear();
            } else {
                client.execute(ClientParticleManager::clear);
            }
        });
    }
}
