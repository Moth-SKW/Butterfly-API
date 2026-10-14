package moth.butterflyapi.client.outline;

import moth.butterflyapi.client.outline.internal.ClientBlockOutlineManager;
import moth.butterflyapi.client.outline.internal.ClientOutlineManager;
import moth.butterflyapi.outline.EntityOutline;
import moth.butterflyapi.outline.EntityOutlinePart;
import moth.butterflyapi.outline.OutlineStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public final class ClientOutlines {
    private ClientOutlines() {
    }

    public static void set(Entity target, OutlineStyle style) {
        ClientOutlineManager.setLocal(
                Objects.requireNonNull(target, "target"),
                Objects.requireNonNull(style, "style")
        );
    }

    public static void set(Entity target, EntityOutline outline) {
        ClientOutlineManager.setLocal(
                Objects.requireNonNull(target, "target"),
                Objects.requireNonNull(outline, "outline")
        );
    }

    public static void setPart(Entity target, EntityOutlinePart part, OutlineStyle style) {
        ClientOutlineManager.setLocalPart(
                Objects.requireNonNull(target, "target"),
                Objects.requireNonNull(part, "part"),
                Objects.requireNonNull(style, "style")
        );
    }

    public static void setArmor(Entity target, EquipmentSlot slot, OutlineStyle style) {
        setPart(target, EntityOutlinePart.armor(Objects.requireNonNull(slot, "slot")), style);
    }

    public static void setHeldItems(Entity target, OutlineStyle style) {
        setPart(target, EntityOutlinePart.MAIN_HAND, style);
        setPart(target, EntityOutlinePart.OFF_HAND, style);
    }

    public static void set(Entity target, String color) {
        set(target, OutlineStyle.of(color));
    }

    public static void set(Entity target, Consumer<OutlineStyle.Builder> configuration) {
        set(target, configuredStyle(configuration));
    }

    public static void set(BlockEntity target, OutlineStyle style) {
        ClientOutlineManager.setLocal(
                Objects.requireNonNull(target, "target"),
                Objects.requireNonNull(style, "style")
        );
    }

    public static void set(BlockEntity target, String color) {
        set(target, OutlineStyle.of(color));
    }

    public static void set(BlockEntity target, Consumer<OutlineStyle.Builder> configuration) {
        set(target, configuredStyle(configuration));
    }

    public static void set(BlockPos position, OutlineStyle style) {
        ClientBlockOutlineManager.set(position, style);
    }

    public static void set(BlockPos position, String color) {
        set(position, blockGeometryStyle(color));
    }

    public static void set(BlockPos position, Consumer<OutlineStyle.Builder> configuration) {
        set(position, configuredBlockGeometryStyle(configuration));
    }

    public static void setAll(Iterable<BlockPos> positions, OutlineStyle style) {
        ClientBlockOutlineManager.setAll(positions, style);
    }

    public static void setAll(Iterable<BlockPos> positions, String color) {
        setAll(positions, blockGeometryStyle(color));
    }

    public static void setAll(Iterable<BlockPos> positions, Consumer<OutlineStyle.Builder> configuration) {
        setAll(positions, configuredBlockGeometryStyle(configuration));
    }

    public static void clear(Entity target) {
        ClientOutlineManager.clearLocal(Objects.requireNonNull(target, "target"));
    }

    public static void clearPart(Entity target, EntityOutlinePart part) {
        ClientOutlineManager.clearLocalPart(
                Objects.requireNonNull(target, "target"),
                Objects.requireNonNull(part, "part")
        );
    }

    public static void clearArmor(Entity target, EquipmentSlot slot) {
        clearPart(target, EntityOutlinePart.armor(Objects.requireNonNull(slot, "slot")));
    }

    public static void clear(BlockEntity target) {
        ClientOutlineManager.clearLocal(Objects.requireNonNull(target, "target"));
    }

    public static void clear(BlockPos position) {
        ClientBlockOutlineManager.clear(position);
    }

    public static void clearAll() {
        ClientOutlineManager.clearAllLocal();
        ClientBlockOutlineManager.clearAll();
    }

    public static Optional<OutlineStyle> getLocal(Entity target) {
        return Optional.ofNullable(ClientOutlineManager.getLocal(Objects.requireNonNull(target, "target")));
    }

    public static Optional<OutlineStyle> getLocal(BlockEntity target) {
        return Optional.ofNullable(ClientOutlineManager.getLocal(Objects.requireNonNull(target, "target")));
    }

    public static Optional<OutlineStyle> getEffective(Entity target) {
        return Optional.ofNullable(ClientOutlineManager.getEffective(Objects.requireNonNull(target, "target")));
    }

    public static Optional<EntityOutline> getLocalOutline(Entity target) {
        return Optional.ofNullable(ClientOutlineManager.getLocalOutline(Objects.requireNonNull(target, "target")));
    }

    public static Optional<EntityOutline> getEffectiveOutline(Entity target) {
        return Optional.ofNullable(ClientOutlineManager.getEffectiveOutline(Objects.requireNonNull(target, "target")));
    }

    public static Optional<OutlineStyle> getEffective(BlockEntity target) {
        return Optional.ofNullable(ClientOutlineManager.getEffective(Objects.requireNonNull(target, "target")));
    }

    public static Optional<OutlineStyle> get(BlockPos position) {
        return Optional.ofNullable(ClientBlockOutlineManager.get(position));
    }

    private static OutlineStyle configuredStyle(Consumer<OutlineStyle.Builder> configuration) {
        OutlineStyle.Builder builder = OutlineStyle.builder();
        Objects.requireNonNull(configuration, "configuration").accept(builder);
        return builder.build();
    }

    private static OutlineStyle blockGeometryStyle(String color) {
        return OutlineStyle.builder(color).thickness(1).occludedByWalls().build();
    }

    private static OutlineStyle configuredBlockGeometryStyle(Consumer<OutlineStyle.Builder> configuration) {
        OutlineStyle.Builder builder = OutlineStyle.builder().thickness(1).occludedByWalls();
        Objects.requireNonNull(configuration, "configuration").accept(builder);
        return builder.build();
    }
}
