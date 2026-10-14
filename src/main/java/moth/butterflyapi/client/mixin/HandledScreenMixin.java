package moth.butterflyapi.client.mixin;

//? if >=1.21.11 {

import moth.butterflyapi.client.CreativeInventoryScreenExtension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(value = AbstractContainerScreen.class, priority = 900)
public abstract class HandledScreenMixin {
    @Inject(method = "renderSlotHighlightBack", at = @At("HEAD"), cancellable = true, require = 0)
    private void butterflyApi$hideCategorySlotHighlightBack(GuiGraphics context, CallbackInfo ci) {
        if ((Object) this instanceof CreativeInventoryScreenExtension extension) {
            if (extension.butterflyApi$isCategorySlotFocused()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "renderSlotHighlightFront", at = @At("HEAD"), cancellable = true, require = 0)
    private void butterflyApi$hideCategorySlotHighlightFront(GuiGraphics context, CallbackInfo ci) {
        if ((Object) this instanceof CreativeInventoryScreenExtension extension
                && extension.butterflyApi$isCategorySlotFocused()) {
            ci.cancel();
        }
    }
}
//?}
