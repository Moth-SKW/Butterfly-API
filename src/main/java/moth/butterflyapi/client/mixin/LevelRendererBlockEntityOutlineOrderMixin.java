package moth.butterflyapi.client.mixin;

//? if >=26.2 {
/*
import com.mojang.blaze3d.vertex.PoseStack;
import moth.butterflyapi.plush.PlushBlockEntityRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererBlockEntityOutlineOrderMixin {
    @Inject(method = "submitEntities", at = @At("HEAD"), require = 1)
    private void butterflyApi$submitPlushOutlinesBeforeEntities(
            PoseStack matrices,
            LevelRenderState levelRenderState,
            SubmitNodeCollector queue,
            CallbackInfo ci
    ) {
        Vec3 camera = levelRenderState.cameraRenderState.pos;
        for (BlockEntityRenderState renderState : levelRenderState.blockEntityRenderStates) {
            if (!(renderState instanceof PlushBlockEntityRenderer.State plushState) || renderState.blockPos == null) {
                continue;
            }
            matrices.pushPose();
            matrices.translate(
                    renderState.blockPos.getX() - camera.x,
                    renderState.blockPos.getY() - camera.y,
                    renderState.blockPos.getZ() - camera.z
            );
            PlushBlockEntityRenderer.submitOutline(plushState, matrices, queue);
            matrices.popPose();
        }
    }
}
*///?}
