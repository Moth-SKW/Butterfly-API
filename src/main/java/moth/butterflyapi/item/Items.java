package moth.butterflyapi.item;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import moth.butterflyapi.item.bar.ItemProgressBars;
import moth.butterflyapi.mod.ModContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class Items {
    private final ModContext context;

    public Items(ModContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public <T extends Item> T create(String path, Function<Item.Properties, T> factory) {
        return context.item(path, factory);
    }

    public <T extends Item> ItemBuilder<T> builder(String path, Function<Item.Properties, T> factory) {
        return new ItemBuilder<>(context, path, factory);
    }

    public void progress(ItemLike item, ItemProgressBarDefinition definition) {
        ItemProgressBars.register(item, definition);
    }

    public void progress(
            ItemLike item,
            ToDoubleFunction<ItemStack> progress,
            Consumer<ItemProgressBarDefinition.Builder> configuration
    ) {
        ItemProgressBarDefinition.Builder builder = ItemProgressBarDefinition.builder(progress);
        Objects.requireNonNull(configuration, "configuration").accept(builder);
        progress(item, builder.build());
    }
}
