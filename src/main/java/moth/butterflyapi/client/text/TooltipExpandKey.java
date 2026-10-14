package moth.butterflyapi.client.text;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public record TooltipExpandKey(Component name, BooleanSupplier pressed) {
    public static final TooltipExpandKey SHIFT = dualKey("Shift", InputConstants.KEY_LSHIFT, InputConstants.KEY_RSHIFT);
    public static final TooltipExpandKey CONTROL = dualKey("Ctrl", InputConstants.KEY_LCONTROL, InputConstants.KEY_RCONTROL);
    public static final TooltipExpandKey ALT = dualKey("Alt", InputConstants.KEY_LALT, InputConstants.KEY_RALT);

    public TooltipExpandKey {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(pressed, "pressed");
    }

    public boolean isPressed() {
        return pressed.getAsBoolean();
    }

    public static TooltipExpandKey key(String name, int keyCode) {
        return key(Component.literal(Objects.requireNonNull(name, "name")), keyCode);
    }

    public static TooltipExpandKey key(Component name, int keyCode) {
        return new TooltipExpandKey(name, () -> {
            Minecraft client = Minecraft.getInstance();
            return client != null && InputConstants.isKeyDown(
//? if >=26.3 {
/*
                    keyCode
*///?} else if <1.21.11 {
/*
                    client.getWindow().getWindow(), keyCode
*///?} else {
                    client.getWindow(), keyCode
//?}
            );
        });
    }

    public static TooltipExpandKey keyBinding(KeyMapping keyBinding) {
        Objects.requireNonNull(keyBinding, "keyBinding");
        return new TooltipExpandKey(keyBinding.getTranslatedKeyMessage(), keyBinding::isDown);
    }

    public static TooltipExpandKey custom(Component name, BooleanSupplier pressed) {
        return new TooltipExpandKey(name, pressed);
    }

    private static TooltipExpandKey dualKey(String name, int leftKey, int rightKey) {
        return new TooltipExpandKey(Component.literal(name), () -> {
            Minecraft client = Minecraft.getInstance();
            return client != null
                    && (InputConstants.isKeyDown(
//? if >=26.3 {
/*
                    leftKey
*///?} else if <1.21.11 {
/*
                    client.getWindow().getWindow(), leftKey
*///?} else {
                    client.getWindow(), leftKey
//?}
            )
                    || InputConstants.isKeyDown(
//? if >=26.3 {
/*
                    rightKey
*///?} else if <1.21.11 {
/*
                    client.getWindow().getWindow(), rightKey
*///?} else {
                    client.getWindow(), rightKey
//?}
            ));
        });
    }
}
