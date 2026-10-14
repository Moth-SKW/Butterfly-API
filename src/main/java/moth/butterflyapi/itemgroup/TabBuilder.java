package moth.butterflyapi.itemgroup;

import moth.butterflyapi.mod.ModContext;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
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
    private static final String DEFAULT_SEARCH_BACKGROUND = "item_search";

    private final ModContext context;
    private final String path;
    private final CreativeModeTab.Builder builder;
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

        builder.backgroundTexture(CreativeModeTab.createTextureLocation(DEFAULT_SEARCH_BACKGROUND));
        builder.title(Component.translatable(context.tabs().translationKey(path)));
    }

    public TabBuilder icon(ItemLike icon) {
        Objects.requireNonNull(icon, "icon");
        return icon(() -> new ItemStack(icon));
    }

    public TabBuilder icon(Supplier<ItemStack> icon) {
        builder.icon(Objects.requireNonNull(icon, "icon"));
        return this;
    }

    public TabBuilder displayName(Component displayName) {
        builder.title(Objects.requireNonNull(displayName, "displayName"));
        return this;
    }

    public TabBuilder translationKey(String translationKey) {
        return displayName(Component.translatable(Objects.requireNonNull(translationKey, "translationKey")));
    }

    public TabBuilder entries(Consumer<CreativeModeTab.Output> entries) {
        uncategorizedOperations.add(new TabEntriesOperation(
                Objects.requireNonNull(entries, "entries"),
                false
        ));
        return this;
    }

    public TabBuilder searchOnlyEntries(Consumer<CreativeModeTab.Output> entries) {
        uncategorizedOperations.add(new TabEntriesOperation(
                Objects.requireNonNull(entries, "entries"),
                true
        ));
        return this;
    }

    public TabBuilder add(ItemLike... entries) {
        Objects.requireNonNull(entries, "entries");
        return entries(groupEntries -> addItems(groupEntries, entries));
    }

    public TabBuilder addStacks(ItemStack... stacks) {
        Objects.requireNonNull(stacks, "stacks");
        return entries(groupEntries -> addStacks(groupEntries, stacks));
    }

    public TabBuilder entry(ItemLike entry, Consumer<TabEntryStyle.Builder> appearance) {
        Objects.requireNonNull(entry, "entry");
        return styledEntry(new ItemStack(entry), false, appearance);
    }

    public TabBuilder entry(ItemStack stack, Consumer<TabEntryStyle.Builder> appearance) {
        return styledEntry(Objects.requireNonNull(stack, "stack"), false, appearance);
    }

    public TabBuilder searchOnly(ItemLike... entries) {
        Objects.requireNonNull(entries, "entries");
        return searchOnlyEntries(groupEntries -> addItems(groupEntries, entries));
    }

    public TabBuilder searchOnlyStacks(ItemStack... stacks) {
        Objects.requireNonNull(stacks, "stacks");
        return searchOnlyEntries(groupEntries -> addStacks(groupEntries, stacks));
    }

    public TabBuilder searchOnlyEntry(ItemLike entry, Consumer<TabEntryStyle.Builder> appearance) {
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

    public TabBuilder category(String id, Component text, Consumer<TabCategory.Builder> configuration) {
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
        return category(id, Component.literal(Objects.requireNonNull(text, "text")), configuration);
    }

    public TabBuilder category(String id, Component text) {
        return category(id, text, category -> {
        });
    }

    public TabBuilder category(String id, String text) {
        return category(id, Component.literal(Objects.requireNonNull(text, "text")));
    }

    public TabBuilder categoryEntries(String id, Consumer<CreativeModeTab.Output> entries) {
        categoryBuilder(id).entries(entries);
        return this;
    }

    public TabBuilder searchOnlyCategoryEntries(String id, Consumer<CreativeModeTab.Output> entries) {
        categoryBuilder(id).searchOnlyEntries(entries);
        return this;
    }

    public TabBuilder addToCategory(String id, ItemLike... entries) {
        categoryBuilder(id).add(entries);
        return this;
    }

    public TabBuilder addStacksToCategory(String id, ItemStack... stacks) {
        categoryBuilder(id).addStacks(stacks);
        return this;
    }

    public TabBuilder entryInCategory(
            String id,
            ItemLike entry,
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

    public TabBuilder searchOnlyInCategory(String id, ItemLike... entries) {
        categoryBuilder(id).searchOnly(entries);
        return this;
    }

    public TabBuilder searchOnlyStacksInCategory(String id, ItemStack... stacks) {
        categoryBuilder(id).searchOnlyStacks(stacks);
        return this;
    }

    public TabBuilder searchOnlyEntryInCategory(
            String id,
            ItemLike entry,
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
        builder.alignedRight();
        return this;
    }

    public TabBuilder noScrollbar() {
        builder.noScrollBar();
        return this;
    }

    public TabBuilder noRenderedName() {
        builder.hideTitle();
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
        builder.backgroundTexture(CreativeModeTab.createTextureLocation(Objects.requireNonNull(suffix, "suffix")));
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

    public CreativeModeTab build() {
        validateAppearance();
        ResourceKey<CreativeModeTab> key = context.tabs().key(path);
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

        builder.displayItems((displayContext, targetEntries) -> {
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

        CreativeModeTab group = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, context.id(path), builder.build());
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
                entries -> entries.accept(capturedStack.copy()),
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

    private static void addItems(CreativeModeTab.Output target, ItemLike[] entries) {
        for (ItemLike entry : entries) {
            target.accept(Objects.requireNonNull(entry, "entry"));
        }
    }

    private static void addStacks(CreativeModeTab.Output target, ItemStack[] stacks) {
        for (ItemStack stack : stacks) {
            target.accept(Objects.requireNonNull(stack, "stack"));
        }
    }
}
