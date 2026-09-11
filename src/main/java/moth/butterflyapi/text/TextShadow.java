package moth.butterflyapi.text;

import org.jetbrains.annotations.Nullable;

public record TextShadow(
        boolean draws,
        @Nullable Integer color,
        float opacity,
        float offsetX,
        float offsetY,
        TextTransform transform
) {
    public static final TextShadow DEFAULT = new TextShadow(true, null, 1.0F, 1.0F, 1.0F, TextTransform.NONE);
    public static final TextShadow NONE = new TextShadow(false, null, 0.0F, 0.0F, 0.0F, TextTransform.NONE);

    public TextShadow {
        opacity = TextRenderStyle.clamp01(opacity);
        transform = transform == null ? TextTransform.NONE : transform;
    }

    public static TextShadow enabled() {
        return DEFAULT;
    }

    public static TextShadow disabled() {
        return NONE;
    }

    public static TextShadow color(int argb) {
        return DEFAULT.withColor(argb);
    }

    public static TextShadow offset(float x, float y) {
        return DEFAULT.withOffset(x, y);
    }

    public TextShadow withColor(int argb) {
        return new TextShadow(draws, argb, opacity, offsetX, offsetY, transform);
    }

    public TextShadow withoutColorOverride() {
        return new TextShadow(draws, null, opacity, offsetX, offsetY, transform);
    }

    public TextShadow withOpacity(float opacity) {
        return new TextShadow(draws, color, TextRenderStyle.clamp01(opacity), offsetX, offsetY, transform);
    }

    public TextShadow withOffset(float x, float y) {
        return new TextShadow(draws, color, opacity, x, y, transform);
    }

    public TextShadow withTransform(TextTransform transform) {
        return new TextShadow(draws, color, opacity, offsetX, offsetY, transform);
    }

    public TextShadow rotate(float degrees) {
        return withTransform(transform.rotate(degrees));
    }

    public TextShadow mirrorX() {
        return withTransform(transform.mirrorX());
    }

    public TextShadow mirrorY() {
        return withTransform(transform.mirrorY());
    }

    public TextShadow disabledCopy() {
        return new TextShadow(false, color, 0.0F, offsetX, offsetY, transform);
    }
}
