package moth.butterflyapi.itemgroup;

import moth.butterflyapi.mod.ModContext;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TabBuilder {
    private static final String DEFAULT_SEARCH_BACKGROUND = "item_search.png";

    private final ModContext context;
    private final String path;
    private final ItemGroup.Builder builder;
    private final List<TabEntryOperation> uncategorizedOperations = new ArrayList<>();
    private final Map<String, TabCategory.Builder> categoryBuilders = new LinkedHashMap<>();

    private List<String> explicitCategoryOrder;
    private boolean customOverlay = true;
    private Identifier customBackgroundTexture;
    private TabSurface slotBackground;
    private TabSurface betweenSlotsBackground;
    private Integer tintColor;
    private Integer tabNameColor;
    private boolean specialPosition;

    TabBuilder(ModContext context, String path) {
        this.context = Objects.requireNonNull(context, "context");
        this.path = Objects.requireNonNull(path, "path");
        this.builder = FabricItemGroup.builder();

        builder.texture(DEFAULT_SEARCH_BACKGROUND);
        builder.displayName(Text.translatable(context.tabs().translationKey(path)));
    }

    public TabBuilder icon(ItemConvertible icon) {
        Objects.requireNonNull(icon, "icon");
        return icon(() -> new ItemStack(icon));
    }

    public TabBuilder icon(Supplier<ItemStack> icon) {
        builder.icon(Objects.requireNonNull(icon, "icon"));
        return this;
    }

    public TabBuilder displayName(Text displayName) {
        builder.displayName(Objects.requireNonNull(displayName, "displayName"));
        return this;
    }

    public TabBuilder translationKey(String translationKey) {
        return displayName(Text.translatable(Objects.requireNonNull(translationKey, "translationKey")));
    }

    public TabBuilder entries(Consumer<ItemGroup.Entries> entries) {
        uncategorizedOperations.add(new TabEntriesOperation(
                Objects.requireNonNull(entries, "entries"),
                false
        ));
        return this;
    }

    public TabBuilder searchOnlyEntries(Consumer<ItemGroup.Entries> entries) {
        uncategorizedOperations.add(new TabEntriesOperation(
                Objects.requireNonNull(entries, "entries"),
                true
        ));
        return this;
    }

    public TabBuilder add(ItemConvertible... entries) {
        Objects.requireNonNull(entries, "entries");
        return entries(groupEntries -> addItems(groupEntries, entries));
    }

    public TabBuilder addStacks(ItemStack... stacks) {
        Objects.requireNonNull(stacks, "stacks");
        return entries(groupEntries -> addStacks(groupEntries, stacks));
    }

    public TabBuilder entry(ItemConvertible entry, Consumer<TabEntryStyle.Builder> appearance) {
        Objects.requireNonNull(entry, "entry");
        return styledEntry(new ItemStack(entry), false, appearance);
    }

    public TabBuilder entry(ItemStack stack, Consumer<TabEntryStyle.Builder> appearance) {
        return styledEntry(Objects.requireNonNull(stack, "stack"), false, appearance);
    }

    public TabBuilder searchOnly(ItemConvertible... entries) {
        Objects.requireNonNull(entries, "entries");
        return searchOnlyEntries(groupEntries -> addItems(groupEntries, entries));
    }

    public TabBuilder searchOnlyStacks(ItemStack... stacks) {
        Objects.requireNonNull(stacks, "stacks");
        return searchOnlyEntries(groupEntries -> addStacks(groupEntries, stacks));
    }

    public TabBuilder searchOnlyEntry(ItemConvertible entry, Consumer<TabEntryStyle.Builder> appearance) {
        Objects.requireNonNull(entry, "entry");
        return styledEntry(new ItemStack(entry), true, appearance);
    }

    public TabBuilder searchOnlyEntry(ItemStack stack, Consumer<TabEntryStyle.Builder> appearance) {
        return styledEntry(Objects.requireNonNull(stack, "stack"), true, appearance);
    }

    public TabBuilder emptySlot() {
        uncategorizedOperations.add(TabEmptySlotOperation.INSTANCE);
        return this;
    }

    public TabBuilder emptyRow() {
        uncategorizedOperations.add(TabEmptyRowOperation.INSTANCE);
        return this;
    }

    public TabBuilder category(String id, Text text, Consumer<TabCategory.Builder> configuration) {
        Objects.requireNonNull(configuration, "configuration");
        if (categoryBuilders.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate creative-tab category id: " + id);
        }

        TabCategory.Builder category = TabCategory.builder(id, text);
        configuration.accept(category);
        categoryBuilders.put(id, category);
        return this;
    }

    public TabBuilder category(String id, String text, Consumer<TabCategory.Builder> configuration) {
        return category(id, Text.literal(Objects.requireNonNull(text, "text")), configuration);
    }

    public TabBuilder category(String id, Text text) {
        return category(id, text, category -> {
        });
    }

    public TabBuilder category(String id, String text) {
        return category(id, Text.literal(Objects.requireNonNull(text, "text")));
    }

    public TabBuilder categoryEntries(String id, Consumer<ItemGroup.Entries> entries) {
        categoryBuilder(id).entries(entries);
        return this;
    }

    public TabBuilder searchOnlyCategoryEntries(String id, Consumer<ItemGroup.Entries> entries) {
        categoryBuilder(id).searchOnlyEntries(entries);
        return this;
    }

    public TabBuilder addToCategory(String id, ItemConvertible... entries) {
        categoryBuilder(id).add(entries);
        return this;
    }

    public TabBuilder addStacksToCategory(String id, ItemStack... stacks) {
        categoryBuilder(id).addStacks(stacks);
        return this;
    }

    public TabBuilder entryInCategory(
            String id,
            ItemConvertible entry,
            Consumer<TabEntryStyle.Builder> appearance
    ) {
        categoryBuilder(id).entry(entry, appearance);
        return this;
    }

    public TabBuilder entryInCategory(
            String id,
            ItemStack stack,
            Consumer<TabEntryStyle.Builder> appearance
    ) {
        categoryBuilder(id).entry(stack, appearance);
        return this;
    }

    public TabBuilder searchOnlyInCategory(String id, ItemConvertible... entries) {
        categoryBuilder(id).searchOnly(entries);
        return this;
    }

    public TabBuilder searchOnlyStacksInCategory(String id, ItemStack... stacks) {
        categoryBuilder(id).searchOnlyStacks(stacks);
        return this;
    }

    public TabBuilder searchOnlyEntryInCategory(
            String id,
            ItemConvertible entry,
            Consumer<TabEntryStyle.Builder> appearance
    ) {
        categoryBuilder(id).searchOnlyEntry(entry, appearance);
        return this;
    }

    public TabBuilder searchOnlyEntryInCategory(
            String id,
            ItemStack stack,
            Consumer<TabEntryStyle.Builder> appearance
    ) {
        categoryBuilder(id).searchOnlyEntry(stack, appearance);
        return this;
    }

    public TabBuilder emptySlot(String categoryId) {
        categoryBuilder(categoryId).emptySlot();
        return this;
    }

    public TabBuilder emptyRow(String categoryId) {
        categoryBuilder(categoryId).emptyRow();
        return this;
    }

    public TabBuilder categoryOrder(String... categoryIds) {
        Objects.requireNonNull(categoryIds, "categoryIds");
        List<String> requestedOrder = Arrays.stream(categoryIds)
                .map(id -> Objects.requireNonNull(id, "category id"))
                .toList();
        Set<String> unique = new LinkedHashSet<>(requestedOrder);
        if (unique.size() != requestedOrder.size()) {
            throw new IllegalArgumentException("Category order cannot contain duplicate ids");
        }
        explicitCategoryOrder = List.copyOf(requestedOrder);
        return this;
    }

    public TabBuilder searchBar() {
        return this;
    }

    public TabBuilder special() {
        specialPosition = true;
        builder.special();
        return this;
    }

    public TabBuilder noScrollbar() {
        builder.noScrollbar();
        return this;
    }

    public TabBuilder noRenderedName() {
        builder.noRenderedName();
        return this;
    }

    public TabBuilder customOverlay(boolean enabled) {
        customOverlay = enabled;
        return this;
    }

    public TabBuilder vanillaAppearance() {
        return customOverlay(false);
    }

    public TabBuilder customAppearance() {
        return customOverlay(true);
    }

    public TabBuilder backgroundSuffix(String suffix) {
        builder.texture(Objects.requireNonNull(suffix, "suffix"));
        customBackgroundTexture = null;
        return this;
    }

    public TabBuilder backgroundTexture(Identifier texture) {
        customBackgroundTexture = Objects.requireNonNull(texture, "texture");
        return this;
    }

    public TabBuilder slotBackgroundTexture(Identifier texture) {
        slotBackground = TabSurface.texture(texture);
        return this;
    }

    public TabBuilder slotBackgroundColor(String hexColor) {
        slotBackground = TabSurface.color(hexColor);
        return this;
    }

    public TabBuilder noSlotBackground() {
        slotBackground = null;
        return this;
    }

    public TabBuilder betweenSlotsTexture(Identifier texture) {
        betweenSlotsBackground = TabSurface.texture(texture);
        return this;
    }

    public TabBuilder betweenSlotsColor(String hexColor) {
        betweenSlotsBackground = TabSurface.color(hexColor);
        return this;
    }

    public TabBuilder gridBackgroundTexture(Identifier texture) {
        return betweenSlotsTexture(texture);
    }

    public TabBuilder gridBackgroundColor(String hexColor) {
        return betweenSlotsColor(hexColor);
    }

    public TabBuilder noGridBackground() {
        betweenSlotsBackground = null;
        return this;
    }

    public TabBuilder tint(String hexColor) {
        tintColor = HexColor.parseTint(hexColor);
        return this;
    }

    public TabBuilder tint(String hexColor, float opacity) {
        tintColor = HexColor.withOpacity(hexColor, opacity);
        return this;
    }

    public TabBuilder vanillaTint(String hexColor) {
        return vanillaAppearance().tint(hexColor);
    }

    public TabBuilder vanillaTint(String hexColor, float opacity) {
        return vanillaAppearance().tint(hexColor, opacity);
    }

    public TabBuilder noTint() {
        tintColor = null;
        return this;
    }

    public TabBuilder tabNameColor(String hexColor) {
        tabNameColor = HexColor.parse(hexColor);
        return this;
    }

    public TabBuilder defaultTabNameColor() {
        tabNameColor = null;
        return this;
    }

    public ItemGroup build() {
        validateAppearance();
        RegistryKey<ItemGroup> key = context.tabs().key(path);
        List<TabCategory> categories = resolveCategories();
        TabProperties.Definition definition = new TabProperties.Definition(
                customOverlay,
                !specialPosition,
                customBackgroundTexture,
                slotBackground,
                betweenSlotsBackground,
                tintColor,
                tabNameColor,
                categories
        );

        builder.entries((displayContext, targetEntries) -> {
            List<TabLayout.Element> uncategorized = TabEntryCapture.capture(
                    uncategorizedOperations,
                    targetEntries
            );
            Map<String, List<TabLayout.Element>> categoryLayouts = new LinkedHashMap<>();
            for (TabCategory category : categories) {
                categoryLayouts.put(
                        category.id(),
                        TabEntryCapture.capture(category.operations(), targetEntries)
                );
            }
            definition.updateLayout(new TabLayout(uncategorized, categoryLayouts));
        });

        ItemGroup group = Registry.register(Registries.ITEM_GROUP, context.id(path), builder.build());
        TabProperties.register(key, definition);
        return group;
    }

    private TabBuilder styledEntry(
            ItemStack stack,
            boolean searchOnly,
            Consumer<TabEntryStyle.Builder> appearance
    ) {
        ItemStack capturedStack = Objects.requireNonNull(stack, "stack").copy();
        TabEntryStyle style = TabEntryStyle.configure(appearance);
        uncategorizedOperations.add(new TabEntriesOperation(
                entries -> entries.add(capturedStack.copy()),
                searchOnly,
                style.slotBackground()
        ));
        return this;
    }

    private void validateAppearance() {
        if (customOverlay && tintColor != null) {
            throw new IllegalStateException(
                    "Creative-tab tint is only available with customOverlay(false), vanillaAppearance(), "
                            + "or vanillaTint(...)"
            );
        }
    }

    private TabCategory.Builder categoryBuilder(String id) {
        TabCategory.Builder category = categoryBuilders.get(Objects.requireNonNull(id, "id"));
        if (category == null) {
            throw new IllegalArgumentException("Unknown creative-tab category id: " + id);
        }
        return category;
    }

    private List<TabCategory> resolveCategories() {
        List<String> order;
        if (explicitCategoryOrder == null) {
            order = List.copyOf(categoryBuilders.keySet());
        } else {
            Set<String> declared = categoryBuilders.keySet();
            Set<String> requested = new LinkedHashSet<>(explicitCategoryOrder);
            if (!declared.equals(requested)) {
                Set<String> missing = new LinkedHashSet<>(declared);
                missing.removeAll(requested);
                Set<String> unknown = new LinkedHashSet<>(requested);
                unknown.removeAll(declared);
                throw new IllegalStateException(
                        "Category order must include every declared category exactly once; missing=" + missing
                                + ", unknown=" + unknown
                );
            }
            order = explicitCategoryOrder;
        }

        List<TabCategory> categories = new ArrayList<>(order.size());
        for (String id : order) {
            categories.add(categoryBuilders.get(id).build());
        }
        return List.copyOf(categories);
    }

    private static void addItems(ItemGroup.Entries target, ItemConvertible[] entries) {
        for (ItemConvertible entry : entries) {
            target.add(Objects.requireNonNull(entry, "entry"));
        }
    }

    private static void addStacks(ItemGroup.Entries target, ItemStack[] stacks) {
        for (ItemStack stack : stacks) {
            target.add(Objects.requireNonNull(stack, "stack"));
        }
    }
}
