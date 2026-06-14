package moth.butterflyapi;

import moth.butterflyapi.content.ContentBootstrap;
import net.fabricmc.api.ModInitializer;
import software.bernie.geckolib.GeckoLib;

public final class ButterflyApiMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ButterflyApi.LOGGER.info("Initializing {}", ButterflyApi.MOD_NAME);
        GeckoLib.initialize();
        new ContentBootstrap().onInitialize();
    }
}
