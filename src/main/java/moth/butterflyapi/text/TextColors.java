package moth.butterflyapi.text;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.Objects;

public final class TextColors {
    private TextColors() {
    }

    public static int rgb(String hexColor) {
        Objects.requireNonNull(hexColor, "hexColor");
        String value = hexColor.trim().toLowerCase(Locale.ROOT);
        if (value.startsWith("#")) {
            value = value.substring(1);
        } else if (value.startsWith("0x")) {
            value = value.substring(2);
        }
        if (value.length() != 6) {
            throw new IllegalArgumentException("Expected a 6-digit RGB hex color: " + hexColor);
        }
        return Integer.parseInt(value, 16) & 0xFFFFFF;
    }

    public static int argb(String hexColor) {
        Objects.requireNonNull(hexColor, "hexColor");
        String value = hexColor.trim().toLowerCase(Locale.ROOT);
        if (value.startsWith("#")) {
            value = value.substring(1);
        } else if (value.startsWith("0x")) {
            value = value.substring(2);
        }
        if (value.length() == 6) {
            return 0xFF000000 | (Integer.parseInt(value, 16) & 0xFFFFFF);
        }
        if (value.length() != 8) {
            throw new IllegalArgumentException("Expected a 6-digit RGB or 8-digit ARGB hex color: " + hexColor);
        }
        return (int) Long.parseLong(value, 16);
    }

    public static String legacyHex(String hexColor) {
        int rgb = rgb(hexColor);
        return String.format(Locale.ROOT, "\u00a7#%06X", rgb);
    }

    public static Style color(Style style, String hexColor) {
        return style.withColor(rgb(hexColor));
    }

    public static MutableText color(Text text, String hexColor) {
        return text.copy().styled(style -> color(style, hexColor));
    }

    public static MutableText literal(String text, String hexColor) {
        return Text.literal(text).styled(style -> color(style, hexColor));
    }
}
