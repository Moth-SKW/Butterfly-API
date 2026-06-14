package moth.butterflyapi.content.items;

import moth.butterflyapi.content.blocks.MatchstickFireBlock;
import moth.butterflyapi.content.sounds.ModSounds;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CandleCakeBlock;
import net.minecraft.block.SoulFireBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public final class MatchstickItem extends Item {

    private static final int DEFAULT_FIRE_SECONDS = 8;

    public MatchstickItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        var state = world.getBlockState(pos);

        if (CampfireBlock.canBeLit(state) || CandleBlock.canBeLit(state) || CandleCakeBlock.canBeLit(state)) {
            playUseSound(world, pos, player);

            if (!world.isClient) {
                world.setBlockState(pos, state.with(net.minecraft.state.property.Properties.LIT, true), 11);
                world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                consumeOne(context.getStack(), player);
            }

            return ActionResult.success(world.isClient);
        }

        BlockPos targetPos = pos.offset(context.getSide());
        if (!MatchstickFireBlock.canPlaceAt(world, targetPos, context.getHorizontalPlayerFacing())) {
            return ActionResult.FAIL;
        }

        playUseSound(world, targetPos, player);

        if (!world.isClient) {
            var fireState = MatchstickFireBlock.getMatchstickState(world, targetPos);
            world.setBlockState(targetPos, fireState, 11);
            world.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos);

            if (player instanceof ServerPlayerEntity serverPlayer) {
                Criteria.PLACED_BLOCK.trigger(serverPlayer, targetPos, context.getStack());
            }

            consumeOne(context.getStack(), player);
        }

        return ActionResult.success(world.isClient);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (entity.isFireImmune()) {
            return ActionResult.PASS;
        }

        World world = player.getWorld();
        playUseSound(world, entity.getBlockPos(), player);

        if (!world.isClient) {
            entity.setOnFireFor(DEFAULT_FIRE_SECONDS);
            entity.damage(world.getDamageSources().inFire(), isOnSoulBase(world, entity) ? 2.0f : 1.0f);
            consumeOne(stack, player);
        }

        return ActionResult.success(world.isClient);
    }

    private static boolean isOnSoulBase(World world, LivingEntity entity) {
        return SoulFireBlock.isSoulBase(entity.getSteppingBlockState())
                || SoulFireBlock.isSoulBase(world.getBlockState(entity.getBlockPos().down()));
    }

    private static void consumeOne(ItemStack stack, PlayerEntity player) {
        if (player == null || !player.isCreative()) {
            stack.decrement(1);
        }
    }

    private static void playUseSound(World world, BlockPos pos, PlayerEntity player) {
        world.playSound(player, pos, ModSounds.MATCHSTICK, SoundCategory.BLOCKS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
    }
}
