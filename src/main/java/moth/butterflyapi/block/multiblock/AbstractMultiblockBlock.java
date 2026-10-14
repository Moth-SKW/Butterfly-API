package moth.butterflyapi.block.multiblock;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
//? if >=1.21.11 {
import net.minecraft.world.level.ScheduledTickAccess;
//?}
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public abstract class AbstractMultiblockBlock extends Block {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty PART_X = IntegerProperty.create("part_x", 0, 1);
    public static final IntegerProperty PART_Y = IntegerProperty.create("part_y", 0, 3);
    public static final IntegerProperty PART_Z = IntegerProperty.create("part_z", 0, 1);
    private static final ThreadLocal<Boolean> UPDATING_PARTS = ThreadLocal.withInitial(() -> false);

    private final MultiblockShape shape;

    protected AbstractMultiblockBlock(MultiblockShape shape, BlockBehaviour.Properties settings) {
        super(settings);
        this.shape = Objects.requireNonNull(shape, "shape");
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART_X, 0)
                .setValue(PART_Y, 0)
                .setValue(PART_Z, 0));
    }

    public MultiblockShape shape() {
        return shape;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos origin = context.getClickedPos();
        if (!MultiblockPlacement.canPlace(context.getLevel(), origin, facing, shape, context)) {
            return null;
        }
        return stateForPart(facing, new MultiblockPart(0, 0, 0));
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (world.isClientSide()) {
            return;
        }
        Direction facing = state.getValue(FACING);
        BlockPos origin = originOf(pos, state);
        runUpdatingParts(() -> MultiblockPlacement.place(world, origin, facing, shape, part -> stateForPart(facing, part)));
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return shape.contains(partOf(state));
    }

//? if >=1.21.11 {
    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader world,
            ScheduledTickAccess tickView,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        if (!UPDATING_PARTS.get() && !isComplete(world, pos, state)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }
//?} else {
/*
    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            net.minecraft.world.level.LevelAccessor world,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        if (!UPDATING_PARTS.get() && !isComplete(world, pos, state)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }
*///?}

//? if >=1.21.11 {
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
        if (moved || UPDATING_PARTS.get()) {
            return;
        }

        runUpdatingParts(() -> MultiblockPlacement.remove(world, originOf(pos, state), state.getValue(FACING), shape, this));
    }
//?} else {
/*
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState replacement, boolean moved) {
        super.onRemove(state, world, pos, replacement, moved);
        if (moved || UPDATING_PARTS.get() || state.getBlock() == replacement.getBlock() || world.isClientSide()) {
            return;
        }
        runUpdatingParts(() -> MultiblockPlacement.remove(world, originOf(pos, state), state.getValue(FACING), shape, this));
    }
*///?}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART_X, PART_Y, PART_Z);
    }

    protected BlockState stateForPart(Direction facing, MultiblockPart part) {
        if (!shape.contains(part)) {
            throw new IllegalArgumentException("Part " + part + " is outside shape " + shape);
        }
        return defaultBlockState()
                .setValue(FACING, MultiblockShape.horizontal(facing))
                .setValue(PART_X, part.x())
                .setValue(PART_Y, part.y())
                .setValue(PART_Z, part.z());
    }

    protected MultiblockPart partOf(BlockState state) {
        return new MultiblockPart(state.getValue(PART_X), state.getValue(PART_Y), state.getValue(PART_Z));
    }

    protected BlockPos originOf(BlockPos pos, BlockState state) {
        return MultiblockPlacement.originOf(pos, state.getValue(FACING), partOf(state));
    }

    private boolean isComplete(LevelReader world, BlockPos pos, BlockState state) {
        if (!shape.contains(partOf(state))) {
            return false;
        }

        Direction facing = state.getValue(FACING);
        BlockPos origin = originOf(pos, state);
        return shape.parts().allMatch(part -> {
            BlockState partState = world.getBlockState(MultiblockPlacement.partPos(origin, facing, part));
            return partState.is(this)
                    && partState.getValue(FACING) == facing
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
