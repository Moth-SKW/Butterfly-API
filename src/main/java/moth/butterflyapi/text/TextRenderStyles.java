package moth.butterflyapi.text;

import moth.butterflyapi.text.internal.TextStyleEffects;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.UnaryOperator;

public final class TextRenderStyles {
    private TextRenderStyles() {
    }

    public static Style with(Style style, TextRenderStyle renderStyle) {
        return TextStyleEffects.withRenderStyle(style, renderStyle);
    }

    public static Style update(Style style, UnaryOperator<TextRenderStyle> updater) {
        Objects.requireNonNull(updater, "updater");
        TextRenderStyle current = getOrDefault(style);
        TextRenderStyle updated = Objects.requireNonNull(updater.apply(current), "renderStyle");
        return with(style, updated.isDefault() ? null : updated);
    }

    public static Style clear(Style style) {
        return TextStyleEffects.withRenderStyle(style, null);
    }

    @Nullable
    public static TextRenderStyle get(Style style) {
        return TextStyleEffects.getRenderStyle(style);
    }

    public static TextRenderStyle getOrDefault(Style style) {
        TextRenderStyle renderStyle = get(style);
        return renderStyle == null ? TextRenderStyle.DEFAULT : renderStyle;
    }
}
