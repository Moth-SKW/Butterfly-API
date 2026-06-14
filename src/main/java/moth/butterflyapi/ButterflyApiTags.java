package moth.butterflyapi;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public final class ButterflyApiTags {
    public static final TagKey<Block> PLUSH_BLOCKS = block("plushes");
    public static final TagKey<Item> PLUSH_ITEMS = item("plushes");

    private ButterflyApiTags() {
    }

    public static TagKey<Block> block(String path) {
        return TagKey.of(RegistryKeys.BLOCK, ButterflyApi.id(path));
    }

    public static TagKey<Item> item(String path) {
        return TagKey.of(RegistryKeys.ITEM, ButterflyApi.id(path));
    }
}
