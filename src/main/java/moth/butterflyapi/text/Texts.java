package moth.butterflyapi.text;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class Texts {
    private Texts() {
    }

    public static TextBuilder builder() {
        return new TextBuilder();
    }

    public static TextBuilder builder(String text) {
        return builder().text(text);
    }

    public static TextBuilder builder(Text text) {
        return builder().append(text);
    }

    public static MutableText literal(String text) {
        return Text.literal(text);
    }

    public static MutableText translatable(String translationKey, Object... args) {
        return Text.translatable(translationKey, args);
    }

    public static MutableText color(String text, String hexColor) {
        return TextColors.literal(text, hexColor);
    }

    public static MutableText color(Text text, String hexColor) {
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

    public static MutableText shaky(String text) {
        return TextEffects.shaky(text);
    }

    public static MutableText shaky(String text, TextShakingOptions options) {
        return TextEffects.shaky(text, options);
    }

    public static MutableText shaky(Text text) {
        return TextEffects.shaky(text);
    }

    public static MutableText shaky(Text text, TextShakingOptions options) {
        return TextEffects.shaky(text, options);
    }

    public static Style shaky(Style style) {
        return TextEffects.shaky(style);
    }

    public static Style shaky(Style style, TextShakingOptions options) {
        return TextEffects.shaky(style, options);
    }

    public static MutableText font(Text text, Identifier font) {
        return TextFonts.font(text, font);
    }

    public static Style font(Style style, Identifier font) {
        return TextFonts.font(style, font);
    }

    public static Identifier registerFont(Identifier font) {
        return TextFonts.register(font);
    }
}
