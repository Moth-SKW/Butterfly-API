package moth.butterflyapi.block;

import moth.butterflyapi.mod.ModContext;
import moth.butterflyapi.registry.BlockItemFactory;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.TallBlockItem;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class BlockBuilder<T extends Block> {
    private final ModContext context;
    private final String path;
    private final Function<AbstractBlock.Settings, T> blockFactory;
    private UnaryOperator<AbstractBlock.Settings> settingsConfigurer = UnaryOperator.identity();
    private BlockItemFactory<T> itemFactory;

    public BlockBuilder(ModContext context, String path, Function<AbstractBlock.Settings, T> blockFactory) {
        this.context = Objects.requireNonNull(context, "context");
        this.path = Objects.requireNonNull(path, "path");
        this.blockFactory = Objects.requireNonNull(blockFactory, "blockFactory");
        this.itemFactory = block -> new BlockItem(block, context.itemSettings(path));
    }

    public BlockBuilder<T> settings(UnaryOperator<AbstractBlock.Settings> configurer) {
        this.settingsConfigurer = Objects.requireNonNull(configurer, "configurer");
        return this;
    }

    public BlockBuilder<T> item() {
        this.itemFactory = block -> new BlockItem(block, context.itemSettings(path));
        return this;
    }

    public BlockBuilder<T> itemSettings(UnaryOperator<Item.Settings> configurer) {
        Objects.requireNonNull(configurer, "configurer");
        this.itemFactory = block -> new BlockItem(block, configurer.apply(context.itemSettings(path)));
        return this;
    }

    public BlockBuilder<T> itemFactory(BlockItemFactory<T> itemFactory) {
        this.itemFactory = Objects.requireNonNull(itemFactory, "itemFactory");
        return this;
    }

    public BlockBuilder<T> tallBlockItem() {
        this.itemFactory = block -> new TallBlockItem(block, context.itemSettings(path));
        return this;
    }

    public BlockBuilder<T> noItem() {
        this.itemFactory = null;
        return this;
    }

    public BlockRegistration<T> register() {
        AbstractBlock.Settings settings = Objects.requireNonNull(settingsConfigurer.apply(context.blockSettings(path)), "configured settings");
        T block = Objects.requireNonNull(blockFactory.apply(settings), "block");
        if (itemFactory == null) {
            return new BlockRegistration<>(context.blockOnly(path, block), null);
        }

        T registered = context.blockOnly(path, block);
        Item item = context.item(path, Objects.requireNonNull(itemFactory.create(registered), "block item"));
        return new BlockRegistration<>(registered, item);
    }
}
