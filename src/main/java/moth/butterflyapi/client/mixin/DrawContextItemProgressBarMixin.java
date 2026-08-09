package moth.butterflyapi.client.mixin;

import moth.butterflyapi.client.item.ItemProgressBarRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContextItemProgressBarMixin {
    @Inject(method = "drawItemBar", at = @At("HEAD"), cancellable = true)
    private void butterflyApi$drawItemProgressBars(ItemStack stack, int x, int y, CallbackInfo ci) {
        if (ItemProgressBarRenderer.render((DrawContext) (Object) this, stack, x, y)) {
            ci.cancel();
        }
    }
}
