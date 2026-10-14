package moth.butterflyapi.client.mixin;

//? if >=1.21.11 {
import com.mojang.blaze3d.vertex.PoseStack;
import moth.butterflyapi.client.outline.internal.ClientOutlineManager;
import moth.butterflyapi.outline.EntityOutlinePart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerOutlineMixin {
    @Unique
    private int butterflyApi$previousOutlineColor;

    @Inject(method = "submitArmWithItem", at = @At("HEAD"), require = 1)
    private void butterflyApi$selectHandOutline(
            ArmedEntityRenderState state,
            ItemStackRenderState itemState,
            ItemStack stack,
            HumanoidArm arm,
            PoseStack matrices,
            SubmitNodeCollector collector,
            int light,
            CallbackInfo ci
    ) {
        butterflyApi$previousOutlineColor = state.outlineColor;
        state.outlineColor = ClientOutlineManager.resolveRenderPartColor(
                state,
                EntityOutlinePart.hand(arm, state.mainArm),
                state.outlineColor
        );
    }

    @Inject(method = "submitArmWithItem", at = @At("RETURN"), require = 1)
    private void butterflyApi$restoreHandOutline(
            ArmedEntityRenderState state,
            ItemStackRenderState itemState,
            ItemStack stack,
            HumanoidArm arm,
            PoseStack matrices,
            SubmitNodeCollector collector,
            int light,
            CallbackInfo ci
    ) {
        state.outlineColor = butterflyApi$previousOutlineColor;
    }
}
//?}
