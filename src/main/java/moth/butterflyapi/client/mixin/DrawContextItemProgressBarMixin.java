package moth.butterflyapi.client.mixin;

import moth.butterflyapi.client.item.ItemProgressBarRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class DrawContextItemProgressBarMixin {
//? if >=26.1 {
/*
    @Inject(
            method = "itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void butterflyApi$drawItemProgressBars(
            net.minecraft.client.gui.Font font,
            ItemStack stack,
            int x,
            int y,
            CallbackInfo ci
    ) {
        if (ItemProgressBarRenderer.render((GuiGraphics) (Object) this, stack, x, y)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void butterflyApi$drawItemProgressBarsWithOverlayText(
            net.minecraft.client.gui.Font font,
            ItemStack stack,
            int x,
            int y,
            String overlayText,
            CallbackInfo ci
    ) {
        if (ItemProgressBarRenderer.render((GuiGraphics) (Object) this, stack, x, y)) {
            ci.cancel();
        }
    }
*///?} else {
//? if >=1.21.11 {
/*
    @Inject(method = "renderItemBar", at = @At("HEAD"), cancellable = true)
    private void butterflyApi$drawItemProgressBars(ItemStack stack, int x, int y, CallbackInfo ci) {
        if (ItemProgressBarRenderer.render((GuiGraphics) (Object) this, stack, x, y)) {
            ci.cancel();
        }
    }
*///?} else {
    @Inject(
            method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void butterflyApi$drawItemProgressBars(
            net.minecraft.client.gui.Font font,
            ItemStack stack,
            int x,
            int y,
            CallbackInfo ci
    ) {
        if (ItemProgressBarRenderer.render((GuiGraphics) (Object) this, stack, x, y)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void butterflyApi$drawItemProgressBarsWithOverlayText(
            net.minecraft.client.gui.Font font,
            ItemStack stack,
            int x,
            int y,
            String overlayText,
            CallbackInfo ci
    ) {
        if (ItemProgressBarRenderer.render((GuiGraphics) (Object) this, stack, x, y)) {
            ci.cancel();
        }
    }
//?}
//?}
}
