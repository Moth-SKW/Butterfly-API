package moth.butterflyapi.client.mixin;

//? if <1.21.11 {
/*
import moth.butterflyapi.client.text.internal.LegacyTextRenderContext;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Font.class)
public abstract class LegacyFontTextPlanMixin {
    @Shadow
    @Final
    private boolean filterFishyGlyphs;

    @Shadow
    abstract FontSet getFontSet(ResourceLocation font);

    @Inject(
            method = "renderText(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F",
            at = @At("HEAD")
    )
    private void butterflyApi$beginTextPlan(
            FormattedCharSequence text,
            float x,
            float y,
            int color,
            boolean shadow,
            Matrix4f matrix,
            MultiBufferSource buffers,
            Font.DisplayMode displayMode,
            int backgroundColor,
            int light,
            CallbackInfoReturnable<Float> cir
    ) {
        LegacyTextRenderContext.begin(
                text,
                x,
                y,
                shadow,
                (style, codePoint) -> getFontSet(style.getFont())
                        .getGlyphInfo(codePoint, filterFishyGlyphs)
                        .getAdvance(style.isBold())
        );
    }

    @Inject(
            method = "renderText(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F",
            at = @At("RETURN")
    )
    private void butterflyApi$endTextPlan(
            FormattedCharSequence text,
            float x,
            float y,
            int color,
            boolean shadow,
            Matrix4f matrix,
            MultiBufferSource buffers,
            Font.DisplayMode displayMode,
            int backgroundColor,
            int light,
            CallbackInfoReturnable<Float> cir
    ) {
        LegacyTextRenderContext.end();
    }
}
*///?}
