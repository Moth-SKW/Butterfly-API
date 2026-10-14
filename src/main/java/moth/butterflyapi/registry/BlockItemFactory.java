package moth.butterflyapi.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

@FunctionalInterface
public interface BlockItemFactory<T extends Block> {
    Item create(T block);

    static <T extends Block> BlockItemFactory<T> simple() {
        return block -> new BlockItem(block, new Item.Properties());
    }
}
