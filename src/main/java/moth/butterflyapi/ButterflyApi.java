package moth.butterflyapi;

import moth.butterflyapi.mod.ModContext;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ButterflyApi {
    public static final String MOD_ID = "butterfly_api";
    public static final String MOD_NAME = "Butterfly API";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private ButterflyApi() {
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static ModContext mod(String modId) {
        return ModContext.of(modId);
    }

    public static ModContext mod(String modId, String modName) {
        return ModContext.of(modId, modName);
    }
}
