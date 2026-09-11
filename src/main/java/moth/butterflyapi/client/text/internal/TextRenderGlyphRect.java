package moth.butterflyapi.client.text.internal;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import moth.butterflyapi.text.TextGradient;
import moth.butterflyapi.text.TextRenderBounds;
import moth.butterflyapi.text.TextRenderOffset;
import moth.butterflyapi.text.TextRenderStyle;
import moth.butterflyapi.text.TextShadow;
import net.minecraft.client.font.TextDrawable;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public final class TextRenderGlyphRect implements TextDrawable.DrawnGlyphRect {
    private final TextDrawable.DrawnGlyphRect textGlyph;
    @Nullable
    private final TextDrawable.DrawnGlyphRect shadowGlyph;
    private final Style style;
    private final int textColor;
    private final int shadowColor;
    private final TextRenderStyle renderStyle;
    private final TextRenderBounds clusterBounds;
    private final TextRenderOffset effectOffset;

    public TextRenderGlyphRect(
            TextDrawable.DrawnGlyphRect textGlyph,
            @Nullable TextDrawable.DrawnGlyphRect shadowGlyph,
            Style style,
            int textColor,
            int shadowColor,
            TextRenderStyle renderStyle
    ) {
        this(
                textGlyph,
                shadowGlyph,
                style,
                textColor,
                shadowColor,
                renderStyle,
                boundsOf(textGlyph),
                TextRenderOffset.ZERO
        );
    }

    private TextRenderGlyphRect(
            TextDrawable.DrawnGlyphRect textGlyph,
            @Nullable TextDrawable.DrawnGlyphRect shadowGlyph,
            Style style,
            int textColor,
            int shadowColor,
            TextRenderStyle renderStyle,
            TextRenderBounds clusterBounds,
            TextRenderOffset effectOffset
    ) {
        this.textGlyph = textGlyph;
        this.shadowGlyph = shadowGlyph;
        this.style = style;
        this.textColor = textColor;
        this.shadowColor = shadowColor;
        this.renderStyle = renderStyle;
        this.clusterBounds = clusterBounds;
        this.effectOffset = effectOffset;
    }

    public TextRenderStyle renderStyle() {
        return renderStyle;
    }

    public TextRenderGlyphRect withRenderContext(TextRenderBounds clusterBounds, TextRenderOffset effectOffset) {
        return new TextRenderGlyphRect(textGlyph, shadowGlyph, style, textColor, shadowColor, renderStyle, clusterBounds, effectOffset);
    }

    @Override
    public Style style() {
        return style;
    }

    @Override
    public float getLeft() {
        return Math.min(textGlyph.getLeft(), shadowGlyph == null ? textGlyph.getLeft() : shadowGlyph.getLeft());
    }

    @Override
    public float getTop() {
        return Math.min(textGlyph.getTop(), shadowGlyph == null ? textGlyph.getTop() : shadowGlyph.getTop());
    }

    @Override
    public float getRight() {
        return Math.max(textGlyph.getRight(), shadowGlyph == null ? textGlyph.getRight() : shadowGlyph.getRight());
    }

    @Override
    public float getBottom() {
        return Math.max(textGlyph.getBottom(), shadowGlyph == null ? textGlyph.getBottom() : shadowGlyph.getBottom());
    }

    @Override
    public void render(Matrix4f matrix, VertexConsumer vertexConsumer, int light, boolean shadow) {
        TextRenderBounds glyphBounds = boundsOf(textGlyph);
        int baseTextColor = resolveBaseTextColor(glyphBounds);
        int resolvedTextColor = applyOpacity(baseTextColor, renderStyle.textOpacity());
        if (shadowGlyph != null) {
            int resolvedShadowColor = resolveShadowColor(baseTextColor);
            if (alpha(resolvedShadowColor) > 0) {
                shadowGlyph.render(
                        matrix,
                        new ColorOverrideVertexConsumer(
                                vertexConsumer,
                                resolvedShadowColor,
                                renderStyle.shadow().transform(),
                                glyphBounds,
                                clusterBounds,
                                effectOffset
                        ),
                        light,
                        false
                );
            }
        }
        if (alpha(resolvedTextColor) > 0) {
            textGlyph.render(
                    matrix,
                    new ColorOverrideVertexConsumer(
                            vertexConsumer,
                            resolvedTextColor,
                            renderStyle.transform(),
                            glyphBounds,
                            clusterBounds,
                            effectOffset
                    ),
                    light,
                    false
            );
        }
    }

    @Override
    public RenderLayer getRenderLayer(TextRenderer.TextLayerType layerType) {
        return textGlyph.getRenderLayer(layerType);
    }

    @Override
    public GpuTextureView textureView() {
        return textGlyph.textureView();
    }

    @Override
    public RenderPipeline getPipeline() {
        return textGlyph.getPipeline();
    }

    @Override
    public float getEffectiveMinX() {
        return getLeft();
    }

    @Override
    public float getEffectiveMinY() {
        return getTop();
    }

    @Override
    public float getEffectiveMaxX() {
        return getRight();
    }

    @Override
    public float getEffectiveMaxY() {
        return getBottom();
    }

    private int resolveBaseTextColor(TextRenderBounds glyphBounds) {
        TextGradient gradient = renderStyle.gradient();
        return gradient == null ? textColor : gradient.colorAt(clusterBounds, glyphBounds, textColor);
    }

    private int resolveShadowColor(int resolvedTextColor) {
        TextShadow shadow = renderStyle.shadow();
        if (!shadow.draws()) {
            return 0;
        }
        int color = shadow.color() != null ? shadow.color() : shadowColor;
        if (color == 0 && !shadow.equals(TextShadow.DEFAULT)) {
            color = scaleRgb(resolvedTextColor, 0.25F);
        } else if (renderStyle.gradient() != null && shadow.color() == null) {
            color = scaleRgb(resolvedTextColor, 0.25F);
        }
        return applyOpacity(color, shadow.opacity());
    }

    private static int applyOpacity(int color, float opacity) {
        int alpha = alpha(color);
        if (alpha == 0 && (color & 0xFFFFFF) != 0) {
            alpha = 255;
        }
        return (Math.round(alpha * TextRenderStyle.clamp01(opacity)) << 24) | (color & 0xFFFFFF);
    }

    private static int scaleRgb(int color, float scale) {
        int alpha = alpha(color);
        if (alpha == 0 && (color & 0xFFFFFF) != 0) {
            alpha = 255;
        }
        int red = Math.round(((color >> 16) & 0xFF) * scale);
        int green = Math.round(((color >> 8) & 0xFF) * scale);
        int blue = Math.round((color & 0xFF) * scale);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int alpha(int color) {
        return (color >>> 24) & 0xFF;
    }

    public static TextRenderBounds boundsOf(TextDrawable.DrawnGlyphRect glyph) {
        return TextRenderBounds.of(glyph.getLeft(), glyph.getTop(), glyph.getRight(), glyph.getBottom());
    }
}
