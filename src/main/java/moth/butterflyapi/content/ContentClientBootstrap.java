package moth.butterflyapi.content;

import moth.butterflyapi.content.blocks.ModBlocks;
import moth.butterflyapi.content.blocks.ModBlockEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.render.BlockRenderLayer;

@Environment(EnvType.CLIENT)
public class ContentClientBootstrap implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModBlocks.init();
        ModBlockEntities.init();

        BlockRenderLayerMap.putBlock(
                ModBlocks.BIG_RED_BUTTON,
                BlockRenderLayer.CUTOUT
        );
        BlockRenderLayerMap.putBlocks(
                BlockRenderLayer.CUTOUT,
                ModBlocks.MATCHSTICK_FIRE,
                ModBlocks.MATCHSTICK_SOUL_FIRE
        );
    }
}
