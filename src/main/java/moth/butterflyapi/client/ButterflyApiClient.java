package moth.butterflyapi.client;

import moth.butterflyapi.ButterflyApi;
import moth.butterflyapi.client.outline.OutlineRenderBridge;
import moth.butterflyapi.client.outline.GuiItemOutlineRenderer;
import moth.butterflyapi.client.outline.internal.ClientOutlineBootstrap;
import moth.butterflyapi.client.particle.ClientParticleBootstrap;
import moth.butterflyapi.client.text.internal.ClientTextLifecycle;
import moth.butterflyapi.client.testing.ClientIntegrationTestService;
import net.fabricmc.api.ClientModInitializer;

public final class ButterflyApiClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ButterflyApi.LOGGER.info("Initializing {} client", ButterflyApi.MOD_NAME);
        OutlineRenderBridge.initialize();
        GuiItemOutlineRenderer.initialize();
        ClientOutlineBootstrap.initialize();
        ClientParticleBootstrap.initialize();
        ClientTextLifecycle.initialize();
        PlushClientBootstrap.initialize();
        ClientIntegrationTestService.initialize();
    }
}
