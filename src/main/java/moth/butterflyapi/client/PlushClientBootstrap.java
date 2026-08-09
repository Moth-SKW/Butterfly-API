package moth.butterflyapi.client;

import moth.butterflyapi.plush.ButterflyPlushes;
import moth.butterflyapi.plush.PlushBlockEntityRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.block.Block;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public final class PlushClientBootstrap {
    private static boolean initialized;

    private PlushClientBootstrap() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        if (ButterflyPlushes.all().isEmpty()) {
            initialized = true;
            return;
        }

        BlockEntityRendererFactories.register(
                ButterflyPlushes.blockEntityType(),
                context -> new PlushBlockEntityRenderer(context)
        );

        Block[] blocks = ButterflyPlushes.all().stream()
                .map(plush -> plush.block())
                .toArray(Block[]::new);
        if (blocks.length > 0) {
            BlockRenderLayerMap.putBlocks(BlockRenderLayer.CUTOUT, blocks);
        }

        initialized = true;
    }
}
