package moth.butterflyapi;

import moth.butterflyapi.plush.ButterflyPlushes;
import moth.butterflyapi.outline.internal.OutlineServerManager;
import moth.butterflyapi.text.TextEffects;
import net.fabricmc.api.ModInitializer;

public final class ButterflyApiMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ButterflyApi.LOGGER.info("Initializing {}", ButterflyApi.MOD_NAME);
        TextEffects.bootstrap();
        OutlineServerManager.initialize();
        ButterflyPlushes.bootstrap();
    }
}
