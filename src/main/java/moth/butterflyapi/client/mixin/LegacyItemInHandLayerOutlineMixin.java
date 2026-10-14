package moth.butterflyapi.client.mixin;

//? if <1.21.11 {
/*
import com.mojang.blaze3d.vertex.PoseStack;
import moth.butterflyapi.client.outline.LegacyOutlineParts;
import moth.butterflyapi.outline.EntityOutlinePart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class LegacyItemInHandLayerOutlineMixin {
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true, require = 1)
    private void butterflyApi$selectHeldItemPart(
            LivingEntity entity,
            ItemStack stack,
            ItemDisplayContext displayContext,
            HumanoidArm arm,
            PoseStack matrices,
            MultiBufferSource buffers,
            int light,
            CallbackInfo ci
    ) {
        if (!LegacyOutlineParts.configure(buffers, entity, EntityOutlinePart.hand(arm, entity.getMainArm()))) {
            ci.cancel();
        }
    }

    @Inject(method = "renderArmWithItem", at = @At("RETURN"), require = 1)
    private void butterflyApi$restoreBodyPart(
            LivingEntity entity,
            ItemStack stack,
            ItemDisplayContext displayContext,
            HumanoidArm arm,
            PoseStack matrices,
            MultiBufferSource buffers,
            int light,
            CallbackInfo ci
    ) {
        LegacyOutlineParts.restoreBody(buffers, entity);
    }
}
*///?}
