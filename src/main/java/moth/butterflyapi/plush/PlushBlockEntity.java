package moth.butterflyapi.plush;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PlushBlockEntity extends BlockEntity {
    public double squash = 0.0;

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
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putDouble("squash", squash);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        squash = view.getDouble("squash", 0.0);
    }

    @Nullable
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }
}
