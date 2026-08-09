package moth.butterflyapi.outline;

import moth.butterflyapi.outline.internal.OutlineServerManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class ServerOutlines {
    private ServerOutlines() {
    }

    public static void set(Entity entity, OutlineStyle style) {
        OutlineServerManager.set(entity, style);
    }

    public static void setFor(ServerPlayerEntity viewer, Entity entity, OutlineStyle style) {
        OutlineServerManager.setFor(viewer, entity, style);
    }

    public static void clear(Entity entity) {
        OutlineServerManager.clear(entity);
    }

    public static void clearFor(ServerPlayerEntity viewer, Entity entity) {
        OutlineServerManager.clearFor(viewer, entity);
    }

    @Nullable
    public static OutlineStyle get(Entity entity) {
        return OutlineServerManager.get(entity);
    }

    @Nullable
    public static OutlineStyle getFor(ServerPlayerEntity viewer, Entity entity) {
        return OutlineServerManager.getFor(viewer, entity);
    }

    public static void set(BlockEntity blockEntity, OutlineStyle style) {
        OutlineServerManager.set(blockEntity, style);
    }

    public static void setFor(ServerPlayerEntity viewer, BlockEntity blockEntity, OutlineStyle style) {
        OutlineServerManager.setFor(viewer, blockEntity, style);
    }

    public static void clear(BlockEntity blockEntity) {
        OutlineServerManager.clear(blockEntity);
    }

    public static void clearFor(ServerPlayerEntity viewer, BlockEntity blockEntity) {
        OutlineServerManager.clearFor(viewer, blockEntity);
    }

    @Nullable
    public static OutlineStyle get(BlockEntity blockEntity) {
        return OutlineServerManager.get(blockEntity);
    }

    @Nullable
    public static OutlineStyle getFor(ServerPlayerEntity viewer, BlockEntity blockEntity) {
        return OutlineServerManager.getFor(viewer, blockEntity);
    }
}
