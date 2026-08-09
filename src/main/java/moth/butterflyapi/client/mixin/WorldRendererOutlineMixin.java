package moth.butterflyapi.client.mixin;

import moth.butterflyapi.client.outline.internal.ClientOutlineManager;
import moth.butterflyapi.outline.OutlineOnlyBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererOutlineMixin {
    @Shadow
    private WorldRenderState worldRenderState;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/WorldRenderer;renderMain(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/Frustum;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZLnet/minecraft/client/render/state/WorldRenderState;Lnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/util/profiler/Profiler;)V",
                    shift = At.Shift.BEFORE
            ),
            require = 1
    )
    private void butterflyApi$includeBlockEntityOutlines(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null || worldRenderState.hasOutline) {
            return;
        }

        for (BlockEntityRenderState renderState : worldRenderState.blockEntityRenderStates) {
            if (renderState.pos == null) {
                continue;
            }
            BlockEntity blockEntity = world.getBlockEntity(renderState.pos);
            if (blockEntity instanceof OutlineOnlyBlockEntity
                    && ClientOutlineManager.resolve(blockEntity) != null) {
                worldRenderState.hasOutline = true;
                return;
            }
        }
    }
}
