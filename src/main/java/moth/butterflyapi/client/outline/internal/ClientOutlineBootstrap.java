package moth.butterflyapi.client.outline.internal;

import moth.butterflyapi.bootstrap.Bootstrap;
import moth.butterflyapi.outline.internal.network.BlockEntityOutlineUpdateS2CPacket;
import moth.butterflyapi.outline.internal.network.EntityOutlineUpdateS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public final class ClientOutlineBootstrap {
    private static final Bootstrap BOOTSTRAP = Bootstrap.create();

    private ClientOutlineBootstrap() {
    }

    public static void initialize() {
        BOOTSTRAP.run(
                ClientOutlineBootstrap::registerReceivers,
                ClientOutlineBootstrap::registerLifecycleHooks,
                BlockOutlineGeometryRenderer::initialize
        );
    }

    private static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(
                EntityOutlineUpdateS2CPacket.ID,
                (packet, context) -> ClientOutlineManager.accept(packet)
        );
        ClientPlayNetworking.registerGlobalReceiver(
                BlockEntityOutlineUpdateS2CPacket.ID,
                (packet, context) -> ClientOutlineManager.accept(packet)
        );
    }

    private static void registerLifecycleHooks() {
        ClientEntityEvents.ENTITY_LOAD.register(ClientOutlineManager::onEntityLoad);
        ClientEntityEvents.ENTITY_UNLOAD.register(ClientOutlineManager::onEntityUnload);
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register(ClientOutlineManager::onBlockEntityLoad);
        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register(ClientOutlineManager::onBlockEntityUnload);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                ClientOutlineManager.onJoin(client.level));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (client.isSameThread()) {
                ClientOutlineManager.onDisconnect();
                ClientBlockOutlineManager.onDisconnect();
                return;
            }
            client.execute(() -> {
                ClientOutlineManager.onDisconnect();
                ClientBlockOutlineManager.onDisconnect();
            });
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientOutlineManager.onEndClientTick(client);
            ClientBlockOutlineManager.onEndClientTick(client);
        });
    }
}
