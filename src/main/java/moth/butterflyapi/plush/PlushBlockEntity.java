package moth.butterflyapi.plush;

import moth.butterflyapi.outline.OutlineOnlyBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
//? if >=1.21.11 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?}
import org.jetbrains.annotations.Nullable;

public class PlushBlockEntity extends BlockEntity implements OutlineOnlyBlockEntity {
    public double squash = 0.0;
    private double previousSquash;
    private long lastSoundTick = Long.MIN_VALUE;

    public PlushBlockEntity(BlockPos pos, BlockState state) {
        super(ButterflyPlushes.blockEntityType(), pos, state);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, PlushBlockEntity be) {
        be.previousSquash = be.squash;
        if (be.squash > 0.0) {
            be.squash /= 2.5;
            if (be.squash < 0.01) {
                be.squash = 0.0;
                world.sendBlockUpdated(pos, state, state, 2);
            }
        }
    }

    public void squish(int amount) {
        previousSquash = squash;
        squash += amount;
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
        setChanged();
    }

    public double interpolatedSquash(float tickDelta) {
        float progress = Math.max(0.0F, Math.min(1.0F, tickDelta));
        return previousSquash + (squash - previousSquash) * progress;
    }

    public boolean tryPlayInteractionSound(long gameTime, int cooldownTicks) {
        if (cooldownTicks < 0) {
            throw new IllegalArgumentException("cooldownTicks cannot be negative");
        }
        if (lastSoundTick != Long.MIN_VALUE && gameTime - lastSoundTick < cooldownTicks) {
            return false;
        }
        lastSoundTick = gameTime;
        return true;
    }

    public PlushDefinition plushDefinition() {
        if (getBlockState().getBlock() instanceof PlushBlock plushBlock) {
            return plushBlock.plushDefinition();
        }

        throw new IllegalStateException("Plush block entity at " + worldPosition + " is attached to a non-plush block");
    }

    public void onUsed() {
        plushDefinition().onUse(this);
    }

//? if >=1.21.11 {
    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.putDouble("squash", squash);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        previousSquash = squash;
        squash = view.getDoubleOr("squash", 0.0);
    }
//?} else {
/*
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("squash", squash);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        previousSquash = squash;
        squash = tag.getDouble("squash");
    }
*///?}

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
