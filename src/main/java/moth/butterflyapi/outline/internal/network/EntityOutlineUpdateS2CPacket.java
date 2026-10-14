package moth.butterflyapi.outline.internal.network;

import moth.butterflyapi.ButterflyApi;
import moth.butterflyapi.outline.OutlineStyle;
import moth.butterflyapi.outline.EntityOutline;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public record EntityOutlineUpdateS2CPacket(
        Identifier dimension,
        int entityId,
        UUID entityUuid,
        @Nullable EntityOutline outline
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EntityOutlineUpdateS2CPacket> ID =
            new CustomPacketPayload.Type<>(ButterflyApi.id("entity_outline_update_v3"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityOutlineUpdateS2CPacket> CODEC =
            StreamCodec.of((buffer, packet) -> packet.write(buffer), EntityOutlineUpdateS2CPacket::new);

    public EntityOutlineUpdateS2CPacket {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(entityUuid, "entityUuid");
    }

    private EntityOutlineUpdateS2CPacket(FriendlyByteBuf buffer) {
        this(
                buffer.readIdentifier(),
                buffer.readVarInt(),
                buffer.readUUID(),
                OutlinePacketCodecs.readEntityOutline(buffer)
        );
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeIdentifier(dimension);
        buffer.writeVarInt(entityId);
        buffer.writeUUID(entityUuid);
        OutlinePacketCodecs.writeEntityOutline(buffer, outline);
    }

    @Override
    public CustomPacketPayload.Type<EntityOutlineUpdateS2CPacket> type() {
        return ID;
    }
}
