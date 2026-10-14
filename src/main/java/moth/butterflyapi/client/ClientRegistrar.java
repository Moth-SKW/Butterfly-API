package moth.butterflyapi.client;

import moth.butterflyapi.mod.ModContext;
//? if <26.1 {
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
//?}
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
//? if >=1.21.11 {
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
//?}
//? if >=1.21.11 && <26.1 {
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
//?}
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import java.util.Objects;

public final class ClientRegistrar {
    public ClientRegistrar(ModContext context) {
        Objects.requireNonNull(context, "context");
    }

    public void cutout(Block... blocks) {
//? if <1.21.11 {
/*
        if (blocks.length > 0) {
            BlockRenderLayerMap.INSTANCE.putBlocks(net.minecraft.client.renderer.RenderType.cutout(), blocks);
        }
*///?} else {
//? if >=26.1 {
/*
        Objects.requireNonNull(blocks, "blocks");
*///?} else {
        if (blocks.length > 0) {
            BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT, blocks);
        }
//?}
//?}
    }

    public void cutoutMipped(Block... blocks) {
//? if <1.21.11 {
/*
        if (blocks.length > 0) {
            BlockRenderLayerMap.INSTANCE.putBlocks(net.minecraft.client.renderer.RenderType.cutoutMipped(), blocks);
        }
*///?} else {
//? if >=26.1 {
/*
        Objects.requireNonNull(blocks, "blocks");
*///?} else {
        if (blocks.length > 0) {
            BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT, blocks);
        }
//?}
//?}
    }

    public void translucent(Block... blocks) {
//? if <1.21.11 {
/*
        if (blocks.length > 0) {
            BlockRenderLayerMap.INSTANCE.putBlocks(net.minecraft.client.renderer.RenderType.translucent(), blocks);
        }
*///?} else {
//? if >=26.1 {
/*
        Objects.requireNonNull(blocks, "blocks");
*///?} else {
        if (blocks.length > 0) {
            BlockRenderLayerMap.putBlocks(ChunkSectionLayer.TRANSLUCENT, blocks);
        }
//?}
//?}
    }

    public <T extends Entity> void entity(EntityType<? extends T> entityType, EntityRendererProvider<T> factory) {
//? if <1.21.11 {
/*
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(entityType, factory);
*///?} else {
        EntityRenderers.register(entityType, factory);
//?}
    }

//? if >=1.21.11 {
    public <T extends BlockEntity, S extends BlockEntityRenderState> void blockEntity(
            BlockEntityType<? extends T> blockEntityType,
            BlockEntityRendererProvider<T, S> factory
    ) {
        BlockEntityRenderers.register(blockEntityType, factory);
    }
//?} else {
/*
    public <T extends BlockEntity> void blockEntity(
            BlockEntityType<? extends T> blockEntityType,
            BlockEntityRendererProvider<T> factory
    ) {
        BlockEntityRenderers.register(blockEntityType, factory);
    }
*///?}

    public <T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> void screen(MenuType<? extends T> screenHandlerType, MenuScreens.ScreenConstructor<T, U> provider) {
        MenuScreens.register(screenHandlerType, provider);
    }

    public void modelLayer(ModelLayerLocation layer, EntityModelLayerRegistry.TexturedModelDataProvider provider) {
        EntityModelLayerRegistry.registerModelLayer(layer, provider);
    }

    public <T extends ParticleOptions> void particle(ParticleType<T> particleType, ParticleFactoryRegistry.PendingParticleFactory<T> factory) {
        ParticleFactoryRegistry.getInstance().register(particleType, factory);
    }
}
