package moth.butterflyapi.block.multiblock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.Objects;
import java.util.function.Function;

public final class MultiblockPlacement {
    private MultiblockPlacement() {
    }

    public static BlockPos partPos(BlockPos origin, Direction facing, MultiblockPart part) {
        return Objects.requireNonNull(origin, "origin").add(part.offset(facing));
    }

    public static BlockPos originOf(BlockPos partPos, Direction facing, MultiblockPart part) {
        return Objects.requireNonNull(partPos, "partPos").subtract(part.offset(facing));
    }

    public static boolean canPlace(WorldView world, BlockPos origin, Direction facing, MultiblockShape shape, ItemPlacementContext context) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(shape, "shape");
        Objects.requireNonNull(context, "context");
        Direction checkedFacing = MultiblockShape.horizontal(facing);
        return shape.parts().allMatch(part -> {
            BlockPos pos = partPos(origin, checkedFacing, part);
            if (world.isOutOfHeightLimit(pos)) {
                return false;
            }
            ItemPlacementContext partContext = ItemPlacementContext.offset(context, pos, Direction.UP);
            return world.getBlockState(pos).canReplace(partContext);
        });
    }

    public static void place(World world, BlockPos origin, Direction facing, MultiblockShape shape, Function<MultiblockPart, BlockState> stateFactory) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(shape, "shape");
        Objects.requireNonNull(stateFactory, "stateFactory");
        Direction checkedFacing = MultiblockShape.horizontal(facing);
        shape.parts().forEach(part -> world.setBlockState(partPos(origin, checkedFacing, part), stateFactory.apply(part), Block.NOTIFY_ALL));
    }

    public static void remove(World world, BlockPos origin, Direction facing, MultiblockShape shape, Block block) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(shape, "shape");
        Objects.requireNonNull(block, "block");
        Direction checkedFacing = MultiblockShape.horizontal(facing);
        shape.parts().forEach(part -> {
            BlockPos pos = partPos(origin, checkedFacing, part);
            if (world.getBlockState(pos).isOf(block)) {
                world.breakBlock(pos, false);
            }
        });
    }
}
