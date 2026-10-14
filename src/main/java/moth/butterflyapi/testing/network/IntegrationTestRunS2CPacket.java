package moth.butterflyapi.testing.network;

import java.util.Objects;
import moth.butterflyapi.ButterflyApi;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record IntegrationTestRunS2CPacket(BlockPos anchor, boolean clear) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<IntegrationTestRunS2CPacket> ID =
            new CustomPacketPayload.Type<>(ButterflyApi.id("integration_test_run"));
    public static final StreamCodec<RegistryFriendlyByteBuf, IntegrationTestRunS2CPacket> CODEC =
            StreamCodec.of((buffer, packet) -> packet.write(buffer), IntegrationTestRunS2CPacket::new);

    public IntegrationTestRunS2CPacket {
        anchor = Objects.requireNonNull(anchor, "anchor").immutable();
    }

    private IntegrationTestRunS2CPacket(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readBoolean());
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
        buffer.writeBoolean(clear);
    }

    @Override
    public CustomPacketPayload.Type<IntegrationTestRunS2CPacket> type() {
        return ID;
    }
}
