package moth.butterflyapi.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import java.util.Objects;
import java.util.function.Consumer;

public final class Texts {
    private Texts() {
    }

    public static TextBuilder builder() {
        return new TextBuilder();
    }

    public static TextBuilder builder(String text) {
        return builder().text(text);
    }

    public static TextBuilder builder(Component text) {
        return builder().append(text);
    }

    public static MutableComponent of(String text) {
        return literal(text);
    }

    public static MutableComponent of(String text, Consumer<TextBuilder> configuration) {
        TextBuilder builder = builder(text);
        Objects.requireNonNull(configuration, "configuration").accept(builder);
        return builder.build();
    }

    public static MutableComponent of(Component text, Consumer<TextBuilder> configuration) {
        TextBuilder builder = builder(text);
        Objects.requireNonNull(configuration, "configuration").accept(builder);
        return builder.build();
    }

    public static MutableComponent compose(Consumer<TextBuilder> composition) {
        TextBuilder builder = builder();
        Objects.requireNonNull(composition, "composition").accept(builder);
        return builder.build();
    }

    public static MutableComponent literal(String text) {
        return Component.literal(text);
    }

    public static MutableComponent translatable(String translationKey, Object... args) {
        return Component.translatable(translationKey, args);
    }

    public static MutableComponent color(String text, String hexColor) {
        return TextColors.literal(text, hexColor);
    }

    public static MutableComponent color(Component text, String hexColor) {
        return TextColors.color(text, hexColor);
    }

    public static int rgb(String hexColor) {
        return TextColors.rgb(hexColor);
    }

    public static int argb(String hexColor) {
        return TextColors.argb(hexColor);
    }

    public static String legacyHex(String hexColor) {
        return TextColors.legacyHex(hexColor);
    }

    public static Style renderStyle(Style style, TextRenderStyle renderStyle) {
        return TextRenderStyles.with(style, renderStyle);
    }

    public static Style clearRenderStyle(Style style) {
        return TextRenderStyles.clear(style);
    }

    public static MutableComponent shaky(String text) {
        return TextEffects.shaky(text);
    }

    public static MutableComponent shaky(String text, TextShakingOptions options) {
        return TextEffects.shaky(text, options);
    }

    public static MutableComponent shaky(Component text) {
        return TextEffects.shaky(text);
    }

    public static MutableComponent shaky(Component text, TextShakingOptions options) {
        return TextEffects.shaky(text, options);
    }

    public static Style shaky(Style style) {
        return TextEffects.shaky(style);
    }

    public static Style shaky(Style style, TextShakingOptions options) {
        return TextEffects.shaky(style, options);
    }

    public static MutableComponent font(Component text, Identifier font) {
        return TextFonts.font(text, font);
    }

    public static Style font(Style style, Identifier font) {
        return TextFonts.font(style, font);
    }

    public static Identifier registerFont(Identifier font) {
        return TextFonts.register(font);
    }
}
