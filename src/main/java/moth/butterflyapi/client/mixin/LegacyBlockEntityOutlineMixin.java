package moth.butterflyapi.client.mixin;

//? if <1.21.11 {
/*
import com.mojang.blaze3d.vertex.PoseStack;
import moth.butterflyapi.client.outline.OutlineRenderBridge;
import moth.butterflyapi.client.outline.internal.ClientOutlineManager;
import moth.butterflyapi.outline.OutlineStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class LegacyBlockEntityOutlineMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private <E extends BlockEntity> void butterflyApi$renderCustomOutline(
            E blockEntity,
            float tickDelta,
            PoseStack matrices,
            MultiBufferSource buffers,
            CallbackInfo ci
    ) {
        OutlineStyle style = ClientOutlineManager.resolve(blockEntity);
        if (style == null) {
            return;
        }

        OutlineBufferSource outlines = Minecraft.getInstance().renderBuffers().outlineBufferSource();
        int color = OutlineRenderBridge.encodedLegacyBlockColor(
                blockEntity,
                style,
                Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()
        );
        outlines.setColor(
                (color >> 16) & 0xFF,
                (color >> 8) & 0xFF,
                color & 0xFF,
                (color >>> 24) & 0xFF
        );

        BlockEntityRenderer<E> renderer = ((BlockEntityRenderDispatcher) (Object) this).getRenderer(blockEntity);
        if (renderer != null) {
            renderer.render(blockEntity, tickDelta, matrices, outlines, 0x00F000F0, OverlayTexture.NO_OVERLAY);
        }
    }
}
*///?}
