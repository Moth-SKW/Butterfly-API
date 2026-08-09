package moth.butterflyapi.outline.internal.network;

import moth.butterflyapi.ButterflyApi;
import moth.butterflyapi.outline.OutlineStyle;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record BlockEntityOutlineUpdateS2CPacket(
        Identifier dimension,
        BlockPos pos,
        @Nullable OutlineStyle style
) implements CustomPayload {
    public static final CustomPayload.Id<BlockEntityOutlineUpdateS2CPacket> ID =
            new CustomPayload.Id<>(ButterflyApi.id("block_entity_outline_update_v2"));
    public static final PacketCodec<RegistryByteBuf, BlockEntityOutlineUpdateS2CPacket> CODEC =
            PacketCodec.ofStatic((buffer, packet) -> packet.write(buffer), BlockEntityOutlineUpdateS2CPacket::new);

    public BlockEntityOutlineUpdateS2CPacket {
        Objects.requireNonNull(dimension, "dimension");
        pos = Objects.requireNonNull(pos, "pos").toImmutable();
    }

    private BlockEntityOutlineUpdateS2CPacket(PacketByteBuf buffer) {
        this(
                buffer.readIdentifier(),
                buffer.readBlockPos(),
                OutlinePacketCodecs.readStyle(buffer)
        );
    }

    public void write(PacketByteBuf buffer) {
        buffer.writeIdentifier(dimension);
        buffer.writeBlockPos(pos);
        OutlinePacketCodecs.writeStyle(buffer, style);
    }

    @Override
    public CustomPayload.Id<BlockEntityOutlineUpdateS2CPacket> getId() {
        return ID;
    }
}
