package moth.butterflyapi.item;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import moth.butterflyapi.mod.ModContext;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public final class ItemBuilder<T extends Item> {
    private final ModContext context;
    private final String path;
    private final Function<Item.Properties, T> factory;
    private UnaryOperator<Item.Properties> settingsConfigurer = UnaryOperator.identity();
    private ItemProgressBarDefinition progressBar;

    ItemBuilder(ModContext context, String path, Function<Item.Properties, T> factory) {
        this.context = Objects.requireNonNull(context, "context");
        this.path = Objects.requireNonNull(path, "path");
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    public ItemBuilder<T> settings(UnaryOperator<Item.Properties> configurer) {
        UnaryOperator<Item.Properties> previous = settingsConfigurer;
        UnaryOperator<Item.Properties> next = Objects.requireNonNull(configurer, "configurer");
        settingsConfigurer = settings -> Objects.requireNonNull(next.apply(previous.apply(settings)), "configured settings");
        return this;
    }

    public ItemBuilder<T> stacksTo(int maxStackSize) {
        return settings(settings -> settings.stacksTo(maxStackSize));
    }

    public ItemBuilder<T> durability(int durability) {
        return settings(settings -> settings.durability(durability));
    }

    public ItemBuilder<T> attributes(ItemAttributeModifiers attributes) {
        ItemAttributeModifiers checkedAttributes = Objects.requireNonNull(attributes, "attributes");
        return settings(settings -> settings.attributes(checkedAttributes));
    }

    public ItemBuilder<T> combatAttributes(double attackDamage, double displayedAttackSpeed) {
        Identifier id = context.id(path);
        return attributes(ItemUtils.combatAttributes(id, attackDamage, displayedAttackSpeed));
    }

    public ItemBuilder<T> progress(ItemProgressBarDefinition progressBar) {
        this.progressBar = Objects.requireNonNull(progressBar, "progressBar");
        return this;
    }

    public ItemBuilder<T> progress(
            java.util.function.ToDoubleFunction<net.minecraft.world.item.ItemStack> progress,
            Consumer<ItemProgressBarDefinition.Builder> configuration
    ) {
        ItemProgressBarDefinition.Builder builder = ItemProgressBarDefinition.builder(progress);
        Objects.requireNonNull(configuration, "configuration").accept(builder);
        return progress(builder.build());
    }

    public T register() {
        Item.Properties settings = Objects.requireNonNull(settingsConfigurer.apply(context.itemSettings(path)), "configured settings");
        T item = context.item(path, Objects.requireNonNull(factory.apply(settings), "item"));
        if (progressBar != null) {
            moth.butterflyapi.item.bar.ItemProgressBars.register(item, progressBar);
        }
        return item;
    }
}
