package moth.butterflyapi.client.testing;

import java.util.List;
import moth.butterflyapi.ButterflyApi;
import moth.butterflyapi.client.outline.ClientOutlines;
import moth.butterflyapi.client.outline.ClientItemOutlines;
import moth.butterflyapi.client.particle.ClientParticles;
import moth.butterflyapi.client.text.ClientTexts;
import moth.butterflyapi.outline.EntityOutline;
import moth.butterflyapi.outline.ItemOutlineTarget;
import moth.butterflyapi.outline.OutlineStyle;
import moth.butterflyapi.testing.IntegrationTestService;
import moth.butterflyapi.testing.network.IntegrationTestRunS2CPacket;
import moth.butterflyapi.text.Texts;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public final class ClientIntegrationTestService {
    private static boolean initialized;

    private ClientIntegrationTestService() {
    }

    public static void initialize() {
        if (!IntegrationTestService.enabled() || initialized) {
            return;
        }
        ClientPlayNetworking.registerGlobalReceiver(IntegrationTestRunS2CPacket.ID, (packet, context) -> {
            if (packet.clear()) {
                clear();
            } else {
                run(packet.anchor());
            }
        });
        initialized = true;
    }

    private static void run(BlockPos anchor) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }
        Vec3 center = Vec3.atCenterOf(anchor).add(0.0D, 1.0D, 0.0D);
        boolean ember = ClientParticles.spawn(ButterflyApi.id("examples/ember"), center);
        boolean cloud = ClientParticles.spawn(ButterflyApi.id("examples/cloud"), center.add(0.8D, 0.0D, 0.0D));
        boolean burst = ClientParticles.spawn(ButterflyApi.id("examples/burst"), center.add(-0.8D, 0.0D, 0.0D));
        ClientOutlines.setAll(
                List.of(anchor.offset(1, 0, 0), anchor.offset(0, 0, 1), anchor.offset(1, 0, 1)),
                style -> style.color("#FFD166")
        );
        if (client.player != null) {
            ClientOutlines.set(client.player, EntityOutline.builder()
                    .body(OutlineStyle.builder("#C77DFF").thickness(2).build())
                    .mainHand(OutlineStyle.builder("#FFD166").thickness(2).build())
                    .offHand(OutlineStyle.builder("#4CC9F0").thickness(2).build())
                    .build());
            ClientItemOutlines.set(
                    client.player.getMainHandItem().getItem(),
                    OutlineStyle.builder("#7CFF6B").thickness(2).build(),
                    ItemOutlineTarget.DROPPED,
                    ItemOutlineTarget.GUI
            );
        }
        ClientTexts.title(Texts.builder("Butterfly API")
                .gradientHorizontal("#F72585", "#4CC9F0")
                .bold()
                .text(" integration")
                .build());
        ClientTexts.subtitle(Texts.shaky("Inspect the test kit and nearby effects"));
        ClientTexts.actionBar(Texts.color(
                "Particles: ember=%s cloud=%s burst=%s".formatted(ember, cloud, burst),
                ember && cloud && burst ? "#7CFF6B" : "#FF5555"
        ));
    }

    private static void clear() {
        ClientParticles.clear();
        ClientOutlines.clearAll();
        ClientItemOutlines.clearAll();
        ClientTexts.actionBar(Texts.color("Butterfly integration effects cleared", "#8A8A8A"));
    }
}
