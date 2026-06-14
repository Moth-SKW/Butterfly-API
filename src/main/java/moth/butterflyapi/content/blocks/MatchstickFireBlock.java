package moth.butterflyapi.content.blocks;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import moth.butterflyapi.content.mixin.FireBlockAccessor;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.SoulFireBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.dimension.NetherPortal;

public final class MatchstickFireBlock extends FireBlock {
    private static final int DIRECT_SPREAD_MULTIPLIER = 2;
    private static final int AIR_SPREAD_MULTIPLIER = 6;

    private final Object2IntMap<Block> burnChances;
    private final Object2IntMap<Block> spreadChances;
    private final float contactDamage;
    private final boolean soulVariant;

    public MatchstickFireBlock(AbstractBlock.Settings settings, float contactDamage, boolean soulVariant) {
        super(settings);
        this.burnChances = ((FireBlockAccessor) (Object) this).butterflyApi$getBurnChances();
        this.spreadChances = ((FireBlockAccessor) (Object) this).butterflyApi$getSpreadChances();
        this.contactDamage = contactDamage;
        this.soulVariant = soulVariant;

        FireBlock vanillaFire = (FireBlock) Blocks.FIRE;
        FireBlockAccessor vanillaFireAccessor = (FireBlockAccessor) vanillaFire;
        this.burnChances.putAll(vanillaFireAccessor.butterflyApi$getBurnChances());
        this.spreadChances.putAll(vanillaFireAccessor.butterflyApi$getSpreadChances());
    }

    public static BlockState getMatchstickState(BlockView world, BlockPos pos) {
        if (SoulFireBlock.isSoulBase(world.getBlockState(pos.down()))) {
            return ModBlocks.MATCHSTICK_SOUL_FIRE.getDefaultState();
        }

        return ModBlocks.MATCHSTICK_FIRE.stateForPosition(world, pos);
    }

    public static boolean canPlaceAt(World world, BlockPos pos, Direction direction) {
        if (!world.getBlockState(pos).isAir()) {
            return false;
        }

        return getMatchstickState(world, pos).canPlaceAt(world, pos) || shouldLightPortalAt(world, pos, direction);
    }

    public BlockState stateForPosition(BlockView world, BlockPos pos) {
        return super.getStateForPosition(world, pos);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (soulVariant) {
            return SoulFireBlock.isSoulBase(world.getBlockState(pos.down()));
        }

        return super.canPlaceAt(state, world, pos);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }

