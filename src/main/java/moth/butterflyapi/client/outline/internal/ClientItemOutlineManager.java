package moth.butterflyapi.client.outline.internal;

import moth.butterflyapi.outline.ItemOutlineTarget;
import moth.butterflyapi.outline.OutlineStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public final class ClientItemOutlineManager {
    private static final Map<Item, EnumMap<ItemOutlineTarget, OutlineStyle>> STYLES = new HashMap<>();

    private ClientItemOutlineManager() {
    }

    public static void set(Item item, ItemOutlineTarget target, OutlineStyle style) {
        Item checkedItem = Objects.requireNonNull(item, "item");
        ItemOutlineTarget checkedTarget = Objects.requireNonNull(target, "target");
        OutlineStyle checkedStyle = Objects.requireNonNull(style, "style");
        validate(checkedTarget, checkedStyle);
        STYLES.computeIfAbsent(checkedItem, ignored -> new EnumMap<>(ItemOutlineTarget.class))
                .put(checkedTarget, checkedStyle);
    }

    public static void validate(ItemOutlineTarget target, OutlineStyle style) {
        if (Objects.requireNonNull(target, "target") != ItemOutlineTarget.GUI) {
            return;
        }

        OutlineStyle checkedStyle = Objects.requireNonNull(style, "style");
        if (checkedStyle.thickness() > moth.butterflyapi.client.outline.GuiItemOutlineRenderer.MAX_SUPPORTED_THICKNESS) {
            throw new IllegalArgumentException(
                    "GUI item outlines support a maximum thickness of "
                            + moth.butterflyapi.client.outline.GuiItemOutlineRenderer.MAX_SUPPORTED_THICKNESS
                            + " pixels"
            );
        }
        if (checkedStyle.matchModelLayer()) {
            throw new IllegalArgumentException("GUI item outlines cannot match a world-model render layer");
        }
        if (checkedStyle.visibleThroughWalls() != OutlineStyle.DEFAULT_VISIBLE_THROUGH_WALLS) {
            throw new IllegalArgumentException("GUI item outlines do not have world occlusion");
        }
        if (checkedStyle.consistentThickness() != OutlineStyle.DEFAULT_CONSISTENT_THICKNESS
                || Float.compare(checkedStyle.thicknessAdaptationRate(), OutlineStyle.DEFAULT_THICKNESS_ADAPTATION_RATE) != 0) {
            throw new IllegalArgumentException("GUI item outlines do not support distance-adaptive thickness");
        }
    }

    public static void clear(Item item, ItemOutlineTarget target) {
        EnumMap<ItemOutlineTarget, OutlineStyle> styles = STYLES.get(Objects.requireNonNull(item, "item"));
        if (styles == null) {
            return;
        }
        styles.remove(Objects.requireNonNull(target, "target"));
        if (styles.isEmpty()) {
            STYLES.remove(item);
        }
    }

    public static void clear(Item item) {
        STYLES.remove(Objects.requireNonNull(item, "item"));
    }

    public static void clearAll() {
        STYLES.clear();
    }

    @Nullable
    public static OutlineStyle resolve(ItemStack stack, ItemOutlineTarget target) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        EnumMap<ItemOutlineTarget, OutlineStyle> styles = STYLES.get(stack.getItem());
        return styles == null ? null : styles.get(target);
    }
}
