package moth.butterflyapi.client.mixin;

//? if <1.21.11 {
/*
import com.mojang.blaze3d.vertex.PoseStack;
import moth.butterflyapi.client.outline.LegacyOutlineParts;
import moth.butterflyapi.outline.EntityOutlinePart;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class LegacyHumanoidArmorLayerOutlineMixin {
    @Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true, require = 1)
    private void butterflyApi$selectArmorPart(
            PoseStack matrices,
            MultiBufferSource buffers,
            LivingEntity entity,
            EquipmentSlot slot,
            int light,
            HumanoidModel<?> model,
            CallbackInfo ci
    ) {
        if (!LegacyOutlineParts.configure(buffers, entity, EntityOutlinePart.armor(slot))) {
            ci.cancel();
        }
    }

    @Inject(method = "renderArmorPiece", at = @At("RETURN"), require = 1)
    private void butterflyApi$restoreBodyPart(
            PoseStack matrices,
            MultiBufferSource buffers,
            LivingEntity entity,
            EquipmentSlot slot,
            int light,
            HumanoidModel<?> model,
            CallbackInfo ci
    ) {
        LegacyOutlineParts.restoreBody(buffers, entity);
    }
}
*///?}
