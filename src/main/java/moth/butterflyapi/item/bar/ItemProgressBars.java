package moth.butterflyapi.item.bar;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToDoubleFunction;

public final class ItemProgressBars {
    private static final Map<Item, ItemProgressBarProvider> PROVIDERS = new ConcurrentHashMap<>();

    private ItemProgressBars() {
    }

    public static void register(ItemConvertible item, ItemProgressBarProvider provider) {
        PROVIDERS.put(Objects.requireNonNull(item, "item").asItem(), Objects.requireNonNull(provider, "provider"));
    }

    public static void register(ItemConvertible item, int color, ToDoubleFunction<ItemStack> progress) {
        register(item, color, true, progress);
    }

    public static void register(ItemConvertible item, int color, boolean pixelSnap, ToDoubleFunction<ItemStack> progress) {
        Objects.requireNonNull(progress, "progress");
        register(item, stack -> List.of(ItemProgressBar.of(progress.applyAsDouble(stack), color, pixelSnap)));
    }

    public static void registerLayered(ItemConvertible item, boolean pixelSnap, ToDoubleFunction<ItemStack> progress, int... colors) {
        Objects.requireNonNull(progress, "progress");
        ItemProgressBar.layered(0.0D, pixelSnap, colors);
        register(item, stack -> List.of(ItemProgressBar.layered(progress.applyAsDouble(stack), pixelSnap, colors)));
    }

    public static boolean isRegistered(ItemConvertible item) {
        return PROVIDERS.containsKey(Objects.requireNonNull(item, "item").asItem());
    }

    public static void unregister(ItemConvertible item) {
        PROVIDERS.remove(Objects.requireNonNull(item, "item").asItem());
    }

    public static List<ItemProgressBar> bars(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Collections.emptyList();
        }
        ItemProgressBarProvider provider = PROVIDERS.get(stack.getItem());
        if (provider == null) {
            return Collections.emptyList();
        }
        List<ItemProgressBar> bars = provider.bars(stack);
        return bars == null ? Collections.emptyList() : bars;
    }
}
