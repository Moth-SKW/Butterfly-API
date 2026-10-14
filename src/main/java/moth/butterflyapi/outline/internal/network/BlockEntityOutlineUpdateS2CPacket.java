package moth.butterflyapi.outline.internal.network;

import moth.butterflyapi.ButterflyApi;
import moth.butterflyapi.outline.OutlineStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record BlockEntityOutlineUpdateS2CPacket(
        Identifier dimension,
        BlockPos pos,
        @Nullable OutlineStyle style
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BlockEntityOutlineUpdateS2CPacket> ID =
            new CustomPacketPayload.Type<>(ButterflyApi.id("block_entity_outline_update_v2"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockEntityOutlineUpdateS2CPacket> CODEC =
            StreamCodec.of((buffer, packet) -> packet.write(buffer), BlockEntityOutlineUpdateS2CPacket::new);

    public BlockEntityOutlineUpdateS2CPacket {
        Objects.requireNonNull(dimension, "dimension");
        pos = Objects.requireNonNull(pos, "pos").immutable();
    }

    private BlockEntityOutlineUpdateS2CPacket(FriendlyByteBuf buffer) {
        this(
                buffer.readIdentifier(),
                buffer.readBlockPos(),
                OutlinePacketCodecs.readStyle(buffer)
        );
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeIdentifier(dimension);
        buffer.writeBlockPos(pos);
        OutlinePacketCodecs.writeStyle(buffer, style);
    }

    @Override
    public CustomPacketPayload.Type<BlockEntityOutlineUpdateS2CPacket> type() {
        return ID;
    }
}
