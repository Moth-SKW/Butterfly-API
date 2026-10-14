package moth.butterflyapi.item.bar;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToDoubleFunction;
import moth.butterflyapi.item.ItemProgressBarDefinition;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class ItemProgressBars {
    private static final Map<Item, ItemProgressBarProvider> PROVIDERS = new ConcurrentHashMap<>();

    private ItemProgressBars() {
    }

    public static void register(ItemLike item, ItemProgressBarProvider provider) {
        PROVIDERS.put(Objects.requireNonNull(item, "item").asItem(), Objects.requireNonNull(provider, "provider"));
    }

    public static void register(ItemLike item, ItemProgressBarDefinition definition) {
        ItemProgressBarDefinition checkedDefinition = Objects.requireNonNull(definition, "definition");
        register(item, stack -> checkedDefinition.isVisible(stack)
                ? List.of(checkedDefinition.bar(stack))
                : Collections.emptyList());
    }

    public static ItemProgressBarDefinition.Builder definition(ToDoubleFunction<ItemStack> progress) {
        return ItemProgressBarDefinition.builder(progress);
    }

    public static void register(ItemLike item, int color, ToDoubleFunction<ItemStack> progress) {
        register(item, color, true, progress);
    }

    public static void register(ItemLike item, int color, boolean pixelSnap, ToDoubleFunction<ItemStack> progress) {
        register(item, definition(progress).color(color).pixelSnap(pixelSnap).build());
    }

    public static void registerLayered(ItemLike item, boolean pixelSnap, ToDoubleFunction<ItemStack> progress, int... colors) {
        register(item, definition(progress).colors(colors).pixelSnap(pixelSnap).build());
    }

    public static boolean isRegistered(ItemLike item) {
        return PROVIDERS.containsKey(Objects.requireNonNull(item, "item").asItem());
    }

    public static void unregister(ItemLike item) {
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
