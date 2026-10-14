package moth.butterflyapi.block;

import java.util.Objects;
import java.util.function.Function;
import moth.butterflyapi.mod.ModContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class Blocks {
    private final ModContext context;

    public Blocks(ModContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public <T extends Block> T create(String path, Function<BlockBehaviour.Properties, T> factory) {
        return context.block(path, factory);
    }

    public <T extends Block> BlockBuilder<T> builder(
            String path,
            Function<BlockBehaviour.Properties, T> factory
    ) {
        return context.blockBuilder(path, factory);
    }
}
