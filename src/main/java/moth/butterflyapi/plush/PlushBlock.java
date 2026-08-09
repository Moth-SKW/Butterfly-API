package moth.butterflyapi.plush;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.function.Supplier;

public class PlushBlock extends BlockWithEntity implements Waterloggable {
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final IntProperty MODE = IntProperty.of("mode", 0, 1);

    private static final VoxelShape SHAPE = Block.createCuboidShape(2, 0, 2, 14, 15, 14);

    private final PlushDefinition plushDefinition;
    private final Supplier<SoundEvent> clickSound;

    public PlushBlock(PlushDefinition plushDefinition, Supplier<SoundEvent> clickSound) {
        this(plushDefinition, clickSound, Objects.requireNonNull(plushDefinition, "plushDefinition").blockSettings());
    }

    private PlushBlock(PlushDefinition plushDefinition, Supplier<SoundEvent> clickSound, AbstractBlock.Settings settings) {
        super(settings);
        this.plushDefinition = plushDefinition;
        this.clickSound = Objects.requireNonNull(clickSound, "clickSound");
        setDefaultState(getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(WATERLOGGED, false)
                .with(MODE, 0));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return Block.createCodec(settings -> new PlushBlock(plushDefinition, clickSound, settings));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, MODE);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        boolean water = ctx.getWorld().getFluidState(ctx.getBlockPos()).isOf(Fluids.WATER);
        Direction face = ctx.getHorizontalPlayerFacing().getOpposite();
        return getDefaultState()
                .with(FACING, face)
                .with(WATERLOGGED, water)
                .with(MODE, 0);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
            BlockState state,
            net.minecraft.world.WorldView world,
            net.minecraft.world.tick.ScheduledTickView scheduledTickView,
            BlockPos pos,
            Direction dir,
            BlockPos neighborPos,
            BlockState neighbor,
            net.minecraft.util.math.random.Random random
    ) {
        if (state.get(WATERLOGGED)) {
            scheduledTickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, world, scheduledTickView, pos, dir, neighborPos, neighbor, random);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        int next = state.get(MODE) ^ 1;
        world.setBlockState(pos, state.with(MODE, next), Block.NOTIFY_ALL);

        SoundEvent sfx = clickSound.get();
        if (sfx != null) {
            world.playSound(null, pos, sfx, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }

        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof PlushBlockEntity plushBE) {
            plushBE.squish(1);
            plushBE.onUsed();
        }

        return ActionResult.CONSUME;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PlushBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return type == ButterflyPlushes.blockEntityType()
                ? (w, p, s, be) -> PlushBlockEntity.tick(w, p, s, (PlushBlockEntity) be)
                : null;
    }

    public PlushDefinition plushDefinition() {
        return plushDefinition;
    }
}