        return getStateWithAge(world, pos, state.get(AGE));
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity.isFireImmune()) {
            return;
        }

        entity.setFireTicks(entity.getFireTicks() + 1);
        if (entity.getFireTicks() == 0) {
            entity.setOnFireFor(8);
        }

        entity.damage(world.getDamageSources().inFire(), contactDamage);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.scheduleBlockTick(pos, this, getFireTickDelay(random));

        if (!world.getGameRules().getBoolean(net.minecraft.world.GameRules.DO_FIRE_TICK)) {
            return;
        }

        if (!state.canPlaceAt(world, pos)) {
            world.removeBlock(pos, false);
            return;
        }

        int age = state.get(AGE);

        if (world.isRaining() && this.isRainingAround(world, pos) && random.nextFloat() < 0.2f + age * 0.03f) {
            world.removeBlock(pos, false);
            return;
        }

        int newAge = Math.min(15, age + 1 + random.nextInt(2));
        if (age != newAge) {
            state = state.with(AGE, newAge);
            world.setBlockState(pos, state, 4);
            age = newAge;
        }

        if (!areBlocksAroundFlammable(world, pos)) {
            BlockPos belowPos = pos.down();
            if (!world.getBlockState(belowPos).isSideSolidFullSquare(world, belowPos, Direction.UP) || age > 3) {
                world.removeBlock(pos, false);
            }
            return;
        }

        if (age == 15 && random.nextInt(4) == 0 && !this.isFlammable(world.getBlockState(pos.down()))) {
            world.removeBlock(pos, false);
            return;
        }

        boolean humid = world.getBiome(pos).isIn(BiomeTags.INCREASED_FIRE_BURNOUT);
        int humidityPenalty = humid ? -50 : 0;

        trySpreadingFire(world, pos.east(), adjustedSpreadFactor(300 + humidityPenalty), random, age);
        trySpreadingFire(world, pos.west(), adjustedSpreadFactor(300 + humidityPenalty), random, age);
        trySpreadingFire(world, pos.down(), adjustedSpreadFactor(250 + humidityPenalty), random, age);
        trySpreadingFire(world, pos.up(), adjustedSpreadFactor(250 + humidityPenalty), random, age);
        trySpreadingFire(world, pos.north(), adjustedSpreadFactor(300 + humidityPenalty), random, age);
        trySpreadingFire(world, pos.south(), adjustedSpreadFactor(300 + humidityPenalty), random, age);

        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                for (int yOffset = -1; yOffset <= 4; yOffset++) {
                    if (xOffset == 0 && yOffset == 0 && zOffset == 0) {
                        continue;
                    }

                    int chanceBase = 100;
                    if (yOffset > 1) {
                        chanceBase += (yOffset - 1) * 100;
                    }

                    mutablePos.set(pos, xOffset, yOffset, zOffset);
                    int burnChance = getBurnChance(world, mutablePos);
                    if (burnChance <= 0) {
                        continue;
                    }

                    int spreadScore = (burnChance * AIR_SPREAD_MULTIPLIER + 40 + world.getDifficulty().getId() * 7) / (age + 30);
                    if (humid) {
                        spreadScore /= 2;
                    }

                    if (spreadScore <= 0) {
                        continue;
                    }

                    if (random.nextInt(chanceBase) > spreadScore) {
                        continue;
                    }

                    if (world.isRaining() && this.isRainingAround(world, mutablePos)) {
                        continue;
                    }

                    int targetAge = Math.min(15, age + random.nextInt(5) / 4 + 1);
                    world.setBlockState(mutablePos, getStateWithAge(world, mutablePos, targetAge), 3);
                }
            }
        }
    }

    private int getSpreadChance(BlockState state) {
        if (state.contains(net.minecraft.state.property.Properties.WATERLOGGED) && state.get(net.minecraft.state.property.Properties.WATERLOGGED)) {
            return 0;
        }

        return spreadChances.getInt(state.getBlock());
    }

    private int getBurnChance(BlockState state) {
        if (state.contains(net.minecraft.state.property.Properties.WATERLOGGED) && state.get(net.minecraft.state.property.Properties.WATERLOGGED)) {
            return 0;
        }

        return burnChances.getInt(state.getBlock());
    }

    private void trySpreadingFire(World world, BlockPos pos, int spreadFactor, Random random, int currentAge) {
        int spreadChance = getSpreadChance(world.getBlockState(pos));
        if (random.nextInt(spreadFactor) >= spreadChance) {
            return;
        }

        BlockState targetState = world.getBlockState(pos);
        if (random.nextInt(currentAge + 10) < 5 && !world.hasRain(pos)) {
            int targetAge = Math.min(15, currentAge + random.nextInt(5) / 4);
            world.setBlockState(pos, getStateWithAge(world, pos, targetAge), 3);
        } else {
            world.removeBlock(pos, false);
        }

        if (targetState.getBlock() instanceof TntBlock) {
            TntBlock.primeTnt(world, pos);
        }
    }

    private static BlockState getStateWithAge(WorldAccess world, BlockPos pos, int age) {
        return getMatchstickState(world, pos).with(AGE, age);
    }

    private boolean areBlocksAroundFlammable(BlockView world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (this.isFlammable(world.getBlockState(pos.offset(direction)))) {
                return true;
            }
        }

        return false;
    }

    private int getBurnChance(WorldView world, BlockPos pos) {
        if (!world.isAir(pos)) {
            return 0;
        }

        int burnChance = 0;
        for (Direction direction : Direction.values()) {
            burnChance = Math.max(burnChance, getBurnChance(world.getBlockState(pos.offset(direction))));
        }

        return burnChance;
    }

    @Override
    protected boolean isFlammable(BlockState state) {
        return getBurnChance(state) > 0;
    }

    private static boolean shouldLightPortalAt(World world, BlockPos pos, Direction direction) {
        if (!isOverworldOrNether(world)) {
            return false;
        }

        BlockPos.Mutable mutablePos = pos.mutableCopy();
        boolean hasObsidianNeighbor = false;
        for (Direction directionToCheck : Direction.values()) {
            if (world.getBlockState(mutablePos.set(pos).move(directionToCheck)).isOf(Blocks.OBSIDIAN)) {
                hasObsidianNeighbor = true;
                break;
            }
        }

        if (!hasObsidianNeighbor) {
            return false;
        }

        Direction.Axis axis = direction.getAxis().isHorizontal()
                ? direction.rotateYCounterclockwise().getAxis()
                : Direction.Type.HORIZONTAL.randomAxis(world.random);

        return NetherPortal.getNewPortal(world, pos, axis).isPresent();
    }

    private static boolean isOverworldOrNether(World world) {
        return world.getRegistryKey() == World.OVERWORLD || world.getRegistryKey() == World.NETHER;
    }

    private static int getFireTickDelay(Random random) {
        return 30 + random.nextInt(10);
    }

    private static int adjustedSpreadFactor(int spreadFactor) {
        return Math.max(1, spreadFactor / DIRECT_SPREAD_MULTIPLIER);
    }
}
