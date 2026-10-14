package moth.butterflyapi.text;

import java.util.Arrays;
import java.util.Objects;

public final class TextGradient {
    private static final int MAX_COLORS = 5;

    private final int[] colors;
    private final TextGradientOrientation orientation;
    private final float rotationDegrees;

    private TextGradient(TextGradientOrientation orientation, float rotationDegrees, int[] colors) {
        this.orientation = Objects.requireNonNull(orientation, "orientation");
        if (!Float.isFinite(rotationDegrees)) {
            throw new IllegalArgumentException("rotationDegrees must be finite");
        }
        this.rotationDegrees = rotationDegrees;
        Objects.requireNonNull(colors, "colors");
        if (colors.length < 2) {
            throw new IllegalArgumentException("A text gradient needs at least two colors.");
        }
        if (colors.length > MAX_COLORS) {
            throw new IllegalArgumentException("A text gradient supports up to five colors.");
        }
        this.colors = colors.clone();
    }

    public static TextGradient horizontal(int... colors) {
        return new TextGradient(TextGradientOrientation.HORIZONTAL, 0.0F, colors);
    }

    public static TextGradient horizontal(String... colors) {
        return horizontal(toRgb(colors));
    }

    public static TextGradient vertical(int... colors) {
        return new TextGradient(TextGradientOrientation.VERTICAL, 90.0F, colors);
    }

    public static TextGradient vertical(String... colors) {
        return vertical(toRgb(colors));
    }

    public static TextGradient advanced(float rotationDegrees, int... colors) {
        return new TextGradient(TextGradientOrientation.ADVANCED, rotationDegrees, colors);
    }

    public static TextGradient advanced(float rotationDegrees, String... colors) {
        return advanced(rotationDegrees, toRgb(colors));
    }

    public int[] colors() {
        return colors.clone();
    }

    public TextGradientOrientation orientation() {
        return orientation;
    }

    public float rotationDegrees() {
        return rotationDegrees;
    }

    public int colorAt(TextRenderBounds cluster, TextRenderBounds glyph, int fallbackArgb) {
        return withFallbackAlpha(sample(position(cluster, glyph)), fallbackArgb);
    }

    private float position(TextRenderBounds cluster, TextRenderBounds glyph) {
        float centerX = glyph.centerX();
        float centerY = glyph.centerY();
        if (orientation == TextGradientOrientation.HORIZONTAL) {
            return divide(centerX - cluster.minX(), cluster.width());
        }
        if (orientation == TextGradientOrientation.VERTICAL) {
            return divide(centerY - cluster.minY(), cluster.height());
        }
        double radians = Math.toRadians(rotationDegrees);
        float axisX = (float) Math.cos(radians);
        float axisY = (float) Math.sin(radians);
        float normalizedX = divide(centerX - cluster.minX(), cluster.width()) - 0.5F;
        float normalizedY = divide(centerY - cluster.minY(), cluster.height()) - 0.5F;
        float projection = normalizedX * axisX + normalizedY * axisY;
        float radius = Math.abs(axisX) * 0.5F + Math.abs(axisY) * 0.5F;
        return radius <= 0.0F ? 0.0F : clamp01((projection + radius) / (radius * 2.0F));
    }

    private int sample(float amount) {
        float scaled = clamp01(amount) * (colors.length - 1);
        int index = Math.min(colors.length - 2, (int) Math.floor(scaled));
        float local = scaled - index;
        return lerp(colors[index], colors[index + 1], local);
    }

    private static int lerp(int from, int to, float amount) {
        int fromR = (from >> 16) & 0xFF;
        int fromG = (from >> 8) & 0xFF;
        int fromB = from & 0xFF;
        int toR = (to >> 16) & 0xFF;
        int toG = (to >> 8) & 0xFF;
        int toB = to & 0xFF;
        return (channel(fromR, toR, amount) << 16)
                | (channel(fromG, toG, amount) << 8)
                | channel(fromB, toB, amount);
    }

    private static int channel(int from, int to, float amount) {
        return Math.round(from + (to - from) * amount);
    }

    private static int withFallbackAlpha(int rgb, int fallbackArgb) {
        return ((fallbackArgb >>> 24) << 24) | (rgb & 0xFFFFFF);
    }

    private static float divide(float value, float max) {
        return max <= 0.0F ? 0.0F : clamp01(value / max);
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static int[] toRgb(String[] colors) {
        Objects.requireNonNull(colors, "colors");
        int[] rgb = new int[colors.length];
        for (int index = 0; index < colors.length; index++) {
            rgb[index] = TextColors.rgb(colors[index]);
        }
        return rgb;
    }

    @Override
    public String toString() {
        return "TextGradient{" +
                "colors=" + Arrays.toString(colors) +
                ", orientation=" + orientation +
                ", rotationDegrees=" + rotationDegrees +
                '}';
    }
}
