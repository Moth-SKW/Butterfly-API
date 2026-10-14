package moth.butterflyapi.outline.internal.network;

import moth.butterflyapi.outline.OutlinePlacement;
import moth.butterflyapi.outline.EntityOutline;
import moth.butterflyapi.outline.EntityOutlinePart;
import moth.butterflyapi.outline.OutlineStyle;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

final class OutlinePacketCodecs {
    private OutlinePacketCodecs() {
    }

    static void writeStyle(FriendlyByteBuf buffer, @Nullable OutlineStyle style) {
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

    static void writeEntityOutline(FriendlyByteBuf buffer, @Nullable EntityOutline outline) {
        buffer.writeBoolean(outline != null);
        if (outline == null) {
            return;
        }

        buffer.writeVarInt(outline.styles().size());
        for (var entry : outline.styles().entrySet()) {
            buffer.writeVarInt(entry.getKey().ordinal());
            writeStyleData(buffer, entry.getValue());
        }
    }

    @Nullable
    static OutlineStyle readStyle(FriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return null;
        }

        return readStyleData(buffer);
    }

    @Nullable
    static EntityOutline readEntityOutline(FriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return null;
        }

        int size = buffer.readVarInt();
        if (size < 0 || size > EntityOutlinePart.values().length) {
            throw new IllegalArgumentException("Invalid entity outline part count: " + size);
        }
        EntityOutline.Builder builder = EntityOutline.builder();
        for (int index = 0; index < size; index++) {
            int ordinal = buffer.readVarInt();
            EntityOutlinePart[] parts = EntityOutlinePart.values();
            if (ordinal < 0 || ordinal >= parts.length) {
                throw new IllegalArgumentException("Invalid entity outline part: " + ordinal);
            }
            builder.part(parts[ordinal], readStyleData(buffer));
        }
        return builder.build();
    }

    private static void writeStyleData(FriendlyByteBuf buffer, OutlineStyle style) {
        buffer.writeInt(style.rgb());
        buffer.writeVarInt(style.thickness());
        buffer.writeVarInt(style.placement().id());
        buffer.writeBoolean(style.blackBorder());
        buffer.writeBoolean(style.visibleThroughWalls());
        buffer.writeBoolean(style.matchModelLayer());
        buffer.writeBoolean(style.consistentThickness());
        buffer.writeFloat(style.thicknessAdaptationRate());
    }

    private static OutlineStyle readStyleData(FriendlyByteBuf buffer) {
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
