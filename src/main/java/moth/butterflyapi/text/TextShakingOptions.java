package moth.butterflyapi.text;

public record TextShakingOptions(
        float strength,
        ShakyTextProfile profile,
        float speed,
        long seed
) {
    public static final TextShakingOptions DEFAULT = new TextShakingOptions(
            1.25F,
            ShakyTextProfile.JITTER,
            1.0F,
            0x51A7E11L
    );

    public TextShakingOptions {
        if (strength < 0.0F) {
            throw new IllegalArgumentException("strength must be >= 0");
        }
        if (speed <= 0.0F) {
            throw new IllegalArgumentException("speed must be > 0");
        }
        profile = profile == null ? ShakyTextProfile.JITTER : profile;
    }

    public static TextShakingOptions letters(float strength) {
        return DEFAULT.withStrength(strength);
    }

    public TextShakingOptions withStrength(float strength) {
        return new TextShakingOptions(strength, profile, speed, seed);
    }

    public TextShakingOptions withProfile(ShakyTextProfile profile) {
        return new TextShakingOptions(strength, profile, speed, seed);
    }

    public TextShakingOptions withSpeed(float speed) {
        return new TextShakingOptions(strength, profile, speed, seed);
    }

    public TextShakingOptions withSeed(long seed) {
        return new TextShakingOptions(strength, profile, speed, seed);
    }
}
