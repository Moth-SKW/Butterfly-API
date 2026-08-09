package moth.butterflyapi.itemgroup;

import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class TabProperties {
    private static final Map<RegistryKey<ItemGroup>, Definition> DEFINITIONS = new ConcurrentHashMap<>();

    private TabProperties() {
    }

    static void register(RegistryKey<ItemGroup> group, Definition definition) {
        RegistryKey<ItemGroup> checkedGroup = Objects.requireNonNull(group, "group");
        Definition checkedDefinition = Objects.requireNonNull(definition, "definition");
        Definition previous = DEFINITIONS.putIfAbsent(checkedGroup, checkedDefinition);
        if (previous != null) {
            throw new IllegalStateException("Creative-tab properties were already registered for " + group.getValue());
        }
    }

    @Nullable
    public static Definition get(RegistryKey<ItemGroup> group) {
        return DEFINITIONS.get(Objects.requireNonNull(group, "group"));
    }

    @Nullable
    public static Identifier backgroundTexture(RegistryKey<ItemGroup> group) {
        Definition definition = get(group);
        return definition == null ? null : definition.backgroundTexture();
    }

    public static boolean isSearchTab(RegistryKey<ItemGroup> group) {
        return get(group) != null;
    }

    public static final class Definition {
        private final boolean customOverlay;
        private final boolean normalTabPosition;
        @Nullable
        private final Identifier backgroundTexture;
        @Nullable
        private final TabSurface slotBackground;
        @Nullable
        private final TabSurface betweenSlotsBackground;
        @Nullable
        private final Integer tintColor;
        @Nullable
        private final Integer tabNameColor;
        private final List<TabCategory> categories;
        private volatile TabLayout layout = TabLayout.empty();

        Definition(
                boolean customOverlay,
                boolean normalTabPosition,
                @Nullable Identifier backgroundTexture,
                @Nullable TabSurface slotBackground,
                @Nullable TabSurface betweenSlotsBackground,
                @Nullable Integer tintColor,
                @Nullable Integer tabNameColor,
                List<TabCategory> categories
        ) {
            if (customOverlay && tintColor != null) {
                throw new IllegalArgumentException(
                        "Creative-tab tint is only supported with vanilla appearance"
                );
            }
            this.customOverlay = customOverlay;
            this.normalTabPosition = normalTabPosition;
            this.backgroundTexture = backgroundTexture;
            this.slotBackground = slotBackground;
            this.betweenSlotsBackground = betweenSlotsBackground;
            this.tintColor = tintColor;
            this.tabNameColor = tabNameColor;
            this.categories = validateCategories(categories);
        }

        private static List<TabCategory> validateCategories(List<TabCategory> categories) {
            List<TabCategory> copied = List.copyOf(Objects.requireNonNull(categories, "categories"));
            Set<String> ids = new HashSet<>();
            for (TabCategory category : copied) {
                Objects.requireNonNull(category, "category");
                if (!ids.add(category.id())) {
                    throw new IllegalArgumentException("Duplicate creative-tab category id: " + category.id());
                }
            }
            return copied;
        }

        public boolean customOverlay() {
            return customOverlay;
        }

        public boolean normalTabPosition() {
            return normalTabPosition;
        }

        public boolean usesCustomGridOverlay() {
            return customOverlay
                    && (slotBackground != null
                    || betweenSlotsBackground != null
                    || !categories.isEmpty()
                    || layout.hasSlotOverrides());
        }

        @Nullable
        public Identifier backgroundTexture() {
            return backgroundTexture;
        }

        @Nullable
        public TabSurface slotBackground() {
            return slotBackground;
        }

        @Nullable
        public TabSurface betweenSlotsBackground() {
            return betweenSlotsBackground;
        }

        @Nullable
        public Integer tintColor() {
            return tintColor;
        }

        @Nullable
        public Integer tabNameColor() {
            return tabNameColor;
        }

        public List<TabCategory> categories() {
            return categories;
        }

        public TabLayout layout() {
            return layout;
        }

        void updateLayout(TabLayout layout) {
            this.layout = Objects.requireNonNull(layout, "layout");
        }
    }
}
