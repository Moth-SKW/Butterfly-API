package moth.butterflyapi.itemgroup;

import java.util.Locale;
import java.util.Objects;

public final class HexColor {
    private static final int DEFAULT_TINT_ALPHA = 0x40;

    private HexColor() {
    }

    public static int parse(String hex) {
        String value = normalize(hex);
        if (value.length() != 6) {
            throw new IllegalArgumentException("Hex colors must use exactly six RGB digits: #RRGGBB");
        }
        return 0xFF000000 | parseDigits(value, hex);
    }

    public static int parseArgb(String hex) {
        String value = normalize(hex);
        if (value.length() == 6) {
            return 0xFF000000 | parseDigits(value, hex);
        }
        if (value.length() == 8) {
            return (int) Long.parseLong(validateDigits(value, hex), 16);
        }
        throw new IllegalArgumentException("ARGB colors must use #RRGGBB or #AARRGGBB");
    }

    public static int parseTint(String hex) {
        String value = normalize(hex);
        if (value.length() == 6) {
            return (DEFAULT_TINT_ALPHA << 24) | parseDigits(value, hex);
        }
        if (value.length() == 8) {
            return (int) Long.parseLong(validateDigits(value, hex), 16);
        }
        throw new IllegalArgumentException("Tint colors must use #RRGGBB or #AARRGGBB");
    }

    public static int withOpacity(String hex, float opacity) {
        if (!Float.isFinite(opacity) || opacity < 0.0F || opacity > 1.0F) {
            throw new IllegalArgumentException("Opacity must be between 0.0 and 1.0");
        }

        int alpha = Math.round(opacity * 255.0F);
        return (alpha << 24) | (parse(hex) & 0x00FFFFFF);
    }

    private static String normalize(String hex) {
        Objects.requireNonNull(hex, "hex");

        String value = hex.trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        }
        return value.toUpperCase(Locale.ROOT);
    }

    private static int parseDigits(String value, String original) {
        return Integer.parseInt(validateDigits(value, original), 16);
    }

    private static String validateDigits(String value, String original) {
        for (int index = 0; index < value.length(); index++) {
            if (Character.digit(value.charAt(index), 16) < 0) {
                throw new IllegalArgumentException("Invalid hex color: " + original);
            }
        }
        return value;
    }
}
