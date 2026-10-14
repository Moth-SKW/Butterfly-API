package moth.butterflyapi.text;

import moth.butterflyapi.text.internal.TextStyleEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
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

    public static MutableComponent shaky(String text) {
        return shaky(text, TextShakingOptions.DEFAULT);
    }

    public static MutableComponent shaky(String text, TextShakingOptions options) {
        return withEffect(Component.literal(SHAKY_FORMATTING + Objects.requireNonNull(text, "text")), new ShakyTextEffect(options));
    }

    public static MutableComponent shaky(Component text) {
        return shaky(text, TextShakingOptions.DEFAULT);
    }

    public static MutableComponent shaky(Component text, TextShakingOptions options) {
        Objects.requireNonNull(text, "text");
        String literal = text.tryCollapseToString();
        MutableComponent copy = literal == null
                ? text.copy()
                : Component.literal(SHAKY_FORMATTING + literal).setStyle(text.getStyle());
        for (Component sibling : text.getSiblings()) {
            copy.append(sibling.copy());
        }
        return withEffect(copy, new ShakyTextEffect(options));
    }

    public static MutableComponent withEffect(Component text, TextEffect effect) {
        Objects.requireNonNull(effect, "effect");
        return text.copy().withStyle(style -> withEffect(style, effect));
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

    public static MutableComponent clear(Component text) {
        return text.copy().withStyle(TextEffects::clear);
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
