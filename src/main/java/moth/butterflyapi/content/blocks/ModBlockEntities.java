package moth.butterflyapi.content.blocks;

import net.minecraft.block.entity.BlockEntityType;
import moth.butterflyapi.plush.ButterflyPlushes;

public final class ModBlockEntities {
    private static boolean initialized;

    public static BlockEntityType<?> PLUSH;

    private ModBlockEntities() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        ModBlocks.init();

        PLUSH = ButterflyPlushes.blockEntityType();
    }
}
