package moth.butterflyapi;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ButterflyApiTags {
    public static final TagKey<Block> PLUSH_BLOCKS = block("plushes");
    public static final TagKey<Item> PLUSH_ITEMS = item("plushes");

    private ButterflyApiTags() {
    }

    public static TagKey<Block> block(String path) {
        return TagKey.create(Registries.BLOCK, ButterflyApi.id(path));
    }

    public static TagKey<Item> item(String path) {
        return TagKey.create(Registries.ITEM, ButterflyApi.id(path));
    }
}
