package moth.butterflyapi.client.text;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;

public final class ClientTexts {
    private ClientTexts() {
    }

    public static void draw(DrawContext context, Text text, int x, int y, int color) {
        draw(context, text, x, y, color, true);
    }

    public static void draw(DrawContext context, Text text, int x, int y, int color, boolean shadow) {
        context.drawText(MinecraftClient.getInstance().textRenderer, text, x, y, color, shadow);
    }

    public static void draw(DrawContext context, TextRenderer renderer, Text text, int x, int y, int color, boolean shadow) {
        context.drawText(renderer, text, x, y, color, shadow);
    }

    public static void centered(DrawContext context, Text text, int centerX, int y, int color) {
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, text, centerX, y, color);
    }

    public static void tooltip(DrawContext context, Text text, int x, int y) {
        context.drawTooltip(MinecraftClient.getInstance().textRenderer, text, x, y);
    }

    public static void tooltip(DrawContext context, List<Text> lines, int x, int y) {
        context.drawTooltip(MinecraftClient.getInstance().textRenderer, lines, x, y);
    }

    public static void actionBar(Text text) {
        MinecraftClient.getInstance().inGameHud.setOverlayMessage(Objects.requireNonNull(text, "text"), false);
    }

    public static void title(Text text) {
        MinecraftClient.getInstance().inGameHud.setTitle(Objects.requireNonNull(text, "text"));
    }

    public static void subtitle(Text text) {
        MinecraftClient.getInstance().inGameHud.setSubtitle(Objects.requireNonNull(text, "text"));
    }
}
