package moth.butterflyapi.client.text;

import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class ClientTexts {
    private ClientTexts() {
    }

    public static void draw(GuiGraphics context, Component text, int x, int y, int color) {
        draw(context, text, x, y, color, true);
    }

    public static void draw(GuiGraphics context, Component text, int x, int y, int color, boolean shadow) {
        context.drawString(Minecraft.getInstance().font, text, x, y, color, shadow);
    }

    public static void draw(GuiGraphics context, Font renderer, Component text, int x, int y, int color, boolean shadow) {
        context.drawString(renderer, text, x, y, color, shadow);
    }

    public static void centered(GuiGraphics context, Component text, int centerX, int y, int color) {
        context.drawCenteredString(Minecraft.getInstance().font, text, centerX, y, color);
    }

    public static void tooltip(GuiGraphics context, Component text, int x, int y) {
//? if >=1.21.11 {
        context.setTooltipForNextFrame(Minecraft.getInstance().font, text, x, y);
//?}
    }

    public static void tooltip(GuiGraphics context, List<Component> lines, int x, int y) {
//? if >=1.21.11 {
        context.setComponentTooltipForNextFrame(Minecraft.getInstance().font, lines, x, y);
//?}
    }

    public static void actionBar(Component text) {
//? if >=26.2 {
/*
        Minecraft.getInstance().gui.hud.setOverlayMessage(Objects.requireNonNull(text, "text"), false);
*///?} else {
        Minecraft.getInstance().gui.setOverlayMessage(Objects.requireNonNull(text, "text"), false);
//?}
    }

    public static void title(Component text) {
//? if >=26.2 {
/*
        Minecraft.getInstance().gui.hud.setTitle(Objects.requireNonNull(text, "text"));
*///?} else {
        Minecraft.getInstance().gui.setTitle(Objects.requireNonNull(text, "text"));
//?}
    }

    public static void subtitle(Component text) {
//? if >=26.2 {
/*
        Minecraft.getInstance().gui.hud.setSubtitle(Objects.requireNonNull(text, "text"));
*///?} else {
        Minecraft.getInstance().gui.setSubtitle(Objects.requireNonNull(text, "text"));
//?}
    }
}
