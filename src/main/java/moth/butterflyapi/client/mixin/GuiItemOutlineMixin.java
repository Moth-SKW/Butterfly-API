package moth.butterflyapi.client.mixin;

import moth.butterflyapi.client.outline.GuiItemOutlineRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class GuiItemOutlineMixin {
//? if >=26.1 {
/*
    @Inject(
            method = "item(Lnet/minecraft/world/item/ItemStack;II)V",
            at = @At("HEAD"),
            require = 1
    )
*///?} else {
    @Inject(
            method = "renderItem(Lnet/minecraft/world/item/ItemStack;II)V",
            at = @At("HEAD"),
            require = 1
    )
//?}
    private void butterflyApi$renderGuiItemOutlineBefore(ItemStack stack, int x, int y, CallbackInfo ci) {
        GuiItemOutlineRenderer.renderBefore((GuiGraphics) (Object) this, stack, x, y);
    }

//? if >=26.1 {
/*
    @Inject(
            method = "item(Lnet/minecraft/world/item/ItemStack;II)V",
            at = @At("RETURN"),
            require = 1
    )
*///?} else {
    @Inject(
            method = "renderItem(Lnet/minecraft/world/item/ItemStack;II)V",
            at = @At("RETURN"),
            require = 1
    )
//?}
    private void butterflyApi$renderGuiItemOutlineAfter(ItemStack stack, int x, int y, CallbackInfo ci) {
        GuiItemOutlineRenderer.renderAfter((GuiGraphics) (Object) this, stack, x, y);
    }
}
