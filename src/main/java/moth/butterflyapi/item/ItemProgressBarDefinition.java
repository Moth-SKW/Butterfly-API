package moth.butterflyapi.item;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import moth.butterflyapi.item.bar.ItemProgressBar;
import net.minecraft.world.item.ItemStack;

public final class ItemProgressBarDefinition {
    private final ToDoubleFunction<ItemStack> progress;
    private final Predicate<ItemStack> visibility;
    private final boolean pixelSnap;
    private final int[] colors;

    private ItemProgressBarDefinition(
            ToDoubleFunction<ItemStack> progress,
            Predicate<ItemStack> visibility,
            boolean pixelSnap,
            int[] colors
    ) {
        this.progress = Objects.requireNonNull(progress, "progress");
        this.visibility = Objects.requireNonNull(visibility, "visibility");
        this.pixelSnap = pixelSnap;
        if (colors.length == 0) {
            throw new IllegalArgumentException("At least one progress bar color is required");
        }
        this.colors = Arrays.copyOf(colors, colors.length);
    }

    public static Builder builder(ToDoubleFunction<ItemStack> progress) {
        return new Builder(progress);
    }

    public boolean isVisible(ItemStack stack) {
        return visibility.test(Objects.requireNonNull(stack, "stack"));
    }

    public ItemProgressBar bar(ItemStack stack) {
        ItemStack checkedStack = Objects.requireNonNull(stack, "stack");
        return bar(progress.applyAsDouble(checkedStack));
    }

    public ItemProgressBar bar(double progress) {
        return ItemProgressBar.layered(progress, pixelSnap, colors);
    }

    public boolean pixelSnap() {
        return pixelSnap;
    }

    public int[] colors() {
        return Arrays.copyOf(colors, colors.length);
    }

    public static final class Builder {
        private final ToDoubleFunction<ItemStack> progress;
        private Predicate<ItemStack> visibility = stack -> true;
        private boolean pixelSnap = true;
        private int[] colors = {0xFFFFFF};

        private Builder(ToDoubleFunction<ItemStack> progress) {
            this.progress = Objects.requireNonNull(progress, "progress");
        }

        public Builder visibleWhen(Predicate<ItemStack> visibility) {
            this.visibility = Objects.requireNonNull(visibility, "visibility");
            return this;
        }

        public Builder pixelSnap(boolean pixelSnap) {
            this.pixelSnap = pixelSnap;
            return this;
        }

        public Builder color(int color) {
            return colors(color);
        }

        public Builder colors(int... colors) {
            Objects.requireNonNull(colors, "colors");
            if (colors.length == 0) {
                throw new IllegalArgumentException("At least one progress bar color is required");
            }
            this.colors = Arrays.copyOf(colors, colors.length);
            return this;
        }

        public ItemProgressBarDefinition build() {
            return new ItemProgressBarDefinition(progress, visibility, pixelSnap, colors);
        }
    }
}
