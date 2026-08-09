package moth.butterflyapi.outline.internal.network;

import moth.butterflyapi.outline.OutlinePlacement;
import moth.butterflyapi.outline.OutlineStyle;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;

final class OutlinePacketCodecs {
    private OutlinePacketCodecs() {
    }

    static void writeStyle(PacketByteBuf buffer, @Nullable OutlineStyle style) {
        buffer.writeBoolean(style != null);
        if (style == null) {
            return;
        }

        buffer.writeInt(style.rgb());
        buffer.writeVarInt(style.thickness());
        buffer.writeVarInt(style.placement().id());
        buffer.writeBoolean(style.blackBorder());
        buffer.writeBoolean(style.visibleThroughWalls());
        buffer.writeBoolean(style.matchModelLayer());
        buffer.writeBoolean(style.consistentThickness());
        buffer.writeFloat(style.thicknessAdaptationRate());
    }

    @Nullable
    static OutlineStyle readStyle(PacketByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return null;
        }

        int rgb = buffer.readInt();
        int thickness = buffer.readVarInt();
        OutlinePlacement placement = OutlinePlacement.fromId(buffer.readVarInt());
        boolean blackBorder = buffer.readBoolean();
        boolean visibleThroughWalls = buffer.readBoolean();
        boolean matchModelLayer = buffer.readBoolean();
        // The extra controls are appended so a 1.1.x client can still read a
        // packet from a newer server. Conversely, a newer client connected to
        // an older server keeps the old behavior when these bytes are absent.
        boolean consistentThickness = buffer.isReadable()
                ? buffer.readBoolean()
                : OutlineStyle.DEFAULT_CONSISTENT_THICKNESS;
        float adaptationRate = buffer.readableBytes() >= Float.BYTES
                ? buffer.readFloat()
                : OutlineStyle.DEFAULT_THICKNESS_ADAPTATION_RATE;
        return new OutlineStyle(
                rgb,
                thickness,
                placement,
                blackBorder,
                visibleThroughWalls,
                matchModelLayer,
                consistentThickness,
                adaptationRate
        );
    }
}
