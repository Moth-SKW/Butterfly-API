package moth.butterflyapi.client.text.internal;

import moth.butterflyapi.debug.ButterflyTextDebug;
import net.fabricmc.loader.api.FabricLoader;

public final class ButterflyTextClientBootstrap {
    private ButterflyTextClientBootstrap() {
    }

    public static void initialize() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            ButterflyTextDebug.initialize();
        }
    }
}
