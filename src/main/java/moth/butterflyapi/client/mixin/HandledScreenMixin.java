package moth.butterflyapi.client.mixin;

import moth.butterflyapi.client.CreativeInventoryScreenExtension;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = HandledScreen.class, priority = 900)
public abstract class HandledScreenMixin {
    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlotHighlight(" +
                            "Lnet/minecraft/client/gui/DrawContext;III)V"
            ),
            require = 0
    )
    private void butterflyApi$drawCreativeSlotHighlight(
            DrawContext context,
            int x,
            int y,
            int z
    ) {
        if ((Object) this instanceof CreativeInventoryScreenExtension extension) {
            if (extension.butterflyApi$isCategorySlotFocused()) {
                return;
            }
        }
        HandledScreen.drawSlotHighlight(context, x, y, z);
    }
}
