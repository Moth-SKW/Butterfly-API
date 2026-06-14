package moth.butterflyapi.content.blocks;

import moth.butterflyapi.content.ContentBootstrap;
import moth.butterflyapi.content.data.BigRedButtonClicksState;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
public class BigRedButtonBlock extends Block {

    public enum MountFace implements StringIdentifiable {
        FLOOR("floor"),
        WALL("wall"),
        CEILING("ceiling");

        private final String id;

        MountFace(String id) {
            this.id = id;
        }

        @Override
        public String asString() {
            return id;
        }
    }

    public static final EnumProperty<MountFace> FACE = EnumProperty.of("face", MountFace.class);
    public static final DirectionProperty FACING = DirectionProperty.of("facing", Direction.Type.HORIZONTAL);
    public static final BooleanProperty PRESSED = BooleanProperty.of("pressed");

    private static final int PRESS_TICKS = 10;

    private static final VoxelShape FLOOR_SHAPE = Block.createCuboidShape(2, 0, 2, 14, 4, 14);
    private static final VoxelShape CEILING_SHAPE = Block.createCuboidShape(2, 12, 2, 14, 16, 14);

    private static final VoxelShape WALL_NORTH_SHAPE = Block.createCuboidShape(2, 2, 0, 14, 14, 4);
    private static final VoxelShape WALL_SOUTH_SHAPE = Block.createCuboidShape(2, 2, 12, 14, 14, 16);
    private static final VoxelShape WALL_WEST_SHAPE = Block.createCuboidShape(0, 2, 2, 4, 14, 14);
    private static final VoxelShape WALL_EAST_SHAPE = Block.createCuboidShape(12, 2, 2, 16, 14, 14);

    private static final long THRESH_1 = 1L;
    private static final long THRESH_100 = 100L;
    private static final long THRESH_10K = 10_000L;
    private static final long THRESH_1M = 1_000_000L;

    private static final Identifier ADV_1 = new Identifier(ContentBootstrap.MOD_ID, "big_red_button/click_1");
    private static final Identifier ADV_100 = new Identifier(ContentBootstrap.MOD_ID, "big_red_button/click_100");
    private static final Identifier ADV_10K = new Identifier(ContentBootstrap.MOD_ID, "big_red_button/click_10000");
    private static final Identifier ADV_1M = new Identifier(ContentBootstrap.MOD_ID, "big_red_button/click_1000000");

    public BigRedButtonBlock() {
        super(AbstractBlock.Settings.create()
                .strength(0.3f)
                .sounds(BlockSoundGroup.METAL)
                .nonOpaque()
                .noCollision()
                .dynamicBounds()
        );
        setDefaultState(getStateManager().getDefaultState()
                .with(FACE, MountFace.FLOOR)
                .with(FACING, Direction.NORTH)
                .with(PRESSED, false)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACE, FACING, PRESSED);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction side = ctx.getSide();

        MountFace face;
        Direction facing;

        if (side == Direction.UP) {
            face = MountFace.FLOOR;
            facing = ctx.getHorizontalPlayerFacing().getOpposite();
        } else if (side == Direction.DOWN) {
            face = MountFace.CEILING;
            facing = ctx.getHorizontalPlayerFacing().getOpposite();
        } else {
            face = MountFace.WALL;
            facing = side.getOpposite();
        }

        return getDefaultState()
                .with(FACE, face)
                .with(FACING, facing)
                .with(PRESSED, false);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        MountFace face = state.get(FACE);
        Direction facing = state.get(FACING);

        BlockPos supportPos;
        Direction supportSide;

        switch (face) {
            case FLOOR -> {
                supportPos = pos.down();
                supportSide = Direction.UP;
            }
            case CEILING -> {
                supportPos = pos.up();
                supportSide = Direction.DOWN;
            }
            default -> {
                supportPos = pos.offset(facing);
                supportSide = facing.getOpposite();
            }
        }

        BlockState support = world.getBlockState(supportPos);
        return support.isSideSolidFullSquare(world, supportPos, supportSide);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState getStateForNeighborUpdate(BlockState state, Direction dir, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return state;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, net.minecraft.block.ShapeContext context) {
        return getButtonShape(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return getButtonShape(state);
    }

    private static VoxelShape getButtonShape(BlockState state) {
        MountFace face = state.get(FACE);
        Direction facing = state.get(FACING);

        return switch (face) {
            case FLOOR -> FLOOR_SHAPE;
            case CEILING -> CEILING_SHAPE;
            case WALL -> switch (facing) {
                case NORTH -> WALL_NORTH_SHAPE;
                case SOUTH -> WALL_SOUTH_SHAPE;
                case WEST -> WALL_WEST_SHAPE;
                case EAST -> WALL_EAST_SHAPE;
                default -> WALL_NORTH_SHAPE;
            };
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        if (state.get(PRESSED)) {
            return ActionResult.CONSUME;
        }

        world.setBlockState(pos, state.with(PRESSED, true), Block.NOTIFY_ALL);
        updateRedstoneNeighbors(state, world, pos);
        world.scheduleBlockTick(pos, this, PRESS_TICKS);

        world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.8f, 1.0f);

        if (player instanceof ServerPlayerEntity sp) {
            MinecraftServer server = sp.getServer();
            if (server != null) {
                ServerWorld overworld = server.getOverworld();
                if (overworld != null) {
                    long total = BigRedButtonClicksState.get(overworld).increment(sp.getUuid());
                    maybeGrantAdvancements(sp, total);
                }
            }
        }

        return ActionResult.CONSUME;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.get(PRESSED)) {
            return;
        }

        world.setBlockState(pos, state.with(PRESSED, false), Block.NOTIFY_ALL);
        updateRedstoneNeighbors(state, world, pos);
        world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, 0.6f, 1.0f);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(PRESSED) ? 15 : 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(PRESSED) && getSupportDirection(state) == direction ? 15 : 0;
    }

    private static void maybeGrantAdvancements(ServerPlayerEntity player, long totalClicks) {
        if (totalClicks >= THRESH_1) {
            grant(player, ADV_1);
        }
        if (totalClicks >= THRESH_100) {
            grant(player, ADV_100);
        }
        if (totalClicks >= THRESH_10K) {
            grant(player, ADV_10K);
        }
        if (totalClicks >= THRESH_1M) {
            grant(player, ADV_1M);
        }
    }

    private static void grant(ServerPlayerEntity player, Identifier advancementId) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }

        Advancement adv = server.getAdvancementLoader().get(advancementId);
        if (adv == null) {
            return;
        }

        AdvancementProgress progress = player.getAdvancementTracker().getProgress(adv);
        if (progress.isDone()) {
            return;
        }

        for (String criterion : progress.getUnobtainedCriteria()) {
            player.getAdvancementTracker().grantCriterion(adv, criterion);
        }
    }

    private static void updateRedstoneNeighbors(BlockState state, World world, BlockPos pos) {
        world.updateNeighborsAlways(pos, state.getBlock());
        world.updateNeighborsAlways(pos.offset(getSupportDirection(state)), state.getBlock());
    }

    private static Direction getSupportDirection(BlockState state) {
        return switch (state.get(FACE)) {
            case FLOOR -> Direction.DOWN;
            case CEILING -> Direction.UP;
            case WALL -> state.get(FACING);
        };
    }
}
