package moth.butterflyapi.text;

import org.jetbrains.annotations.Nullable;

public record TextRenderStyle(
        @Nullable TextGradient gradient,
        float textOpacity,
        TextShadow shadow,
        TextTransform transform
) {
    public static final TextRenderStyle DEFAULT = new TextRenderStyle(null, 1.0F, TextShadow.DEFAULT, TextTransform.NONE);

    public TextRenderStyle {
        textOpacity = clamp01(textOpacity);
        shadow = shadow == null ? TextShadow.DEFAULT : shadow;
        transform = transform == null ? TextTransform.NONE : transform;
    }

    public TextRenderStyle withGradient(@Nullable TextGradient gradient) {
        return new TextRenderStyle(gradient, textOpacity, shadow, transform);
    }

    public TextRenderStyle withTextOpacity(float opacity) {
        return new TextRenderStyle(gradient, opacity, shadow, transform);
    }

    public TextRenderStyle withShadow(TextShadow shadow) {
        return new TextRenderStyle(gradient, textOpacity, shadow, transform);
    }

    public TextRenderStyle withTransform(TextTransform transform) {
        return new TextRenderStyle(gradient, textOpacity, shadow, transform);
    }

    public boolean isDefault() {
        return gradient == null
                && textOpacity == 1.0F
                && shadow.equals(TextShadow.DEFAULT)
                && transform.isNone();
    }

    public static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
