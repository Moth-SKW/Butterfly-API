package moth.butterflyapi.client.mixin;

import moth.butterflyapi.client.CreativeInventoryScreenExtension;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(value = HandledScreen.class, priority = 900)
public abstract class HandledScreenMixin {
    @Inject(method = "drawSlotHighlightBack", at = @At("HEAD"), cancellable = true, require = 0)
    private void butterflyApi$hideCategorySlotHighlightBack(DrawContext context, CallbackInfo ci) {
        if ((Object) this instanceof CreativeInventoryScreenExtension extension) {
            if (extension.butterflyApi$isCategorySlotFocused()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "drawSlotHighlightFront", at = @At("HEAD"), cancellable = true, require = 0)
    private void butterflyApi$hideCategorySlotHighlightFront(DrawContext context, CallbackInfo ci) {
        if ((Object) this instanceof CreativeInventoryScreenExtension extension
                && extension.butterflyApi$isCategorySlotFocused()) {
            ci.cancel();
        }
    }
}
