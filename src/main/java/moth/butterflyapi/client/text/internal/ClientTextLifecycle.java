package moth.butterflyapi.client.text.internal;

import moth.butterflyapi.bootstrap.Bootstrap;
import moth.butterflyapi.client.text.ExpandableTooltip;
import moth.butterflyapi.client.text.TooltipActions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

@Environment(EnvType.CLIENT)
public final class ClientTextLifecycle {
    private static final Bootstrap BOOTSTRAP = Bootstrap.create();

    private ClientTextLifecycle() {
    }

    public static void initialize() {
        BOOTSTRAP.run(() -> ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            Runnable clear = () -> {
                ExpandableTooltip.clearInteractionState();
                TooltipActions.clearInteractionState();
            };
            if (client.isSameThread()) {
                clear.run();
            } else {
                client.execute(clear);
            }
        }));
    }
}
