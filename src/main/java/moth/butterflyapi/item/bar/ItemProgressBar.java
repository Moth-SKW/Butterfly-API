package moth.butterflyapi.item.bar;

import java.util.Arrays;

public record ItemProgressBar(double progress, boolean pixelSnap, int... colors) {
    public ItemProgressBar {
        if (!Double.isFinite(progress) || progress < 0.0D) {
            progress = 0.0D;
        }
        if (colors == null || colors.length == 0) {
            throw new IllegalArgumentException("At least one progress bar color is required");
        }
        colors = Arrays.copyOf(colors, colors.length);
    }

    public static ItemProgressBar of(double progress, int color) {
        return new ItemProgressBar(progress, true, color);
    }

    public static ItemProgressBar of(double progress, int color, boolean pixelSnap) {
        return new ItemProgressBar(progress, pixelSnap, color);
    }

    public static ItemProgressBar layered(double progress, boolean pixelSnap, int... colors) {
        return new ItemProgressBar(progress, pixelSnap, colors);
    }

    @Override
    public int[] colors() {
        return Arrays.copyOf(colors, colors.length);
    }

    public int color(int index) {
        return colors[Math.floorMod(index, colors.length)];
    }

    public int color(long index) {
        return colors[Math.floorMod(index, colors.length)];
    }
}
