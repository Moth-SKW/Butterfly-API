package moth.butterflyapi.text;

import moth.butterflyapi.ButterflyApi;
import net.minecraft.resources.Identifier;
import java.util.Objects;

public final class ShakyTextEffect implements TextEffect {
    public static final Identifier ID = ButterflyApi.id("shaky");
    public static final ShakyTextEffect DEFAULT = new ShakyTextEffect(TextShakingOptions.DEFAULT);

    private final TextShakingOptions options;

    public ShakyTextEffect(TextShakingOptions options) {
        this.options = Objects.requireNonNull(options, "options");
    }

    @Override
    public Identifier id() {
        return ID;
    }

    public TextShakingOptions options() {
        return options;
    }

    @Override
    public TextRenderOffset offset(TextEffectRenderContext context) {
        if (options.strength() == 0.0F) {
            return TextRenderOffset.ZERO;
        }
        int key = context.glyphKey();
        double time = context.frameTime() * 0.075D * options.speed();
        double x;
        double y;
        switch (options.profile()) {
            case WAVE -> {
                double phase = time + key * 0.31D + options.seed() * 0.001D;
                x = Math.sin(phase) * 0.75D;
                y = Math.cos(phase * 1.31D) * 0.55D;
            }
            case DRIFT -> {
                double phase = time * 0.65D + key * 0.17D + options.seed() * 0.001D;
                x = Math.sin(phase) * 0.65D + Math.sin(phase * 0.37D) * 0.35D;
                y = Math.cos(phase * 0.81D) * 0.65D;
            }
            case JITTER -> {
                long sample = hash(options.seed() + key * 31L + context.frameTime() / 2L);
                x = unit(sample) * 2.0D - 1.0D;
                y = unit(sample >>> 24) * 2.0D - 1.0D;
            }
            default -> {
                x = 0.0D;
                y = 0.0D;
            }
        }
        return new TextRenderOffset(
                subpixel(x * options.strength(), key),
                subpixel(y * options.strength(), key * 31L + 17L)
        );
    }

    private static long hash(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= value >>> 33;
        return value;
    }

    private static double unit(long value) {
        return (value & 0xFFFFFFL) / (double) 0xFFFFFFL;
    }

    private static float subpixel(double value, long salt) {
        double nearestPixel = Math.rint(value);
        if (Math.abs(value - nearestPixel) >= 0.04D) {
            return (float) value;
        }
        double nudge = (salt & 1L) == 0L ? 0.37D : -0.37D;
        return (float) (value + nudge);
    }
}
