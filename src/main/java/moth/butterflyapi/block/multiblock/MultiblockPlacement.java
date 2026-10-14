package moth.butterflyapi.block.multiblock;

import java.util.Objects;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class MultiblockPlacement {
    private MultiblockPlacement() {
    }

    public static BlockPos partPos(BlockPos origin, Direction facing, MultiblockPart part) {
        return Objects.requireNonNull(origin, "origin").offset(part.offset(facing));
    }

    public static BlockPos originOf(BlockPos partPos, Direction facing, MultiblockPart part) {
        return Objects.requireNonNull(partPos, "partPos").subtract(part.offset(facing));
    }

    public static boolean canPlace(LevelReader world, BlockPos origin, Direction facing, MultiblockShape shape, BlockPlaceContext context) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(shape, "shape");
        Objects.requireNonNull(context, "context");
        Direction checkedFacing = MultiblockShape.horizontal(facing);
        return shape.parts().allMatch(part -> {
            BlockPos pos = partPos(origin, checkedFacing, part);
            if (world.isOutsideBuildHeight(pos)) {
                return false;
            }
            BlockPlaceContext partContext = BlockPlaceContext.at(context, pos, Direction.UP);
            return world.getBlockState(pos).canBeReplaced(partContext);
        });
    }

    public static void place(Level world, BlockPos origin, Direction facing, MultiblockShape shape, Function<MultiblockPart, BlockState> stateFactory) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(shape, "shape");
        Objects.requireNonNull(stateFactory, "stateFactory");
        Direction checkedFacing = MultiblockShape.horizontal(facing);
        shape.parts().forEach(part -> world.setBlock(partPos(origin, checkedFacing, part), stateFactory.apply(part), Block.UPDATE_ALL));
    }

    public static void remove(Level world, BlockPos origin, Direction facing, MultiblockShape shape, Block block) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(shape, "shape");
        Objects.requireNonNull(block, "block");
        Direction checkedFacing = MultiblockShape.horizontal(facing);
        shape.parts().forEach(part -> {
            BlockPos pos = partPos(origin, checkedFacing, part);
            if (world.getBlockState(pos).is(block)) {
                world.destroyBlock(pos, false);
            }
        });
    }
}
