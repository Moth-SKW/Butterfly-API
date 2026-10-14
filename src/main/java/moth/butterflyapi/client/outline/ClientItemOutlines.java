package moth.butterflyapi.client.outline;

import moth.butterflyapi.client.outline.internal.ClientItemOutlineManager;
import moth.butterflyapi.outline.ItemOutlineTarget;
import moth.butterflyapi.outline.OutlineStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class ClientItemOutlines {
    private ClientItemOutlines() {
    }

    /**
     * Adds an item outline rule. GUI rules use the alpha mask of the rendered item model, not a
     * rectangular item cell. GUI-only constraints are validated when the rule is registered.
     */
    public static void set(Item item, ItemOutlineTarget target, OutlineStyle style) {
        ClientItemOutlineManager.set(item, target, style);
    }

    public static void set(Item item, OutlineStyle style, ItemOutlineTarget... targets) {
        Objects.requireNonNull(targets, "targets");
        Objects.requireNonNull(style, "style");
        for (ItemOutlineTarget target : targets) {
            ClientItemOutlineManager.validate(Objects.requireNonNull(target, "target"), style);
        }
        for (ItemOutlineTarget target : targets) {
            set(item, Objects.requireNonNull(target, "target"), style);
        }
    }

    public static void clear(Item item, ItemOutlineTarget target) {
        ClientItemOutlineManager.clear(item, target);
    }

    public static void clear(Item item) {
        ClientItemOutlineManager.clear(item);
    }

    public static void clearAll() {
        ClientItemOutlineManager.clearAll();
    }

    public static Optional<OutlineStyle> get(ItemStack stack, ItemOutlineTarget target) {
        return Optional.ofNullable(ClientItemOutlineManager.resolve(
                Objects.requireNonNull(stack, "stack"),
                Objects.requireNonNull(target, "target")
        ));
    }
}
