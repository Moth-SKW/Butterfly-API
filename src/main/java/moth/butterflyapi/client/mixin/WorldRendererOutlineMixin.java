package moth.butterflyapi.client.mixin;

//? if >=1.21.11 {
import moth.butterflyapi.client.outline.internal.ClientOutlineManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class WorldRendererOutlineMixin {
    @Shadow
    private LevelRenderState levelRenderState;

//? if >=26.2 {
/*
    @Inject(method = "render", at = @At("HEAD"), require = 1)
*///?} else {
    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;addMainPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/culling/Frustum;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZLnet/minecraft/client/renderer/state/LevelRenderState;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
                    shift = At.Shift.BEFORE
            ),
            require = 1
    )
//?}
    private void butterflyApi$includeBlockEntityOutlines(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel world = client.level;
        if (world == null || levelRenderState.
//? if >=26.2 {
/*
                shouldShowEntityOutlines
*///?} else {
                haveGlowingEntities
//?}
        ) {
            return;
        }

        for (BlockEntityRenderState renderState : levelRenderState.blockEntityRenderStates) {
            if (renderState.blockPos != null
                    && ClientOutlineManager.hasOutlineOnlyBlockEntityOutline(renderState.blockPos)) {
                levelRenderState.
//? if >=26.2 {
/*
                        shouldShowEntityOutlines
*///?} else {
                        haveGlowingEntities
//?}
                        = true;
                return;
            }
        }
    }
}
//?}
