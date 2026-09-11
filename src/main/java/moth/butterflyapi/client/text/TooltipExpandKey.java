package moth.butterflyapi.client.text;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public record TooltipExpandKey(Text name, BooleanSupplier pressed) {
    public static final TooltipExpandKey SHIFT = dualKey("Shift", InputUtil.GLFW_KEY_LEFT_SHIFT, InputUtil.GLFW_KEY_RIGHT_SHIFT);
    public static final TooltipExpandKey CONTROL = dualKey("Ctrl", InputUtil.GLFW_KEY_LEFT_CONTROL, InputUtil.GLFW_KEY_RIGHT_CONTROL);
    public static final TooltipExpandKey ALT = dualKey("Alt", InputUtil.GLFW_KEY_LEFT_ALT, InputUtil.GLFW_KEY_RIGHT_ALT);

    public TooltipExpandKey {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(pressed, "pressed");
    }

    public boolean isPressed() {
        return pressed.getAsBoolean();
    }

    public static TooltipExpandKey key(String name, int keyCode) {
        return key(Text.literal(Objects.requireNonNull(name, "name")), keyCode);
    }

    public static TooltipExpandKey key(Text name, int keyCode) {
        return new TooltipExpandKey(name, () -> {
            MinecraftClient client = MinecraftClient.getInstance();
            return client != null && InputUtil.isKeyPressed(client.getWindow(), keyCode);
        });
    }

    public static TooltipExpandKey keyBinding(KeyBinding keyBinding) {
        Objects.requireNonNull(keyBinding, "keyBinding");
        return new TooltipExpandKey(keyBinding.getBoundKeyLocalizedText(), keyBinding::isPressed);
    }

    public static TooltipExpandKey custom(Text name, BooleanSupplier pressed) {
        return new TooltipExpandKey(name, pressed);
    }

    private static TooltipExpandKey dualKey(String name, int leftKey, int rightKey) {
        return new TooltipExpandKey(Text.literal(name), () -> {
            MinecraftClient client = MinecraftClient.getInstance();
            return client != null
                    && (InputUtil.isKeyPressed(client.getWindow(), leftKey)
                    || InputUtil.isKeyPressed(client.getWindow(), rightKey));
        });
    }
}
