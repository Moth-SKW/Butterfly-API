package moth.butterflyapi.content.blocks;

import moth.butterflyapi.plush.PlushEntrypoint;
import moth.butterflyapi.plush.PlushRegistrar;

public final class DefaultPlushEntrypoint implements PlushEntrypoint {
    @Override
    public void registerPlushes(PlushRegistrar registrar) {
        Plushes.registerAll(registrar);
    }
}
