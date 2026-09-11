package moth.butterflyapi.client.text;

import moth.butterflyapi.text.Texts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.text.Text;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class TooltipActions {
    private static final Map<String, Boolean> PRESSED = new ConcurrentHashMap<>();

    private TooltipActions() {
    }

    public static void add(List<Text> tooltip, TooltipExpandKey key, String actionName, Runnable action) {
        add(tooltip, key, defaultPrompt(key, Text.literal(actionName)), actionName, action);
    }

    public static void add(List<Text> tooltip, TooltipExpandKey key, Text actionName, Runnable action) {
        add(tooltip, key, defaultPrompt(key, actionName), actionName.getString(), action);
    }

    public static void add(List<Text> tooltip, TooltipExpandKey key, Text prompt, String actionId, Runnable action) {
        Objects.requireNonNull(tooltip, "tooltip");
        Objects.requireNonNull(prompt, "prompt");
        tooltip.add(prompt);
        runWhenPressed(key, actionId, action);
    }

    public static void link(List<Text> tooltip, TooltipExpandKey key, String label, String uri) {
        link(tooltip, key, Text.literal(label), URI.create(Objects.requireNonNull(uri, "uri")));
    }

    public static void link(List<Text> tooltip, TooltipExpandKey key, Text label, URI uri) {
        Objects.requireNonNull(uri, "uri");
        add(tooltip, key, defaultPrompt(key, label), "link:" + uri, () -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                ConfirmLinkScreen.open(client.currentScreen, uri);
            }
        });
    }

    static void runWhenPressed(TooltipExpandKey key, String actionId, Runnable action) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(actionId, "actionId");
        Objects.requireNonNull(action, "action");
        String stateKey = key.name().getString() + "|" + actionId;
        boolean pressed = key.isPressed();
        boolean wasPressed = PRESSED.getOrDefault(stateKey, false);
        if (pressed && !wasPressed) {
            action.run();
        }
        if (pressed) {
            PRESSED.put(stateKey, true);
        } else {
            PRESSED.remove(stateKey);
        }
    }

    public static Text defaultPrompt(TooltipExpandKey key, Text actionName) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(actionName, "actionName");
        return Texts.builder("Press ")
                .color("#8A8A8A")
                .append(key.name())
                .color("#FFD447")
                .text(" for ")
                .color("#8A8A8A")
                .append(actionName)
                .color("#7CFF6B")
                .build();
    }
}
