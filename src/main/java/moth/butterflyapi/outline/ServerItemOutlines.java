package moth.butterflyapi.outline;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

public final class ServerItemOutlines {
    private ServerItemOutlines() {
    }

    public static boolean setHeldFor(
            ServerPlayer viewer,
            LivingEntity holder,
            Item item,
            ItemOutlineTarget target,
            OutlineStyle style
    ) {
        Objects.requireNonNull(viewer, "viewer");
        Objects.requireNonNull(holder, "holder");
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(style, "style");
        EntityOutlinePart part = heldPart(target);
        if (!heldStack(holder, target).is(item)) {
            ServerOutlines.clearPartFor(viewer, holder, part);
            return false;
        }
        ServerOutlines.setPartFor(viewer, holder, part, style);
        return true;
    }

    public static boolean setHeldForNearestOther(
            LivingEntity holder,
            Item item,
            ItemOutlineTarget target,
            OutlineStyle style
    ) {
        ServerPlayer viewer = nearestOtherPlayer(holder).orElse(null);
        return viewer != null && setHeldFor(viewer, holder, item, target, style);
    }

    public static boolean setDroppedFor(
            ServerPlayer viewer,
            ItemEntity itemEntity,
            Item item,
            OutlineStyle style
    ) {
        Objects.requireNonNull(viewer, "viewer");
        Objects.requireNonNull(itemEntity, "itemEntity");
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(style, "style");
        if (!itemEntity.getItem().is(item)) {
            ServerOutlines.clearPartFor(viewer, itemEntity, EntityOutlinePart.BODY);
            return false;
        }
        ServerOutlines.setPartFor(viewer, itemEntity, EntityOutlinePart.BODY, style);
        return true;
    }

    public static Optional<ServerPlayer> nearestOtherPlayer(LivingEntity holder) {
        Objects.requireNonNull(holder, "holder");
        if (!(holder.level() instanceof ServerLevel level)) {
            throw new IllegalArgumentException("Item outline holders must be in a server world");
        }
        return PlayerLookup.world(level).stream()
                .filter(player -> !player.getUUID().equals(holder.getUUID()))
                .min(Comparator.comparingDouble(player -> player.distanceToSqr(holder)));
    }

    private static EntityOutlinePart heldPart(ItemOutlineTarget target) {
        return switch (Objects.requireNonNull(target, "target")) {
            case MAIN_HAND -> EntityOutlinePart.MAIN_HAND;
            case OFF_HAND -> EntityOutlinePart.OFF_HAND;
            case DROPPED, GUI -> throw new IllegalArgumentException("A held-item server outline needs MAIN_HAND or OFF_HAND");
        };
    }

    private static net.minecraft.world.item.ItemStack heldStack(LivingEntity holder, ItemOutlineTarget target) {
        return switch (target) {
            case MAIN_HAND -> holder.getMainHandItem();
            case OFF_HAND -> holder.getOffhandItem();
            case DROPPED, GUI -> throw new IllegalArgumentException("A held-item server outline needs MAIN_HAND or OFF_HAND");
        };
    }
}
