package moth.butterflyapi.client.mixin;

//? if <1.21.11 {
/*
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import moth.butterflyapi.client.outline.LegacyOutlineParts;
import moth.butterflyapi.client.outline.internal.ClientOutlineManager;
import moth.butterflyapi.outline.EntityOutline;
import moth.butterflyapi.outline.EntityOutlinePart;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LegacyLivingEntityRendererOutlinePartsMixin {
    private static final ThreadLocal<EntityOutline> BUTTERFLY_API_OUTLINE = new ThreadLocal<>();

    @Inject(method = "render", at = @At("HEAD"), require = 1)
    private void butterflyApi$startOutlinePartRender(
            LivingEntity entity,
            float entityYaw,
            float tickDelta,
            PoseStack matrices,
            MultiBufferSource buffers,
            int light,
            CallbackInfo ci
    ) {
        BUTTERFLY_API_OUTLINE.set(
                buffers instanceof OutlineBufferSource ? ClientOutlineManager.resolveOutline(entity) : null
        );
    }

    @Inject(method = "render", at = @At("RETURN"), require = 1)
    private void butterflyApi$endOutlinePartRender(
            LivingEntity entity,
            float entityYaw,
            float tickDelta,
            PoseStack matrices,
            MultiBufferSource buffers,
            int light,
            CallbackInfo ci
    ) {
        BUTTERFLY_API_OUTLINE.remove();
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
            ),
            require = 1
    )
    private void butterflyApi$renderBodyPart(
            EntityModel<?> model,
            PoseStack matrices,
            VertexConsumer vertices,
            int light,
            int overlay,
            int color
    ) {
        if (LegacyOutlineParts.includes(BUTTERFLY_API_OUTLINE.get(), EntityOutlinePart.BODY)) {
            model.renderToBuffer(matrices, vertices, light, overlay, color);
        }
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V"
            ),
            require = 1
    )
    private void butterflyApi$renderFeaturePart(
            RenderLayer layer,
            PoseStack matrices,
            MultiBufferSource buffers,
            int light,
            Entity entity,
            float limbAngle,
            float limbDistance,
            float tickDelta,
            float animationProgress,
            float headYaw,
            float headPitch
    ) {
        if (!(buffers instanceof OutlineBufferSource) || ClientOutlineManager.resolveOutline(entity) == null) {
            layer.render(matrices, buffers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
            return;
        }
        if (layer instanceof HumanoidArmorLayer || layer instanceof ItemInHandLayer) {
            layer.render(matrices, buffers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
            return;
        }
        if (!LegacyOutlineParts.configure(buffers, entity, EntityOutlinePart.FEATURES)) {
            return;
        }
        try {
            layer.render(matrices, buffers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
        } finally {
            LegacyOutlineParts.restoreBody(buffers, entity);
        }
    }
}
*///?}
