package moth.butterflyapi.text.internal;

//? if <1.21.11 {
/*
import moth.butterflyapi.mixin.LegacyStyleTextEffectAccessor;
*///?} else {
import moth.butterflyapi.mixin.StyleTextEffectAccessor;
//?}
import moth.butterflyapi.text.TextEffect;
import moth.butterflyapi.text.TextRenderStyle;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

public final class TextStyleEffects {
    private TextStyleEffects() {
    }

    public static Style with(Style style, @Nullable TextEffect effect) {
        Style copy = copyOf(style);
        set(copy, effect);
        return copy;
    }

    public static Style withRenderStyle(Style style, @Nullable TextRenderStyle renderStyle) {
        Style copy = copyOf(style);
        setRenderStyle(copy, renderStyle);
        return copy;
    }

    @Nullable
    public static TextEffect get(Style style) {
        Object styleObject = style;
        if (styleObject instanceof TextEffectCarrier carrier) {
            return carrier.butterflyApi$textEffect();
        }
        return null;
    }

    public static void copy(Style source, Style target) {
        set(target, get(source));
        setRenderStyle(target, getRenderStyle(source));
    }

    public static void set(Style style, @Nullable TextEffect effect) {
        Object styleObject = style;
        if (styleObject instanceof TextEffectCarrier carrier) {
            carrier.butterflyApi$setTextEffect(effect);
        }
    }

    @Nullable
    public static TextRenderStyle getRenderStyle(Style style) {
        Object styleObject = style;
        if (styleObject instanceof TextRenderStyleCarrier carrier) {
            return carrier.butterflyApi$textRenderStyle();
        }
        return null;
    }

    public static void setRenderStyle(Style style, @Nullable TextRenderStyle renderStyle) {
        Object styleObject = style;
        if (styleObject instanceof TextRenderStyleCarrier carrier) {
            carrier.butterflyApi$setTextRenderStyle(renderStyle);
        }
    }

    public static Style copyOf(Style style) {
//? if <1.21.11 {
/*
        LegacyStyleTextEffectAccessor access = (LegacyStyleTextEffectAccessor) (Object) style;
        Style copy = LegacyStyleTextEffectAccessor.butterflyApi$create(
                access.butterflyApi$getColor(),
                access.butterflyApi$getBold(),
                access.butterflyApi$getItalic(),
                access.butterflyApi$getUnderlined(),
                access.butterflyApi$getStrikethrough(),
                access.butterflyApi$getObfuscated(),
                access.butterflyApi$getClickEvent(),
                access.butterflyApi$getHoverEvent(),
                access.butterflyApi$getInsertion(),
                access.butterflyApi$getFont()
        );
        copy(style, copy);
        return copy;
*///?} else {
        StyleTextEffectAccessor access = (StyleTextEffectAccessor) (Object) style;
        Style copy = StyleTextEffectAccessor.butterflyApi$create(
                access.butterflyApi$getColor(),
                access.butterflyApi$getShadowColor(),
                access.butterflyApi$getBold(),
                access.butterflyApi$getItalic(),
                access.butterflyApi$getUnderlined(),
                access.butterflyApi$getStrikethrough(),
                access.butterflyApi$getObfuscated(),
                access.butterflyApi$getClickEvent(),
                access.butterflyApi$getHoverEvent(),
                access.butterflyApi$getInsertion(),
                access.butterflyApi$getFont()
        );
        copy(style, copy);
        return copy;
//?}
    }
}
