package moth.butterflyapi.text;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class TextBuilder {
    private final MutableComponent root = Component.empty();
    private Style style = Style.EMPTY;
    private MutableComponent last;
    private int lastIndex = -1;

    TextBuilder() {
    }

    public TextBuilder text(String text) {
        return append(Component.literal(Objects.requireNonNull(text, "text")));
    }

//? if <1.21.11 {
/*
    public TextBuilder drawString(String text) {
        return text(text);
    }
*///?}

    public TextBuilder literal(String text) {
        return text(text);
    }

    public TextBuilder translatable(String translationKey, Object... args) {
        return append(Component.translatable(Objects.requireNonNull(translationKey, "translationKey"), args));
    }

    public TextBuilder append(Component text) {
        MutableComponent segment = Objects.requireNonNull(text, "text").copy().withStyle(style);
        root.append(segment);
        last = segment;
        lastIndex = root.getSiblings().size() - 1;
        markLastLiteralIfShaky();
        return this;
    }

    public TextBuilder append(TextBuilder builder) {
        return append(Objects.requireNonNull(builder, "builder").build());
    }

    public TextBuilder line(String text) {
        if (!isEmpty()) {
            newline();
        }
        return text(text);
    }

    public TextBuilder line(Component text) {
        if (!isEmpty()) {
            newline();
        }
        return append(text);
    }

    public TextBuilder blankLine() {
        return newline().newline();
    }

    public TextBuilder newline() {
        return text("\n");
    }

    public TextBuilder space() {
        return text(" ");
    }

    public TextBuilder color(String hexColor) {
        return style(style -> TextColors.color(style, hexColor));
    }

    public TextBuilder color(int rgb) {
        return style(style -> style.withColor(rgb));
    }

    public TextBuilder noColor() {
        return style(style -> style.withColor((TextColor) null));
    }

    public TextBuilder shadow(int argb) {
//? if <1.21.11 {
/*
        return renderStyle(renderStyle -> renderStyle.withShadow(renderStyle.shadow().withColor(argb)));
*///?} else {
        return style(style -> TextRenderStyles.update(
                style.withShadowColor(argb),
                renderStyle -> renderStyle.withShadow(renderStyle.shadow().withColor(argb))
        ));
//?}
    }

    public TextBuilder shadowColor(int argb) {
        return shadow(argb);
    }

    public TextBuilder shadowColor(String hexColor) {
        return shadowColor(TextColors.argb(hexColor));
    }

    public TextBuilder noShadow() {
//? if <1.21.11 {
/*
        return renderStyle(renderStyle -> renderStyle.withShadow(TextShadow.disabled()));
*///?} else {
        return style(style -> TextRenderStyles.update(
                style.withoutShadow(),
                renderStyle -> renderStyle.withShadow(TextShadow.disabled())
        ));
//?}
    }

    public TextBuilder shadow(TextShadow shadow) {
        return renderStyle(renderStyle -> renderStyle.withShadow(Objects.requireNonNull(shadow, "shadow")));
    }

    public TextBuilder shadowOffset(float x, float y) {
        return renderStyle(renderStyle -> renderStyle.withShadow(renderStyle.shadow().withOffset(x, y)));
    }

    public TextBuilder shadowOpacity(float opacity) {
        return renderStyle(renderStyle -> renderStyle.withShadow(renderStyle.shadow().withOpacity(opacity)));
    }

    public TextBuilder shadowTransform(TextTransform transform) {
        return renderStyle(renderStyle -> renderStyle.withShadow(renderStyle.shadow().withTransform(Objects.requireNonNull(transform, "transform"))));
    }

    public TextBuilder shadowRotate(float degrees) {
        return renderStyle(renderStyle -> renderStyle.withShadow(renderStyle.shadow().rotate(degrees)));
    }

    public TextBuilder shadowMirrorX() {
        return renderStyle(renderStyle -> renderStyle.withShadow(renderStyle.shadow().mirrorX()));
    }

    public TextBuilder shadowMirrorY() {
        return renderStyle(renderStyle -> renderStyle.withShadow(renderStyle.shadow().mirrorY()));
    }

    public TextBuilder gradient(TextGradient gradient) {
        return renderStyle(renderStyle -> renderStyle.withGradient(Objects.requireNonNull(gradient, "gradient")));
    }

    public TextBuilder gradientHorizontal(int... colors) {
        return gradient(TextGradient.horizontal(colors));
    }

    public TextBuilder gradientHorizontal(String... colors) {
        return gradient(TextGradient.horizontal(colors));
    }

    public TextBuilder gradient(String firstColor, String secondColor) {
        return gradientHorizontal(firstColor, secondColor);
    }

    public TextBuilder gradientVertical(int... colors) {
        return gradient(TextGradient.vertical(colors));
    }

    public TextBuilder gradientVertical(String... colors) {
        return gradient(TextGradient.vertical(colors));
    }

    public TextBuilder gradientAdvanced(float rotationDegrees, int... colors) {
        return gradient(TextGradient.advanced(rotationDegrees, colors));
    }

    public TextBuilder gradientAdvanced(float rotationDegrees, String... colors) {
        return gradient(TextGradient.advanced(rotationDegrees, colors));
    }

    public TextBuilder clearGradient() {
        return renderStyle(renderStyle -> renderStyle.withGradient(null));
    }

    public TextBuilder opacity(float opacity) {
        return textOpacity(opacity);
    }

    public TextBuilder textOpacity(float opacity) {
        return renderStyle(renderStyle -> renderStyle.withTextOpacity(opacity));
    }

    public TextBuilder transform(TextTransform transform) {
        return renderStyle(renderStyle -> renderStyle.withTransform(Objects.requireNonNull(transform, "transform")));
    }

    public TextBuilder textOffset(float x, float y) {
        return renderStyle(renderStyle -> renderStyle.withTransform(renderStyle.transform().offset(x, y)));
    }

    public TextBuilder rotate(float degrees) {
        return renderStyle(renderStyle -> renderStyle.withTransform(renderStyle.transform().rotate(degrees)));
    }

    public TextBuilder mirrorX() {
        return renderStyle(renderStyle -> renderStyle.withTransform(renderStyle.transform().mirrorX()));
    }

    public TextBuilder mirrorY() {
        return renderStyle(renderStyle -> renderStyle.withTransform(renderStyle.transform().mirrorY()));
    }

    public TextBuilder flipLetterX() {
        return renderStyle(renderStyle -> renderStyle.withTransform(renderStyle.transform().withScope(TextTransformScope.GLYPH).flipAcrossX()));
    }

    public TextBuilder flipLetterY() {
        return renderStyle(renderStyle -> renderStyle.withTransform(renderStyle.transform().withScope(TextTransformScope.GLYPH).flipAcrossY()));
    }

    public TextBuilder flipClusterX() {
        return renderStyle(renderStyle -> renderStyle.withTransform(renderStyle.transform().withScope(TextTransformScope.CLUSTER).flipAcrossX()));
    }

    public TextBuilder flipClusterY() {
        return renderStyle(renderStyle -> renderStyle.withTransform(renderStyle.transform().withScope(TextTransformScope.CLUSTER).flipAcrossY()));
    }

    public TextBuilder flipTextX() {
        return flipClusterX();
    }

    public TextBuilder flipTextY() {
        return flipClusterY();
    }

    public TextBuilder clearRenderStyle() {
        return style(TextRenderStyles::clear);
    }

    public TextBuilder bold() {
        return bold(true);
    }

    public TextBuilder bold(boolean enabled) {
        return style(style -> style.withBold(enabled));
    }

    public TextBuilder italic() {
        return italic(true);
    }

    public TextBuilder italic(boolean enabled) {
        return style(style -> style.withItalic(enabled));
    }

    public TextBuilder underline() {
        return underline(true);
    }

    public TextBuilder underline(boolean enabled) {
        return style(style -> style.withUnderlined(enabled));
    }

    public TextBuilder strikethrough() {
        return strikethrough(true);
    }

    public TextBuilder strikethrough(boolean enabled) {
        return style(style -> style.withStrikethrough(enabled));
    }

    public TextBuilder obfuscated() {
        return obfuscated(true);
    }

    public TextBuilder obfuscated(boolean enabled) {
        return style(style -> style.withObfuscated(enabled));
    }

    public TextBuilder formatted(ChatFormatting formatting) {
        return style(style -> style.applyFormat(Objects.requireNonNull(formatting, "formatting")));
    }

    public TextBuilder formatted(ChatFormatting... formatting) {
        return style(style -> style.applyFormats(Objects.requireNonNull(formatting, "formatting")));
    }

    public TextBuilder font(Identifier font) {
        return style(style -> TextFonts.font(style, font));
    }

    public TextBuilder shaky() {
        return shaky(TextShakingOptions.DEFAULT);
    }

    public TextBuilder shaky(float strength) {
        return shaky(TextShakingOptions.letters(strength));
    }

    public TextBuilder shaky(TextShakingOptions options) {
        return style(style -> TextEffects.shaky(style, options)).markLastLiteralIfShaky();
    }

    public TextBuilder effect(TextEffect effect) {
        return style(style -> TextEffects.withEffect(style, effect));
    }

    public TextBuilder clearEffect() {
        return style(TextEffects::clear);
    }

    public TextBuilder click(ClickEvent event) {
        return style(style -> style.withClickEvent(event));
    }

    public TextBuilder openUrl(String url) {
        return openUrl(URI.create(Objects.requireNonNull(url, "url")));
    }

    public TextBuilder openUrl(URI uri) {
//? if <1.21.11 {
/*
        return click(new ClickEvent(ClickEvent.Action.OPEN_URL, Objects.requireNonNull(uri, "uri").toString()));
*///?} else {
        return click(new ClickEvent.OpenUrl(Objects.requireNonNull(uri, "uri")));
//?}
    }

    public TextBuilder runCommand(String command) {
//? if <1.21.11 {
/*
        return click(new ClickEvent(ClickEvent.Action.RUN_COMMAND, Objects.requireNonNull(command, "command")));
*///?} else {
        return click(new ClickEvent.RunCommand(Objects.requireNonNull(command, "command")));
//?}
    }

    public TextBuilder suggestCommand(String command) {
//? if <1.21.11 {
/*
        return click(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, Objects.requireNonNull(command, "command")));
*///?} else {
        return click(new ClickEvent.SuggestCommand(Objects.requireNonNull(command, "command")));
//?}
    }

    public TextBuilder copyToClipboard(String value) {
//? if <1.21.11 {
/*
        return click(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, Objects.requireNonNull(value, "value")));
*///?} else {
        return click(new ClickEvent.CopyToClipboard(Objects.requireNonNull(value, "value")));
//?}
    }

    public TextBuilder hover(HoverEvent event) {
        return style(style -> style.withHoverEvent(event));
    }

    public TextBuilder hover(String text) {
        return hover(Component.literal(Objects.requireNonNull(text, "text")));
    }

    public TextBuilder hover(Component text) {
//? if <1.21.11 {
/*
        return hover(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Objects.requireNonNull(text, "text")));
*///?} else {
        return hover(new HoverEvent.ShowText(Objects.requireNonNull(text, "text")));
//?}
    }

    public TextBuilder hover(ItemStack stack) {
//? if <1.21.11 {
/*
        return hover(new HoverEvent(
                HoverEvent.Action.SHOW_ITEM,
                new HoverEvent.ItemStackInfo(Objects.requireNonNull(stack, "stack").copy())
        ));
*///?} else {
        return hover(new HoverEvent.ShowItem(Objects.requireNonNull(stack, "stack").copy()));
//?}
    }

    public TextBuilder insertion(String insertion) {
        return style(style -> style.withInsertion(insertion));
    }

    public TextBuilder reset() {
        style = Style.EMPTY;
        last = null;
        lastIndex = -1;
        return this;
    }

    public TextBuilder resetStyle() {
        return reset();
    }

    public TextBuilder style(UnaryOperator<Style> updater) {
        Objects.requireNonNull(updater, "updater");
        style = Objects.requireNonNull(updater.apply(style), "style");
        if (last != null) {
            last.withStyle(updater);
        }
        return this;
    }

    public TextBuilder renderStyle(UnaryOperator<TextRenderStyle> updater) {
        return style(style -> TextRenderStyles.update(style, updater));
    }

    public MutableComponent build() {
        return root.copy();
    }

    public List<Component> buildLines() {
        if (isEmpty()) {
            return List.of();
        }
        List<Component> lines = new ArrayList<>();
        MutableComponent[] current = {Component.empty()};
        lines.add(current[0]);
        build().visit((partStyle, string) -> {
            int start = 0;
            for (int index = 0; index < string.length(); index++) {
                if (string.charAt(index) != '\n') {
                    continue;
                }
                appendLinePart(current[0], string.substring(start, index), partStyle);
                current[0] = Component.empty();
                lines.add(current[0]);
                start = index + 1;
            }
            appendLinePart(current[0], string.substring(start), partStyle);
            return Optional.empty();
        }, Style.EMPTY);
        return List.copyOf(lines);
    }

    public boolean isEmpty() {
        return root.getSiblings().isEmpty();
    }

    private static void appendLinePart(MutableComponent line, String text, Style style) {
        if (!text.isEmpty()) {
            line.append(Component.literal(text).setStyle(style));
        }
    }

    private TextBuilder markLastLiteralIfShaky() {
        if (last == null || TextEffects.effect(last.getStyle()) == null) {
            return this;
        }
        String literal = last.tryCollapseToString();
        if (literal == null || literal.startsWith(TextEffects.SHAKY_FORMATTING)) {
            return this;
        }
        MutableComponent marked = Component.literal(TextEffects.SHAKY_FORMATTING + literal).setStyle(last.getStyle());
        for (Component sibling : last.getSiblings()) {
            marked.append(sibling.copy());
        }
        root.getSiblings().set(lastIndex, marked);
        last = marked;
        return this;
    }
}
