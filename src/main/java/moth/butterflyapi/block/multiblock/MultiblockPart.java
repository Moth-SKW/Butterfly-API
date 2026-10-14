package moth.butterflyapi.block.multiblock;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record MultiblockPart(int x, int y, int z) {
    public BlockPos offset(Direction facing) {
        Direction forward = MultiblockShape.horizontal(Objects.requireNonNull(facing, "facing"));
        Direction right = forward.getClockWise();
        return BlockPos.ZERO
                .relative(right, x)
                .above(y)
                .relative(forward, z);
    }
}
