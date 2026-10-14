package moth.butterflyapi.client.mixin;

//? if >=1.21.11 {
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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(targets = "net.minecraft.client.gui.Font$PreparedTextBuilder")
public abstract class TextRendererDrawerMixin {
    @Shadow
    @Final
    private List<TextRenderable.Styled> glyphs;
    private final Map<TextRenderStyle, TextRenderBounds> butterflyApi$clusterBounds = new HashMap<>();

    @Inject(method = "visit", at = @At("HEAD"))
    private void butterflyApi$cacheClusterBounds(CallbackInfo ci) {
        butterflyApi$clusterBounds.clear();
        for (TextRenderable.Styled glyph : glyphs) {
            TextRenderStyle renderStyle = glyph instanceof TextRenderGlyphRect styledGlyph
                    ? styledGlyph.renderStyle()
                    : TextRenderStyles.get(glyph.style());
            if (renderStyle == null) {
                continue;
            }
            TextRenderBounds bounds = TextRenderGlyphRect.boundsOf(glyph);
            butterflyApi$clusterBounds.merge(renderStyle, bounds, TextRenderBounds::union);
        }
    }

    @Redirect(
            method = "accept(ILnet/minecraft/network/chat/Style;Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;createGlyph(FFIILnet/minecraft/network/chat/Style;FF)Lnet/minecraft/client/gui/font/TextRenderable$Styled;"
            )
    )
    private TextRenderable.Styled butterflyApi$createStyledGlyph(
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
            return bakedGlyph.createGlyph(x, y, color, shadowColor, style, boldOffset, shadowOffset);
        }
        TextRenderable.Styled textGlyph = bakedGlyph.createGlyph(x, y, 0xFFFFFFFF, 0, style, boldOffset, 0.0F);
        if (textGlyph == null) {
            return null;
        }
        TextShadow shadow = renderStyle.shadow();
        boolean customShadow = !shadow.equals(TextShadow.DEFAULT);
        boolean shouldDrawShadow = shadow.draws()
                && shadow.opacity() > 0.0F
                && (shadowColor != 0 || customShadow || shadow.color() != null);
        TextRenderable.Styled shadowGlyph = null;
        if (shouldDrawShadow) {
            float shadowX = x + (customShadow ? shadow.offsetX() : shadowOffset);
            float shadowY = y + (customShadow ? shadow.offsetY() : shadowOffset);
            shadowGlyph = bakedGlyph.createGlyph(shadowX, shadowY, 0xFFFFFFFF, 0, style, boldOffset, 0.0F);
        }
        return new TextRenderGlyphRect(textGlyph, shadowGlyph, style, color, shadowColor, renderStyle);
    }

    @Redirect(
            method = "visit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font$GlyphVisitor;acceptGlyph(Lnet/minecraft/client/gui/font/TextRenderable$Styled;)V"
            )
    )
    private void butterflyApi$drawTextEffect(Font.GlyphVisitor drawer, TextRenderable.Styled glyph) {
        TextRenderStyle renderStyle = glyph instanceof TextRenderGlyphRect styledGlyph ? styledGlyph.renderStyle() : TextRenderStyles.get(glyph.style());
        TextEffect effect = TextEffects.effect(glyph.style());
        if (renderStyle == null && effect == null) {
            drawer.acceptGlyph(glyph);
            return;
        }
        TextRenderOffset offset = butterflyApi$effectOffset(glyph, effect);
        if (glyph instanceof TextRenderGlyphRect styledGlyph) {
            drawer.acceptGlyph(styledGlyph.withRenderContext(butterflyApi$clusterBounds(glyph, renderStyle), offset));
            return;
        }
        if (offset.isZero()) {
            drawer.acceptGlyph(glyph);
            return;
        }
        drawer.acceptGlyph(new OffsetGlyphRect(glyph, offset.x(), offset.y()));
    }

    private TextRenderOffset butterflyApi$effectOffset(TextRenderable.Styled glyph, TextEffect effect) {
        int glyphKey = Math.round(glyph.activeLeft() * 31.0F + glyph.activeTop() * 17.0F);
        long frameTime = System.nanoTime() / 16_666_667L;
        if (effect == null) {
            return TextRenderOffset.ZERO;
        }
        return effect.offset(new TextEffectRenderContext(
                glyph.activeLeft(),
                glyph.activeTop(),
                glyph.activeRight(),
                glyph.activeBottom(),
                glyphKey,
                frameTime
        ));
    }

    private TextRenderBounds butterflyApi$clusterBounds(TextRenderable.Styled glyph, TextRenderStyle renderStyle) {
        TextRenderBounds bounds = butterflyApi$clusterBounds.get(renderStyle);
        return bounds == null ? TextRenderGlyphRect.boundsOf(glyph) : bounds;
    }
}
//?}
