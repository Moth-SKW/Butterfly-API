package moth.butterflyapi.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public record BlockRegistration<T extends Block>(T block, @Nullable Item item) {
}
