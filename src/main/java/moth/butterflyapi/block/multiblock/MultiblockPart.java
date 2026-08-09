package moth.butterflyapi.block.multiblock;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Objects;

public record MultiblockPart(int x, int y, int z) {
    public BlockPos offset(Direction facing) {
        Direction forward = MultiblockShape.horizontal(Objects.requireNonNull(facing, "facing"));
        Direction right = forward.rotateYClockwise();
        return BlockPos.ORIGIN
                .offset(right, x)
                .up(y)
                .offset(forward, z);
    }
}
