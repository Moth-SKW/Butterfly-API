package moth.butterflyapi.block.multiblock;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AbstractMultiblockBlock extends Block {
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final IntProperty PART_X = IntProperty.of("part_x", 0, 1);
    public static final IntProperty PART_Y = IntProperty.of("part_y", 0, 3);
    public static final IntProperty PART_Z = IntProperty.of("part_z", 0, 1);
    private static final ThreadLocal<Boolean> UPDATING_PARTS = ThreadLocal.withInitial(() -> false);

    private final MultiblockShape shape;

    protected AbstractMultiblockBlock(MultiblockShape shape, AbstractBlock.Settings settings) {
        super(settings);
        this.shape = Objects.requireNonNull(shape, "shape");
        setDefaultState(getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(PART_X, 0)
                .with(PART_Y, 0)
                .with(PART_Z, 0));
    }

    public MultiblockShape shape() {
        return shape;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction facing = context.getHorizontalPlayerFacing().getOpposite();
        BlockPos origin = context.getBlockPos();
        if (!MultiblockPlacement.canPlace(context.getWorld(), origin, facing, shape, context)) {
            return null;
        }
        return stateForPart(facing, new MultiblockPart(0, 0, 0));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (world.isClient()) {
            return;
        }
        Direction facing = state.get(FACING);
        BlockPos origin = originOf(pos, state);
        runUpdatingParts(() -> MultiblockPlacement.place(world, origin, facing, shape, part -> stateForPart(facing, part)));
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return shape.contains(partOf(state));
    }

    @Override
    protected BlockState getStateForNeighborUpdate(
            BlockState state,
            WorldView world,
            ScheduledTickView tickView,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            Random random
    ) {
        if (!UPDATING_PARTS.get() && !isComplete(world, pos, state)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        super.onStateReplaced(state, world, pos, moved);
        if (moved || UPDATING_PARTS.get()) {
            return;
        }

        runUpdatingParts(() -> MultiblockPlacement.remove(world, originOf(pos, state), state.get(FACING), shape, this));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART_X, PART_Y, PART_Z);
    }

    protected BlockState stateForPart(Direction facing, MultiblockPart part) {
        if (!shape.contains(part)) {
            throw new IllegalArgumentException("Part " + part + " is outside shape " + shape);
        }
        return getDefaultState()
                .with(FACING, MultiblockShape.horizontal(facing))
                .with(PART_X, part.x())
                .with(PART_Y, part.y())
                .with(PART_Z, part.z());
    }

    protected MultiblockPart partOf(BlockState state) {
        return new MultiblockPart(state.get(PART_X), state.get(PART_Y), state.get(PART_Z));
    }

    protected BlockPos originOf(BlockPos pos, BlockState state) {
        return MultiblockPlacement.originOf(pos, state.get(FACING), partOf(state));
    }

    private boolean isComplete(WorldView world, BlockPos pos, BlockState state) {
        if (!shape.contains(partOf(state))) {
            return false;
        }

        Direction facing = state.get(FACING);
        BlockPos origin = originOf(pos, state);
        return shape.parts().allMatch(part -> {
            BlockState partState = world.getBlockState(MultiblockPlacement.partPos(origin, facing, part));
            return partState.isOf(this)
                    && partState.get(FACING) == facing
                    && partOf(partState).equals(part);
        });
    }

    private static void runUpdatingParts(Runnable action) {
        boolean wasUpdating = UPDATING_PARTS.get();
        UPDATING_PARTS.set(true);
        try {
            action.run();
        } finally {
            UPDATING_PARTS.set(wasUpdating);
        }
    }
}
