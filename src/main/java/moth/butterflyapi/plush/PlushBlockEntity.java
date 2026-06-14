package moth.butterflyapi.plush;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PlushBlockEntity extends BlockEntity implements GeoBlockEntity {
    public double squash = 0.0;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PlushBlockEntity(BlockPos pos, BlockState state) {
        super(ButterflyPlushes.blockEntityType(), pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, PlushBlockEntity be) {
        if (be.squash > 0.0) {
            be.squash /= 2.5;
            if (be.squash < 0.01) {
                be.squash = 0.0;
                world.updateListeners(pos, state, state, 2);
            }
        }
    }

    public void squish(int amount) {
        squash += amount;
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 2);
        }
        markDirty();
    }

    public PlushDefinition plushDefinition() {
        if (getCachedState().getBlock() instanceof PlushBlock plushBlock) {
            return plushBlock.plushDefinition();
        }

        throw new IllegalStateException("Plush block entity at " + pos + " is attached to a non-plush block");
    }

    public void onUsed() {
        plushDefinition().onUse(this);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putDouble("squash", squash);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        squash = nbt.getDouble("squash");
    }

    @Nullable
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        plushDefinition().registerControllers(this, controllers);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
