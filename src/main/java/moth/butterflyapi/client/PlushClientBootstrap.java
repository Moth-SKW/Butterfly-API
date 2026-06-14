package moth.butterflyapi.client;

import moth.butterflyapi.plush.ButterflyPlushes;
import moth.butterflyapi.plush.PlushBlockEntityRenderer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
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
        Block[] blocks = ButterflyPlushes.all().stream()
                .map(plush -> plush.block())
                .toArray(Block[]::new);
        if (blocks.length > 0) {
            BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), blocks);
        }

        initialized = true;
    }
}
