package moth.butterflyapi.particle;

public record ParticleColor(float red, float green, float blue, float alpha) {
    public ParticleColor {
        red = validateChannel(red, "red");
        green = validateChannel(green, "green");
        blue = validateChannel(blue, "blue");
        alpha = validateChannel(alpha, "alpha");
    }

    public static ParticleColor fromArgb(int argb) {
        return new ParticleColor(
                ((argb >> 16) & 0xFF) / 255.0F,
                ((argb >> 8) & 0xFF) / 255.0F,
                (argb & 0xFF) / 255.0F,
                ((argb >>> 24) & 0xFF) / 255.0F
        );
    }

    public static ParticleColor fromRgb(int rgb) {
        if ((rgb & 0xFF000000) != 0) {
            throw new IllegalArgumentException("RGB colors cannot contain an alpha channel");
        }
        return fromArgb(0xFF000000 | rgb);
    }

    public ParticleColor lerp(ParticleColor other, float progress) {
        if (other == null) {
            throw new NullPointerException("other");
        }
        float clamped = Math.max(0.0F, Math.min(1.0F, progress));
        return new ParticleColor(
                red + (other.red - red) * clamped,
                green + (other.green - green) * clamped,
                blue + (other.blue - blue) * clamped,
                alpha + (other.alpha - alpha) * clamped
        );
    }

    private static float validateChannel(float value, String name) {
        if (!Float.isFinite(value) || value < 0.0F || value > 1.0F) {
            throw new IllegalArgumentException("Particle color " + name + " must be between 0 and 1");
        }
        return value;
    }
}
