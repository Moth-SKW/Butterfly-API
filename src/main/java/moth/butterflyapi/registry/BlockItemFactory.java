package moth.butterflyapi.registry;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

@FunctionalInterface
public interface BlockItemFactory<T extends Block> {
    Item create(T block);

    static <T extends Block> BlockItemFactory<T> simple() {
        return block -> new BlockItem(block, new Item.Settings());
    }
}
