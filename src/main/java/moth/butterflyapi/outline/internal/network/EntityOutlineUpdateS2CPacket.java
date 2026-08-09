package moth.butterflyapi.outline.internal.network;

import moth.butterflyapi.ButterflyApi;
import moth.butterflyapi.outline.OutlineStyle;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public record EntityOutlineUpdateS2CPacket(
        Identifier dimension,
        int entityId,
        UUID entityUuid,
        @Nullable OutlineStyle style
) implements CustomPayload {
    public static final CustomPayload.Id<EntityOutlineUpdateS2CPacket> ID =
            new CustomPayload.Id<>(ButterflyApi.id("entity_outline_update_v2"));
    public static final PacketCodec<RegistryByteBuf, EntityOutlineUpdateS2CPacket> CODEC =
            PacketCodec.ofStatic((buffer, packet) -> packet.write(buffer), EntityOutlineUpdateS2CPacket::new);

    public EntityOutlineUpdateS2CPacket {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(entityUuid, "entityUuid");
    }

    private EntityOutlineUpdateS2CPacket(PacketByteBuf buffer) {
        this(
                buffer.readIdentifier(),
                buffer.readVarInt(),
                buffer.readUuid(),
                OutlinePacketCodecs.readStyle(buffer)
        );
    }

    public void write(PacketByteBuf buffer) {
        buffer.writeIdentifier(dimension);
        buffer.writeVarInt(entityId);
        buffer.writeUuid(entityUuid);
        OutlinePacketCodecs.writeStyle(buffer, style);
    }

    @Override
    public CustomPayload.Id<EntityOutlineUpdateS2CPacket> getId() {
        return ID;
    }
}
