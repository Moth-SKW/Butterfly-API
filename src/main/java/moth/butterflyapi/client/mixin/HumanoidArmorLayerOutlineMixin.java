package moth.butterflyapi.client.mixin;

//? if >=1.21.11 {
import com.mojang.blaze3d.vertex.PoseStack;
import moth.butterflyapi.client.outline.internal.ClientOutlineManager;
import moth.butterflyapi.outline.EntityOutlinePart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerOutlineMixin {
    @Unique
    private int butterflyApi$previousOutlineColor;

    @Inject(method = "renderArmorPiece", at = @At("HEAD"), require = 1)
    private void butterflyApi$selectArmorOutline(
            PoseStack matrices,
            SubmitNodeCollector collector,
            ItemStack stack,
            EquipmentSlot slot,
            int light,
            HumanoidRenderState state,
            CallbackInfo ci
    ) {
        butterflyApi$previousOutlineColor = state.outlineColor;
        state.outlineColor = ClientOutlineManager.resolveRenderPartColor(
                state,
                EntityOutlinePart.armor(slot),
                state.outlineColor
        );
    }

    @Inject(method = "renderArmorPiece", at = @At("RETURN"), require = 1)
    private void butterflyApi$restoreArmorOutline(
            PoseStack matrices,
            SubmitNodeCollector collector,
            ItemStack stack,
            EquipmentSlot slot,
            int light,
            HumanoidRenderState state,
            CallbackInfo ci
    ) {
        state.outlineColor = butterflyApi$previousOutlineColor;
    }
}
//?}
