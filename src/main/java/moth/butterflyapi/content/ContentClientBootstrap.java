package moth.butterflyapi.content;

import moth.butterflyapi.content.blocks.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class ContentClientBootstrap implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModBlocks.init();

        BlockRenderLayerMap.INSTANCE.putBlock(
                ModBlocks.BIG_RED_BUTTON,
                RenderLayer.getCutout()
        );
        BlockRenderLayerMap.INSTANCE.putBlocks(
                RenderLayer.getCutout(),
                ModBlocks.MATCHSTICK_FIRE,
                ModBlocks.MATCHSTICK_SOUL_FIRE
        );
    }
}
