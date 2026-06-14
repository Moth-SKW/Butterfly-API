package moth.butterflyapi.client;

import moth.butterflyapi.mod.ModContext;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemConvertible;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

import java.util.Objects;

public final class ClientRegistrar {
    private final ModContext context;

    public ClientRegistrar(ModContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public void cutout(Block... blocks) {
        if (blocks.length > 0) {
            BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), blocks);
        }
    }

    public void cutoutMipped(Block... blocks) {
        if (blocks.length > 0) {
            BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(), blocks);
        }
    }

    public void translucent(Block... blocks) {
        if (blocks.length > 0) {
            BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), blocks);
        }
    }

    public <T extends Entity> void entity(EntityType<? extends T> entityType, EntityRendererFactory<T> factory) {
        EntityRendererRegistry.register(entityType, factory);
    }

    public <T extends BlockEntity> void blockEntity(BlockEntityType<? extends T> blockEntityType, BlockEntityRendererFactory<? super T> factory) {
        BlockEntityRendererFactories.register(blockEntityType, factory);
    }

    public <T extends ScreenHandler, U extends Screen & ScreenHandlerProvider<T>> void screen(ScreenHandlerType<? extends T> screenHandlerType, HandledScreens.Provider<T, U> provider) {
        HandledScreens.register(screenHandlerType, provider);
    }

    public void predicate(ItemConvertible item, String path, ClampedModelPredicateProvider provider) {
        ModelPredicateProviderRegistry.register(item.asItem(), context.id(path), provider);
    }

    public void modelLayer(EntityModelLayer layer, EntityModelLayerRegistry.TexturedModelDataProvider provider) {
        EntityModelLayerRegistry.registerModelLayer(layer, provider);
    }

    public <T extends ParticleEffect> void particle(ParticleType<T> particleType, ParticleFactoryRegistry.PendingParticleFactory<T> factory) {
        ParticleFactoryRegistry.getInstance().register(particleType, factory);
    }
}
