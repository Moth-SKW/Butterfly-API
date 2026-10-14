package moth.butterflyapi.client.mixin;

//? if <1.21.11 {
/*
import moth.butterflyapi.client.outline.internal.ClientOutlineManager;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public abstract class LegacyLevelRendererOutlineMixin {
    @Inject(method = "shouldShowEntityOutlines", at = @At("RETURN"), cancellable = true)
    private void butterflyApi$includeBlockEntityOutlines(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && ClientOutlineManager.hasAnyCustomOutlines()) {
            cir.setReturnValue(true);
        }
    }
}
*///?}
