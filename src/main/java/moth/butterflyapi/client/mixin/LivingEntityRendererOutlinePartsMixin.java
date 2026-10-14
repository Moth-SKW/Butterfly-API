package moth.butterflyapi.client.mixin;

//? if >=1.21.11 {
import com.mojang.blaze3d.vertex.PoseStack;
import moth.butterflyapi.client.outline.internal.ClientOutlineManager;
import moth.butterflyapi.outline.EntityOutlinePart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererOutlinePartsMixin {
    @Redirect(
            method = "submit",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;outlineColor:I"
            ),
            require = 1
    )
    private int butterflyApi$bodyOutlineColor(LivingEntityRenderState state) {
        return ClientOutlineManager.resolveRenderPartColor(state, EntityOutlinePart.BODY, state.outlineColor);
    }

    @Redirect(
            method = "submit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/EntityRenderState;FF)V"
            ),
            require = 1
    )
    private void butterflyApi$submitFeatureOutline(
            RenderLayer layer,
            PoseStack matrices,
            SubmitNodeCollector collector,
            int light,
            EntityRenderState state,
            float bodyYaw,
            float pitch
    ) {
        if (!ClientOutlineManager.hasRenderOutline(state)
                || layer instanceof HumanoidArmorLayer
                || layer instanceof ItemInHandLayer) {
            layer.submit(matrices, collector, light, state, bodyYaw, pitch);
            return;
        }

        int original = state.outlineColor;
        state.outlineColor = ClientOutlineManager.resolveRenderPartColor(
                state,
                EntityOutlinePart.FEATURES,
                original
        );
        try {
            layer.submit(matrices, collector, light, state, bodyYaw, pitch);
        } finally {
            state.outlineColor = original;
        }
    }
}
//?}
