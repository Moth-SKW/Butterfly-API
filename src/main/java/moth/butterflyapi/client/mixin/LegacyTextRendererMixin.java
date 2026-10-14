package moth.butterflyapi.client.mixin;

//? if <1.21.11 {
/*

import com.mojang.blaze3d.vertex.VertexConsumer;
import moth.butterflyapi.client.text.internal.LegacyTextRenderContext;
import moth.butterflyapi.client.text.internal.LegacyTextVertexConsumer;
import moth.butterflyapi.text.TextEffect;
import moth.butterflyapi.text.TextEffectRenderContext;
import moth.butterflyapi.text.TextGradient;
import moth.butterflyapi.text.TextRenderBounds;
import moth.butterflyapi.text.TextRenderOffset;
import moth.butterflyapi.text.TextRenderStyle;
import moth.butterflyapi.text.TextRenderStyles;
import moth.butterflyapi.text.TextShadow;
import moth.butterflyapi.text.TextTransform;
import moth.butterflyapi.text.TextEffects;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public abstract class LegacyTextRendererMixin {
    @Shadow
    @Final
    private boolean dropShadow;
    @Shadow
    private float x;
    @Shadow
    private float y;
    @Shadow
    private List<BakedGlyph.Effect> effects;
    @Unique
    private Style butterflyApi$currentStyle;
    @Unique
    private int butterflyApi$currentCodePoint;
    @Unique
    private int butterflyApi$currentIndex;
    @Unique
    private BakedGlyph.Effect butterflyApi$backgroundEffect;

    @Inject(method = "accept", at = @At("HEAD"))
    private void butterflyApi$captureStyle(int index, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        butterflyApi$currentStyle = style;
        butterflyApi$currentCodePoint = codePoint;
        butterflyApi$currentIndex = index;
    }

    @Inject(method = "accept", at = @At("RETURN"))
    private void butterflyApi$clearStyle(int index, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        butterflyApi$currentStyle = null;
        butterflyApi$currentCodePoint = 0;
        butterflyApi$currentIndex = 0;
    }

    @Inject(
            method = "finish",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font$StringRenderOutput;addEffect(Lnet/minecraft/client/gui/font/glyphs/BakedGlyph$Effect;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void butterflyApi$captureBackgroundEffect(
            int backgroundColor,
            float textStart,
            CallbackInfoReturnable<Float> cir
    ) {
        if (effects != null && !effects.isEmpty()) {
            butterflyApi$backgroundEffect = effects.get(effects.size() - 1);
        }
    }

    @Redirect(
            method = "finish",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;renderEffect(Lnet/minecraft/client/gui/font/glyphs/BakedGlyph$Effect;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;I)V"
            )
    )
    private void butterflyApi$renderAlignedBackground(
            BakedGlyph glyph,
            BakedGlyph.Effect effect,
            Matrix4f matrix,
            VertexConsumer vertices,
            int light
    ) {
        if (effect != butterflyApi$backgroundEffect) {
            glyph.renderEffect(effect, matrix, vertices, light);
            return;
        }
        LegacyTextRenderContext.Plan plan = LegacyTextRenderContext.current();
        LegacyTextRenderContext.BackgroundTransform transform = plan == null
                ? null
                : plan.backgroundTransform(dropShadow);
        if (transform == null) {
            glyph.renderEffect(effect, matrix, vertices, light);
            return;
        }
        glyph.renderEffect(
                effect,
                matrix,
                new LegacyTextVertexConsumer(
                        vertices,
                        transform.transform(),
                        transform.bounds(),
                        transform.bounds(),
                        transform.offset()
                ),
                light
        );
    }

    @Inject(method = "finish", at = @At("RETURN"))
    private void butterflyApi$clearBackgroundEffect(
            int backgroundColor,
            float textStart,
            CallbackInfoReturnable<Float> cir
    ) {
        butterflyApi$backgroundEffect = null;
    }

    @Redirect(
            method = "accept",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;renderChar(Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;ZZFFFLorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;FFFFI)V"
            )
    )
    private void butterflyApi$renderStyledGlyph(
            Font font,
            BakedGlyph glyph,
            boolean italic,
            boolean bold,
            float x,
            float y,
            float depth,
            Matrix4f matrix,
            VertexConsumer vertices,
            float red,
            float green,
            float blue,
            float alpha,
            int light
    ) {
        Style style = butterflyApi$currentStyle;
        if (style == null) {
            butterflyApi$render(font, glyph, italic, bold, x, y, depth, matrix, vertices, red, green, blue, alpha, light);
            return;
        }

        TextRenderStyle renderStyle = TextRenderStyles.get(style);
        TextEffect effect = TextEffects.effect(style);
        if (renderStyle == null && effect == null) {
            butterflyApi$render(font, glyph, italic, bold, x, y, depth, matrix, vertices, red, green, blue, alpha, light);
            return;
        }

        TextRenderBounds fallbackBounds = TextRenderBounds.of(x, y, x + 1.0F, y + 9.0F);
        LegacyTextRenderContext.Plan plan = LegacyTextRenderContext.current();
        TextRenderBounds glyphBounds = plan == null
                ? fallbackBounds
                : plan.glyphBounds(butterflyApi$currentIndex, fallbackBounds);
        TextRenderBounds clusterBounds = renderStyle == null || plan == null
                ? glyphBounds
                : plan.clusterBounds(renderStyle, glyphBounds);
        if (dropShadow && renderStyle != null && !renderStyle.shadow().draws()) {
            return;
        }
        int resolved = butterflyApi$resolveColor(
                renderStyle,
                clusterBounds,
                glyphBounds,
                red,
                green,
                blue,
                alpha,
                dropShadow
        );
        TextTransform transform = renderStyle == null
                ? TextTransform.NONE
                : dropShadow ? renderStyle.shadow().transform() : renderStyle.transform();
        float renderX = x;
        float renderY = y;
        if (dropShadow && renderStyle != null) {
            TextShadow shadow = renderStyle.shadow();
            if (!shadow.equals(TextShadow.DEFAULT)) {
                renderX = this.x + shadow.offsetX();
                renderY = this.y + shadow.offsetY();
            }
        }
        TextRenderOffset offset = effect == null
                ? TextRenderOffset.ZERO
                : effect.offset(new TextEffectRenderContext(
                        glyphBounds.minX(),
                        glyphBounds.minY(),
                        glyphBounds.maxX(),
                        glyphBounds.maxY(),
                        Math.round(glyphBounds.minX() * 31.0F + glyphBounds.minY() * 17.0F),
                        plan == null ? System.nanoTime() / 16_666_667L : plan.frameTime()
                ));
        VertexConsumer styledVertices = new LegacyTextVertexConsumer(
                vertices,
                resolved,
                transform,
                glyphBounds,
                clusterBounds,
                offset
        );
        butterflyApi$render(font, glyph, italic, bold, renderX, renderY, depth, matrix, styledVertices, red, green, blue, alpha, light);
    }

    @Unique
    private static int butterflyApi$resolveColor(
            TextRenderStyle renderStyle,
            TextRenderBounds clusterBounds,
            TextRenderBounds glyphBounds,
            float red,
            float green,
            float blue,
            float alpha,
            boolean shadow
    ) {
        int fallback = (Math.round(alpha * 255.0F) << 24)
                | (Math.round(red * 255.0F) << 16)
                | (Math.round(green * 255.0F) << 8)
                | Math.round(blue * 255.0F);
        if (renderStyle == null) {
            return fallback;
        }
        TextGradient gradient = renderStyle.gradient();
        int base = gradient == null
                ? shadow ? butterflyApi$scaleRgb(fallback, 4.0F) : fallback
                : gradient.colorAt(
                        clusterBounds,
                        glyphBounds,
                        fallback
                );
        if (!shadow) {
            int resolvedAlpha = Math.round(((base >>> 24) & 0xFF) * renderStyle.textOpacity());
            return (resolvedAlpha << 24) | (base & 0xFFFFFF);
        }
        TextShadow textShadow = renderStyle.shadow();
        int resolved = textShadow.color() == null
                ? butterflyApi$scaleRgb(base, 0.25F)
                : textShadow.color();
        int resolvedAlpha = Math.round(((resolved >>> 24) & 0xFF) * textShadow.opacity());
        return (resolvedAlpha << 24) | (resolved & 0xFFFFFF);
    }

    @Unique
    private static int butterflyApi$scaleRgb(int color, float scale) {
        int alpha = (color >>> 24) & 0xFF;
        int red = Math.min(255, Math.round(((color >>> 16) & 0xFF) * scale));
        int green = Math.min(255, Math.round(((color >>> 8) & 0xFF) * scale));
        int blue = Math.min(255, Math.round((color & 0xFF) * scale));
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    @Unique
    private static void butterflyApi$render(
            Font font,
            BakedGlyph glyph,
            boolean italic,
            boolean bold,
            float x,
            float y,
            float depth,
            Matrix4f matrix,
            VertexConsumer vertices,
            float red,
            float green,
            float blue,
            float alpha,
            int light
    ) {
        ((LegacyFontRenderCharInvoker) (Object) font).butterflyApi$renderChar(
                glyph,
                italic,
                bold,
                x,
                y,
                depth,
                matrix,
                vertices,
                red,
                green,
                blue,
                alpha,
                light
        );
    }
}
*///?}
