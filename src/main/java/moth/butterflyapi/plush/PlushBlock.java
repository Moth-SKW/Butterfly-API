package moth.butterflyapi.plush;

import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PlushBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty MODE = IntegerProperty.create("mode", 0, 1);

    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 15, 14);

    private final PlushDefinition plushDefinition;
    private final Supplier<SoundEvent> clickSound;

    public PlushBlock(PlushDefinition plushDefinition, Supplier<SoundEvent> clickSound) {
        this(plushDefinition, clickSound, Objects.requireNonNull(plushDefinition, "plushDefinition").blockSettings());
    }

    private PlushBlock(PlushDefinition plushDefinition, Supplier<SoundEvent> clickSound, BlockBehaviour.Properties settings) {
        super(settings);
        this.plushDefinition = plushDefinition;
        this.clickSound = Objects.requireNonNull(clickSound, "clickSound");
        registerDefaultState(getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false)
                .setValue(MODE, 0));
    }

//? if <26.3 {
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return Block.simpleCodec(settings -> new PlushBlock(plushDefinition, clickSound, settings));
    }
//?}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, MODE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        boolean water = ctx.getLevel().getFluidState(ctx.getClickedPos()).is(Fluids.WATER);
        Direction face = ctx.getHorizontalDirection().getOpposite();
        return defaultBlockState()
                .setValue(FACING, face)
                .setValue(WATERLOGGED, water)
                .setValue(MODE, 0);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

//? if >=1.21.11 {
    @Override
    public BlockState updateShape(
            BlockState state,
            net.minecraft.world.level.LevelReader world,
            net.minecraft.world.level.ScheduledTickAccess scheduledTickView,
            BlockPos pos,
            Direction dir,
            BlockPos neighborPos,
            BlockState neighbor,
            net.minecraft.util.RandomSource random
    ) {
        if (state.getValue(WATERLOGGED)) {
            scheduledTickView.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return super.updateShape(state, world, scheduledTickView, pos, dir, neighborPos, neighbor, random);
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
        if (state.getValue(WATERLOGGED)) {
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }
*///?}

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        int next = state.getValue(MODE) ^ 1;
        world.setBlock(pos, state.setValue(MODE, next), Block.UPDATE_ALL);

        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof PlushBlockEntity plushBE) {
            PlushDefinition definition = plushBE.plushDefinition();
            SoundEvent sfx = clickSound.get();
            if (sfx != null && plushBE.tryPlayInteractionSound(world.getGameTime(), definition.soundCooldownTicks())) {
                float pitch = definition.minimumSoundPitch()
                        + world.getRandom().nextFloat() * (definition.maximumSoundPitch() - definition.minimumSoundPitch());
                world.playSound(null, pos, sfx, SoundSource.BLOCKS, definition.soundVolume(), pitch);
            }
            plushBE.squish(1);
            plushBE.onUsed();
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlushBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return type == ButterflyPlushes.blockEntityType()
                ? (w, p, s, be) -> PlushBlockEntity.tick(w, p, s, (PlushBlockEntity) be)
                : null;
    }

    public PlushDefinition plushDefinition() {
        return plushDefinition;
    }
}
