package moth.butterflyapi.client;

import moth.butterflyapi.plush.ButterflyPlushes;
import moth.butterflyapi.content.blocks.Plushes;
import moth.butterflyapi.plush.PlushBlockEntityRenderer;
import moth.butterflyapi.plush.PlushItemRenderer;
import moth.butterflyapi.plush.RegisteredPlush;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public final class PlushClientBootstrap {
    private static boolean initialized;

    private PlushClientBootstrap() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        BlockEntityRendererFactories.register(ButterflyPlushes.blockEntityType(), PlushBlockEntityRenderer::new);

        RegisteredPlush ringmasterHex = Plushes.registered(Plushes.RINGMASTER_HEX);
        BuiltinItemRendererRegistry.INSTANCE.register(
                ringmasterHex.item(),
                new PlushItemRenderer(ringmasterHex)
        );
        Block[] blocks = ButterflyPlushes.all().stream()
                .map(plush -> plush.block())
                .toArray(Block[]::new);
        if (blocks.length > 0) {
            BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), blocks);
        }

        initialized = true;
    }
}
