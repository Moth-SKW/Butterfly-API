package moth.butterflyapi.client;

import moth.butterflyapi.ButterflyApi;
import moth.butterflyapi.content.ContentClientBootstrap;
import net.fabricmc.api.ClientModInitializer;

public final class ButterflyApiClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ButterflyApi.LOGGER.info("Initializing {} client", ButterflyApi.MOD_NAME);
        PlushClientBootstrap.initialize();
        new ContentClientBootstrap().onInitializeClient();
    }
}
