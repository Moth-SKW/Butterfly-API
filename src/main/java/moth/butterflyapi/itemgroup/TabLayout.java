package moth.butterflyapi.itemgroup;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class TabLayout {
    private static final TabLayout EMPTY = new TabLayout(List.of(), Map.of());

    private final List<Element> uncategorized;
    private final Map<String, List<Element>> categories;
    private final boolean hasSlotOverrides;

    public TabLayout(List<Element> uncategorized, Map<String, List<Element>> categories) {
        this.uncategorized = List.copyOf(Objects.requireNonNull(uncategorized, "uncategorized"));

        Map<String, List<Element>> copiedCategories = new LinkedHashMap<>();
        Objects.requireNonNull(categories, "categories").forEach((id, entries) ->
                copiedCategories.put(
                        Objects.requireNonNull(id, "category id"),
                        List.copyOf(Objects.requireNonNull(entries, "category entries"))
                ));
        this.categories = Map.copyOf(copiedCategories);
        this.hasSlotOverrides = containsSlotOverride(this.uncategorized)
                || copiedCategories.values().stream().anyMatch(TabLayout::containsSlotOverride);
    }

    public static TabLayout empty() {
        return EMPTY;
    }

    public List<Element> uncategorized() {
        return uncategorized;
    }

    public List<Element> category(String id) {
        return categories.getOrDefault(Objects.requireNonNull(id, "id"), List.of());
    }

    public boolean hasSlotOverrides() {
        return hasSlotOverrides;
    }

    private static boolean containsSlotOverride(List<Element> elements) {
        for (Element element : elements) {
            if (element instanceof StackElement stackElement && stackElement.slotBackground() != null) {
                return true;
            }
        }
        return false;
    }

    public sealed interface Element permits StackElement, EmptySlot, EmptyRow {
    }

    public static final class StackElement implements Element {
        private final ItemStack stack;
        private final boolean searchOnly;
        @Nullable
        private final TabSurface slotBackground;

        public StackElement(ItemStack stack) {
            this(stack, false, null);
        }

        public StackElement(ItemStack stack, boolean searchOnly) {
            this(stack, searchOnly, null);
        }

        public StackElement(
                ItemStack stack,
                boolean searchOnly,
                @Nullable TabSurface slotBackground
        ) {
            this.stack = Objects.requireNonNull(stack, "stack").copy();
            this.searchOnly = searchOnly;
            this.slotBackground = slotBackground;
        }

        public ItemStack stack() {
            return stack.copy();
        }

        public boolean searchOnly() {
            return searchOnly;
        }

        @Nullable
        public TabSurface slotBackground() {
            return slotBackground;
        }
    }

    public enum EmptySlot implements Element {
        INSTANCE
    }

    public enum EmptyRow implements Element {
        INSTANCE
    }
}
