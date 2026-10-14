package moth.butterflyapi.client;

import moth.butterflyapi.plush.ButterflyPlushes;
import moth.butterflyapi.plush.PlushBlockEntityRenderer;
//? if >=1.21.11 && <26.1 {
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
//?}
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
//? if >=1.21.11 && <26.1 {
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
//?}
import net.minecraft.world.level.block.Block;

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

        BlockEntityRenderers.register(
                ButterflyPlushes.blockEntityType(),
                context -> new PlushBlockEntityRenderer(context)
        );

//? if <1.21.11 {
/*
        Block[] blocks = ButterflyPlushes.all().stream()
                .map(plush -> plush.block())
                .toArray(Block[]::new);
        if (blocks.length > 0) {
            net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlocks(
                    net.minecraft.client.renderer.RenderType.cutout(),
                    blocks
            );
        }
*///?} else if <26.1 {
        Block[] blocks = ButterflyPlushes.all().stream()
                .map(plush -> plush.block())
                .toArray(Block[]::new);
        if (blocks.length > 0) {
            BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT, blocks);
        }
//?}

        initialized = true;
    }
}
