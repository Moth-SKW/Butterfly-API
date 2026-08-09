package moth.butterflyapi;

import moth.butterflyapi.plush.ButterflyPlushes;
import moth.butterflyapi.outline.internal.OutlineServerManager;
import net.fabricmc.api.ModInitializer;

public final class ButterflyApiMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ButterflyApi.LOGGER.info("Initializing {}", ButterflyApi.MOD_NAME);
        OutlineServerManager.initialize();
        ButterflyPlushes.bootstrap();
    }
}
