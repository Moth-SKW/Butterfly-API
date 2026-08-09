package moth.butterflyapi.client.outline;

import moth.butterflyapi.client.outline.internal.ClientOutlineManager;
import moth.butterflyapi.outline.OutlineStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;

import java.util.Objects;
import java.util.Optional;

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

    public static void set(BlockEntity target, OutlineStyle style) {
        ClientOutlineManager.setLocal(
                Objects.requireNonNull(target, "target"),
                Objects.requireNonNull(style, "style")
        );
    }

    public static void clear(Entity target) {
        ClientOutlineManager.clearLocal(Objects.requireNonNull(target, "target"));
    }

    public static void clear(BlockEntity target) {
        ClientOutlineManager.clearLocal(Objects.requireNonNull(target, "target"));
    }

    public static void clearAll() {
        ClientOutlineManager.clearAllLocal();
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

    public static Optional<OutlineStyle> getEffective(BlockEntity target) {
        return Optional.ofNullable(ClientOutlineManager.getEffective(Objects.requireNonNull(target, "target")));
    }
}
