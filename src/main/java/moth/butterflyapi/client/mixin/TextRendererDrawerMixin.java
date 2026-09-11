package moth.butterflyapi.client.mixin;

import moth.butterflyapi.client.text.internal.OffsetGlyphRect;
import moth.butterflyapi.client.text.internal.TextRenderGlyphRect;
import moth.butterflyapi.text.TextEffect;
import moth.butterflyapi.text.TextEffectRenderContext;
import moth.butterflyapi.text.TextEffects;
import moth.butterflyapi.text.TextRenderBounds;
import moth.butterflyapi.text.TextRenderOffset;
import moth.butterflyapi.text.TextRenderStyle;
import moth.butterflyapi.text.TextRenderStyles;
import moth.butterflyapi.text.TextShadow;
import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.font.TextDrawable;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Objects;

@Mixin(targets = "net.minecraft.client.font.TextRenderer$Drawer")
public abstract class TextRendererDrawerMixin {
    @Shadow
    @Final
    private List<TextDrawable.DrawnGlyphRect> drawnGlyphs;

    @Redirect(
            method = "accept(ILnet/minecraft/text/Style;Lnet/minecraft/client/font/BakedGlyph;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/font/BakedGlyph;create(FFIILnet/minecraft/text/Style;FF)Lnet/minecraft/client/font/TextDrawable$DrawnGlyphRect;"
            )
    )
    private TextDrawable.DrawnGlyphRect butterflyApi$createStyledGlyph(
            BakedGlyph bakedGlyph,
            float x,
            float y,
            int color,
            int shadowColor,
            Style style,
            float boldOffset,
            float shadowOffset
    ) {
        TextRenderStyle renderStyle = TextRenderStyles.get(style);
        if (renderStyle == null) {
            return bakedGlyph.create(x, y, color, shadowColor, style, boldOffset, shadowOffset);
        }
        TextDrawable.DrawnGlyphRect textGlyph = bakedGlyph.create(x, y, 0xFFFFFFFF, 0, style, boldOffset, 0.0F);
        if (textGlyph == null) {
            return null;
        }
        TextShadow shadow = renderStyle.shadow();
        boolean customShadow = !shadow.equals(TextShadow.DEFAULT);
        boolean shouldDrawShadow = shadow.draws()
                && shadow.opacity() > 0.0F
                && (shadowColor != 0 || customShadow || shadow.color() != null);
        TextDrawable.DrawnGlyphRect shadowGlyph = null;
        if (shouldDrawShadow) {
            float shadowX = x + (customShadow ? shadow.offsetX() : shadowOffset);
            float shadowY = y + (customShadow ? shadow.offsetY() : shadowOffset);
            shadowGlyph = bakedGlyph.create(shadowX, shadowY, 0xFFFFFFFF, 0, style, boldOffset, 0.0F);
        }
        return new TextRenderGlyphRect(textGlyph, shadowGlyph, style, color, shadowColor, renderStyle);
    }

    @Redirect(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/font/TextRenderer$GlyphDrawer;drawGlyph(Lnet/minecraft/client/font/TextDrawable$DrawnGlyphRect;)V"
            )
    )
    private void butterflyApi$drawTextEffect(TextRenderer.GlyphDrawer drawer, TextDrawable.DrawnGlyphRect glyph) {
        TextRenderStyle renderStyle = glyph instanceof TextRenderGlyphRect styledGlyph ? styledGlyph.renderStyle() : TextRenderStyles.get(glyph.style());
        TextEffect effect = TextEffects.effect(glyph.style());
        if (renderStyle == null && effect == null) {
            drawer.drawGlyph(glyph);
            return;
        }
        TextRenderOffset offset = butterflyApi$effectOffset(glyph, effect);
        if (glyph instanceof TextRenderGlyphRect styledGlyph) {
            drawer.drawGlyph(styledGlyph.withRenderContext(butterflyApi$clusterBounds(glyph, renderStyle), offset));
            return;
        }
        if (offset.isZero()) {
            drawer.drawGlyph(glyph);
            return;
        }
        drawer.drawGlyph(new OffsetGlyphRect(glyph, offset.x(), offset.y()));
    }

    private TextRenderOffset butterflyApi$effectOffset(TextDrawable.DrawnGlyphRect glyph, TextEffect effect) {
        int glyphKey = Math.round(glyph.getLeft() * 31.0F + glyph.getTop() * 17.0F);
        long frameTime = System.nanoTime() / 16_666_667L;
        if (effect == null) {
            return TextRenderOffset.ZERO;
        }
        return effect.offset(new TextEffectRenderContext(
                glyph.getLeft(),
                glyph.getTop(),
                glyph.getRight(),
                glyph.getBottom(),
                glyphKey,
                frameTime
        ));
    }

    private TextRenderBounds butterflyApi$clusterBounds(TextDrawable.DrawnGlyphRect glyph, TextRenderStyle renderStyle) {
        TextRenderBounds bounds = null;
        for (TextDrawable.DrawnGlyphRect candidate : drawnGlyphs) {
            TextRenderStyle candidateStyle = candidate instanceof TextRenderGlyphRect styledGlyph
                    ? styledGlyph.renderStyle()
                    : TextRenderStyles.get(candidate.style());
            if (!Objects.equals(candidateStyle, renderStyle)) {
                continue;
            }
            TextRenderBounds candidateBounds = TextRenderGlyphRect.boundsOf(candidate);
            bounds = bounds == null ? candidateBounds : bounds.union(candidateBounds);
        }
        return bounds == null ? TextRenderGlyphRect.boundsOf(glyph) : bounds;
    }
}
