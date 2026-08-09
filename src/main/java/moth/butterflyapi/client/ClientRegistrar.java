package moth.butterflyapi.client;

import moth.butterflyapi.mod.ModContext;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRendererFactories;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

import java.util.Objects;

public final class ClientRegistrar {
    public ClientRegistrar(ModContext context) {
        Objects.requireNonNull(context, "context");
    }

    public void cutout(Block... blocks) {
        if (blocks.length > 0) {
            BlockRenderLayerMap.putBlocks(BlockRenderLayer.CUTOUT, blocks);
        }
    }

    public void cutoutMipped(Block... blocks) {
        if (blocks.length > 0) {
            BlockRenderLayerMap.putBlocks(BlockRenderLayer.CUTOUT, blocks);
        }
    }

    public void translucent(Block... blocks) {
        if (blocks.length > 0) {
            BlockRenderLayerMap.putBlocks(BlockRenderLayer.TRANSLUCENT, blocks);
        }
    }

    public <T extends Entity> void entity(EntityType<? extends T> entityType, EntityRendererFactory<T> factory) {
        EntityRendererFactories.register(entityType, factory);
    }

    public <T extends BlockEntity, S extends BlockEntityRenderState> void blockEntity(
            BlockEntityType<? extends T> blockEntityType,
            BlockEntityRendererFactory<T, S> factory
    ) {
        BlockEntityRendererFactories.register(blockEntityType, factory);
    }

    public <T extends ScreenHandler, U extends Screen & ScreenHandlerProvider<T>> void screen(ScreenHandlerType<? extends T> screenHandlerType, HandledScreens.Provider<T, U> provider) {
        HandledScreens.register(screenHandlerType, provider);
    }

    public void modelLayer(EntityModelLayer layer, EntityModelLayerRegistry.TexturedModelDataProvider provider) {
        EntityModelLayerRegistry.registerModelLayer(layer, provider);
    }

    public <T extends ParticleEffect> void particle(ParticleType<T> particleType, ParticleFactoryRegistry.PendingParticleFactory<T> factory) {
        ParticleFactoryRegistry.getInstance().register(particleType, factory);
    }
}
