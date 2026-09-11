package moth.butterflyapi.text;

import moth.butterflyapi.text.internal.TextStyleEffects;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class TextEffects {
    public static final String SHAKY_FORMATTING = "\u00a7`";

    private TextEffects() {
    }

    public static void bootstrap() {
        if (TextEffectRegistry.get(ShakyTextEffect.ID).isEmpty()) {
            TextEffectRegistry.register(ShakyTextEffect.DEFAULT);
        }
    }

    public static MutableText shaky(String text) {
        return shaky(text, TextShakingOptions.DEFAULT);
    }

    public static MutableText shaky(String text, TextShakingOptions options) {
        return withEffect(Text.literal(SHAKY_FORMATTING + Objects.requireNonNull(text, "text")), new ShakyTextEffect(options));
    }

    public static MutableText shaky(Text text) {
        return shaky(text, TextShakingOptions.DEFAULT);
    }

    public static MutableText shaky(Text text, TextShakingOptions options) {
        Objects.requireNonNull(text, "text");
        String literal = text.getLiteralString();
        MutableText copy = literal == null
                ? text.copy()
                : Text.literal(SHAKY_FORMATTING + literal).setStyle(text.getStyle());
        for (Text sibling : text.getSiblings()) {
            copy.append(sibling.copy());
        }
        return withEffect(copy, new ShakyTextEffect(options));
    }

    public static MutableText withEffect(Text text, TextEffect effect) {
        Objects.requireNonNull(effect, "effect");
        return text.copy().styled(style -> withEffect(style, effect));
    }

    public static Style shaky(Style style) {
        return shaky(style, TextShakingOptions.DEFAULT);
    }

    public static Style shaky(Style style, TextShakingOptions options) {
        return withEffect(style, new ShakyTextEffect(options));
    }

    public static Style withEffect(Style style, TextEffect effect) {
        Objects.requireNonNull(style, "style");
        Objects.requireNonNull(effect, "effect");
        return TextStyleEffects.with(style, effect);
    }

    public static MutableText clear(Text text) {
        return text.copy().styled(TextEffects::clear);
    }

    public static Style clear(Style style) {
        Objects.requireNonNull(style, "style");
        return TextStyleEffects.with(style, null);
    }

    @Nullable
    public static TextEffect effect(Style style) {
        return TextStyleEffects.get(style);
    }
}
