package moth.butterflyapi.math;

import net.minecraft.util.math.MathHelper;

public final class Scalars {
    public static final double EPSILON = 1.0E-7D;

    private Scalars() {
    }

    public static double clamp01(double value) {
        return MathHelper.clamp(value, 0.0D, 1.0D);
    }

    public static float clamp01(float value) {
        return MathHelper.clamp(value, 0.0F, 1.0F);
    }

    public static double clamp(double value, double min, double max) {
        return MathHelper.clamp(value, min, max);
    }

    public static float clamp(float value, float min, float max) {
        return MathHelper.clamp(value, min, max);
    }

    public static int clamp(int value, int min, int max) {
        return MathHelper.clamp(value, min, max);
    }

    public static double square(double value) {
        return value * value;
    }

    public static double cube(double value) {
        return value * value * value;
    }

    public static boolean isZero(double value) {
        return Math.abs(value) <= EPSILON;
    }

    public static boolean nearlyEqual(double a, double b) {
        return Math.abs(a - b) <= EPSILON;
    }

    public static double lerp(double delta, double start, double end) {
        return MathHelper.lerp(delta, start, end);
    }

    public static float lerp(float delta, float start, float end) {
        return MathHelper.lerp(delta, start, end);
    }

    public static double inverseLerp(double value, double start, double end) {
        if (nearlyEqual(start, end)) {
            return 0.0D;
        }
        return (value - start) / (end - start);
    }

    public static double map(double value, double inStart, double inEnd, double outStart, double outEnd) {
        return lerp(inverseLerp(value, inStart, inEnd), outStart, outEnd);
    }

    public static double mapClamped(double value, double inStart, double inEnd, double outStart, double outEnd) {
        return lerp(clamp01(inverseLerp(value, inStart, inEnd)), outStart, outEnd);
    }

    public static double approach(double value, double target, double step) {
        if (step < 0.0D) {
            throw new IllegalArgumentException("step must be >= 0");
        }
        if (value < target) {
            return Math.min(value + step, target);
        }
        return Math.max(value - step, target);
    }

    public static double roundTo(double value, double step) {
        if (step <= 0.0D) {
            throw new IllegalArgumentException("step must be > 0");
        }
        return Math.round(value / step) * step;
    }
}
