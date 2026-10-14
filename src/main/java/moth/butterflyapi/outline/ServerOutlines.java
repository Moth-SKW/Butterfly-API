package moth.butterflyapi.outline;

import moth.butterflyapi.outline.internal.OutlineServerManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public final class ServerOutlines {
    private ServerOutlines() {
    }

    public static void set(Entity entity, OutlineStyle style) {
        OutlineServerManager.set(entity, style);
    }

    public static void set(Entity entity, EntityOutline outline) {
        OutlineServerManager.set(entity, Objects.requireNonNull(outline, "outline"));
    }

    public static void set(Entity entity, String color) {
        set(entity, OutlineStyle.of(color));
    }

    public static void set(Entity entity, Consumer<OutlineStyle.Builder> configuration) {
        set(entity, configuredStyle(configuration));
    }

    public static void setFor(ServerPlayer viewer, Entity entity, OutlineStyle style) {
        OutlineServerManager.setFor(viewer, entity, style);
    }

    public static void setFor(ServerPlayer viewer, Entity entity, EntityOutline outline) {
        OutlineServerManager.setFor(
                Objects.requireNonNull(viewer, "viewer"),
                Objects.requireNonNull(entity, "entity"),
                Objects.requireNonNull(outline, "outline")
        );
    }

    public static void setPart(Entity entity, EntityOutlinePart part, OutlineStyle style) {
        OutlineServerManager.setPart(
                Objects.requireNonNull(entity, "entity"),
                Objects.requireNonNull(part, "part"),
                Objects.requireNonNull(style, "style")
        );
    }

    public static void setPartFor(ServerPlayer viewer, Entity entity, EntityOutlinePart part, OutlineStyle style) {
        OutlineServerManager.setPartFor(
                Objects.requireNonNull(viewer, "viewer"),
                Objects.requireNonNull(entity, "entity"),
                Objects.requireNonNull(part, "part"),
                Objects.requireNonNull(style, "style")
        );
    }

    public static void setArmor(Entity entity, EquipmentSlot slot, OutlineStyle style) {
        setPart(entity, EntityOutlinePart.armor(Objects.requireNonNull(slot, "slot")), style);
    }

    public static void setArmorFor(ServerPlayer viewer, Entity entity, EquipmentSlot slot, OutlineStyle style) {
        setPartFor(viewer, entity, EntityOutlinePart.armor(Objects.requireNonNull(slot, "slot")), style);
    }

    public static void setHeldItems(Entity entity, OutlineStyle style) {
        setPart(entity, EntityOutlinePart.MAIN_HAND, style);
        setPart(entity, EntityOutlinePart.OFF_HAND, style);
    }

    public static int setArmorOnWearers(
            ServerLevel level,
            Item armorItem,
            EquipmentSlot slot,
            OutlineStyle style
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(armorItem, "armorItem");
        EntityOutlinePart.armor(Objects.requireNonNull(slot, "slot"));
        Objects.requireNonNull(style, "style");
        if (!level.getServer().isSameThread()) {
            throw new IllegalStateException("ServerOutlines must be used from the logical server thread");
        }
        int count = 0;
        for (LivingEntity entity : level.getEntities(
                EntityTypeTest.forClass(LivingEntity.class),
                entity -> entity.getItemBySlot(slot).is(armorItem)
        )) {
            setArmor(entity, slot, style);
            count++;
        }
        return count;
    }

    public static void clear(Entity entity) {
        OutlineServerManager.clear(entity);
    }

    public static void clearPart(Entity entity, EntityOutlinePart part) {
        OutlineServerManager.clearPart(
                Objects.requireNonNull(entity, "entity"),
                Objects.requireNonNull(part, "part")
        );
    }

    public static void clearFor(ServerPlayer viewer, Entity entity) {
        OutlineServerManager.clearFor(viewer, entity);
    }

    public static void clearPartFor(ServerPlayer viewer, Entity entity, EntityOutlinePart part) {
        OutlineServerManager.clearPartFor(
                Objects.requireNonNull(viewer, "viewer"),
                Objects.requireNonNull(entity, "entity"),
                Objects.requireNonNull(part, "part")
        );
    }

    @Nullable
    public static OutlineStyle get(Entity entity) {
        return OutlineServerManager.get(entity);
    }

    @Nullable
    public static OutlineStyle getFor(ServerPlayer viewer, Entity entity) {
        return OutlineServerManager.getFor(viewer, entity);
    }

    @Nullable
    public static EntityOutline getOutline(Entity entity) {
        return OutlineServerManager.getOutline(entity);
    }

    @Nullable
    public static EntityOutline getOutlineFor(ServerPlayer viewer, Entity entity) {
        return OutlineServerManager.getOutlineFor(viewer, entity);
    }

    public static void set(BlockEntity blockEntity, OutlineStyle style) {
        OutlineServerManager.set(blockEntity, style);
    }

    public static void set(BlockEntity blockEntity, String color) {
        set(blockEntity, OutlineStyle.of(color));
    }

    public static void set(BlockEntity blockEntity, Consumer<OutlineStyle.Builder> configuration) {
        set(blockEntity, configuredStyle(configuration));
    }

    public static void setFor(ServerPlayer viewer, BlockEntity blockEntity, OutlineStyle style) {
        OutlineServerManager.setFor(viewer, blockEntity, style);
    }

    public static void clear(BlockEntity blockEntity) {
        OutlineServerManager.clear(blockEntity);
    }

    public static void clearFor(ServerPlayer viewer, BlockEntity blockEntity) {
        OutlineServerManager.clearFor(viewer, blockEntity);
    }

    @Nullable
    public static OutlineStyle get(BlockEntity blockEntity) {
        return OutlineServerManager.get(blockEntity);
    }

    @Nullable
    public static OutlineStyle getFor(ServerPlayer viewer, BlockEntity blockEntity) {
        return OutlineServerManager.getFor(viewer, blockEntity);
    }

    private static OutlineStyle configuredStyle(Consumer<OutlineStyle.Builder> configuration) {
        OutlineStyle.Builder builder = OutlineStyle.builder();
        Objects.requireNonNull(configuration, "configuration").accept(builder);
        return builder.build();
    }
}
