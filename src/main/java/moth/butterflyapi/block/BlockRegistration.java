package moth.butterflyapi.block;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import org.jetbrains.annotations.Nullable;

public record BlockRegistration<T extends Block>(T block, @Nullable Item item) {
}
