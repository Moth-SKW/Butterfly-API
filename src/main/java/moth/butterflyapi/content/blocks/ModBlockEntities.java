package moth.butterflyapi.content.blocks;

import moth.butterflyapi.plush.ButterflyPlushes;
import net.minecraft.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static BlockEntityType<?> PLUSH;

    private ModBlockEntities() {
    }

    public static void init() {
        PLUSH = ButterflyPlushes.blockEntityType();
    }
}
