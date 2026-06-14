package moth.butterflyapi.client.mixin;

import moth.butterflyapi.client.CreativeInventoryScreenExtension;
import moth.butterflyapi.itemgroup.TabCategory;
import moth.butterflyapi.itemgroup.TabLayout;
import moth.butterflyapi.itemgroup.TabProperties;
import moth.butterflyapi.itemgroup.TabSurface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.search.SearchManager;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Mixin(value = CreativeInventoryScreen.class, priority = 900)
public abstract class CreativeInventoryScreenMixin implements CreativeInventoryScreenExtension {
    private static final int GRID_COLUMNS = 9;
    private static final int VISIBLE_ROWS = 5;
    private static final int SLOT_SIZE = 16;
    private static final int SLOT_STRIDE = 18;
    private static final int GRID_WIDTH = GRID_COLUMNS * SLOT_STRIDE;
    private static final int GRID_HEIGHT = VISIBLE_ROWS * SLOT_STRIDE;
    private static final int CATEGORY_WIDTH = (GRID_COLUMNS - 1) * SLOT_STRIDE + SLOT_SIZE;
    private static final int CATEGORY_HEIGHT = SLOT_SIZE;
    private static final int GRID_OFFSET_X = 8;
    private static final int GRID_OFFSET_Y = 17;
    private static final int CREATIVE_BACKGROUND_WIDTH = 195;
    private static final int CREATIVE_BACKGROUND_HEIGHT = 136;
    private static final int CATEGORY_TRAILING_ROWS = 2;
    private static final int CATEGORY_TEXT_PADDING = 4;

    @Shadow
    private static ItemGroup selectedTab;

    @Shadow
    private TextFieldWidget searchBox;

    @Shadow
    private float scrollPosition;

    @Unique
    private Map<Integer, TabCategory> butterflyApi$categoryRows = Map.of();

    @Unique
    private Map<Integer, TabSurface> butterflyApi$slotOverrides = Map.of();

    @Redirect(
            method = "drawBackground",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/util/Identifier",
                    ordinal = 0
            ),
            require = 0
    )
    private Identifier butterflyApi$replaceCreativeBackground(String vanillaTexturePath) {
        TabProperties.Definition definition = butterflyApi$getSelectedDefinition();
        if (definition != null
                && definition.customOverlay()
                && definition.backgroundTexture() != null) {
            return definition.backgroundTexture();
        }
        return new Identifier(vanillaTexturePath);
    }

    @Redirect(
            method = "drawForeground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemGroup;getDisplayName()Lnet/minecraft/text/Text;"
            ),
            require = 0
    )
    private Text butterflyApi$colorTabName(ItemGroup group) {
        Text displayName = group.getDisplayName();
        RegistryKey<ItemGroup> key = butterflyApi$getTabKey(group);
        TabProperties.Definition definition = key == null ? null : TabProperties.get(key);
        if (definition == null || definition.tabNameColor() == null) {
            return displayName;
        }
        int rgb = definition.tabNameColor() & 0x00FFFFFF;
        return displayName.copy().styled(style -> style.withColor(rgb));
    }

    @Inject(method = "drawBackground", at = @At("TAIL"), require = 0)
    private void butterflyApi$drawCustomAppearance(
            DrawContext context,
            float delta,
            int mouseX,
            int mouseY,
            CallbackInfo ci
    ) {
        TabProperties.Definition definition = butterflyApi$getSelectedDefinition();
        if (definition == null) {
            return;
        }

        if (!((Object) this instanceof HandledScreenAccessor handledScreen)) {
            return;
        }
        int screenX = handledScreen.butterflyApi$getX();
        int screenY = handledScreen.butterflyApi$getY();

        if (definition.customOverlay()) {
            butterflyApi$drawGridOverlay(context, definition, screenX, screenY);
        }

        if (!definition.customOverlay() && definition.tintColor() != null) {
            context.fill(
                    screenX,
                    screenY,
                    screenX + CREATIVE_BACKGROUND_WIDTH,
                    screenY + CREATIVE_BACKGROUND_HEIGHT,
                    definition.tintColor()
            );
        }
    }

    @Unique
    private void butterflyApi$drawGridOverlay(
            DrawContext context,
            TabProperties.Definition definition,
            int screenX,
            int screenY
    ) {
        int gridX = screenX + GRID_OFFSET_X;
        int gridY = screenY + GRID_OFFSET_Y;

        if (definition.betweenSlotsBackground() != null) {
            butterflyApi$drawSurface(
                    context,
                    definition.betweenSlotsBackground(),
                    gridX,
                    gridY,
                    GRID_WIDTH,
                    GRID_HEIGHT
            );
        }

        int contentX = gridX + 1;
        int contentY = gridY + 1;

        CreativeInventoryScreen screen = (CreativeInventoryScreen) (Object) this;
        Object rawHandler = screen.getScreenHandler();
        if (!(rawHandler instanceof CreativeScreenHandlerAccessor handler)) {
            return;
        }
        int firstVisibleRow = handler.butterflyApi$getVisibleRow(scrollPosition);

        for (int visibleRow = 0; visibleRow < VISIBLE_ROWS; visibleRow++) {
            int logicalRow = firstVisibleRow + visibleRow;
            TabCategory category = butterflyApi$categoryRows.get(logicalRow);
            int rowY = contentY + visibleRow * SLOT_STRIDE;

            if (category != null) {
                butterflyApi$drawCategory(context, category, contentX, rowY);
                continue;
            }

            for (int column = 0; column < GRID_COLUMNS; column++) {
                int logicalIndex = logicalRow * GRID_COLUMNS + column;
                TabSurface slotSurface = butterflyApi$slotOverrides.get(logicalIndex);
                if (slotSurface == null) {
                    slotSurface = definition.slotBackground();
                }
                if (slotSurface != null) {
                    butterflyApi$drawSurface(
                            context,
                            slotSurface,
                            contentX + column * SLOT_STRIDE,
                            rowY,
                            SLOT_SIZE,
                            SLOT_SIZE
                    );
                }
            }
        }
    }

    @Inject(method = "getTabX", at = @At("RETURN"), cancellable = true, require = 0)
    private void butterflyApi$normalizeTabButtonPosition(
            ItemGroup group,
            CallbackInfoReturnable<Integer> cir
    ) {
        TabProperties.Definition definition = butterflyApi$getDefinition(group);
        if (definition == null || !definition.normalTabPosition()) {
            return;
        }

        int normalX = group.getColumn() * 27;
        Integer currentX = cir.getReturnValue();
        if (currentX != null && currentX == normalX + 7) {
            cir.setReturnValue(normalX);
        }
    }

    @Inject(method = "search", at = @At("HEAD"), cancellable = true, require = 0)
    private void butterflyApi$searchCurrentTab(CallbackInfo ci) {
        TabProperties.Definition definition = butterflyApi$getSelectedDefinition();
        if (definition == null) {
            return;
        }

        butterflyApi$rebuildVisibleItems(definition);
        ci.cancel();
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true, require = 0)
    private void butterflyApi$typeInScopedSearch(
            char character,
            int modifiers,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!butterflyApi$usesInjectedSearchInput()) {
            return;
        }

        String previousText = searchBox.getText();
        boolean handled = searchBox.charTyped(character, modifiers);
        if (!Objects.equals(previousText, searchBox.getText())) {
            butterflyApi$rebuildCurrentTab();
        }
        if (handled) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, require = 0)
    private void butterflyApi$pressKeyInScopedSearch(
            int keyCode,
            int scanCode,
            int modifiers,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!butterflyApi$usesInjectedSearchInput()) {
            return;
        }

        String previousText = searchBox.getText();
        if (searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            if (!Objects.equals(previousText, searchBox.getText())) {
                butterflyApi$rebuildCurrentTab();
            }
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "setSelectedTab", at = @At("RETURN"), require = 0)
    private void butterflyApi$prepareSelectedTab(ItemGroup group, CallbackInfo ci) {
        TabProperties.Definition definition = butterflyApi$getDefinition(group);
        if (searchBox == null || definition == null) {
            butterflyApi$categoryRows = Map.of();
            butterflyApi$slotOverrides = Map.of();
            return;
        }

        searchBox.setVisible(true);
        searchBox.setFocusUnlocked(false);
        searchBox.setFocused(true);
        butterflyApi$rebuildVisibleItems(definition);
    }

    @Unique
    private boolean butterflyApi$usesInjectedSearchInput() {
        return searchBox != null && butterflyApi$getSelectedDefinition() != null;
    }

    @Unique
    private void butterflyApi$rebuildCurrentTab() {
        TabProperties.Definition definition = butterflyApi$getSelectedDefinition();
        if (definition != null) {
            butterflyApi$rebuildVisibleItems(definition);
        }
    }

    @Unique
    private void butterflyApi$rebuildVisibleItems(TabProperties.Definition definition) {
        CreativeInventoryScreen screen = (CreativeInventoryScreen) (Object) this;
        CreativeInventoryScreen.CreativeScreenHandler screenHandler = screen.getScreenHandler();
        Object rawHandler = screenHandler;
        if (!(rawHandler instanceof CreativeScreenHandlerAccessor handler)) {
            return;
        }

        DefaultedList<ItemStack> itemList = handler.butterflyApi$getItemList();
        itemList.clear();

        ResolvedLayout resolved = butterflyApi$resolveLayout(
                definition,
                selectedTab.getDisplayStacks(),
                selectedTab.getSearchTabStacks()
        );
        String query = butterflyApi$getNormalizedQuery();
        boolean searching = !query.isEmpty();
        SearchMatcher matcher = butterflyApi$createMatcher(query);

        if (definition.customOverlay()) {
            butterflyApi$appendCustomLayout(itemList, definition, resolved, matcher, searching);
        } else {
            butterflyApi$appendVanillaLayout(itemList, definition, resolved, matcher, searching);
            butterflyApi$categoryRows = Map.of();
            butterflyApi$slotOverrides = Map.of();
        }

        scrollPosition = 0.0F;
        screenHandler.scrollItems(scrollPosition);
    }

    @Unique
    private void butterflyApi$appendCustomLayout(
            DefaultedList<ItemStack> itemList,
            TabProperties.Definition definition,
            ResolvedLayout resolved,
            SearchMatcher matcher,
            boolean searching
    ) {
        Map<Integer, TabCategory> categoryRows = new HashMap<>();
        Map<Integer, TabSurface> slotOverrides = new HashMap<>();

        butterflyApi$appendSection(
                itemList,
                resolved.uncategorized(),
                matcher,
                searching,
                slotOverrides
        );

        for (TabCategory category : definition.categories()) {
            List<TabLayout.Element> categoryEntries = resolved.category(category.id());
            if (!butterflyApi$containsMatchingStack(categoryEntries, matcher, searching)) {
                continue;
            }

            butterflyApi$padToNextRow(itemList);
            int categoryRow = itemList.size() / GRID_COLUMNS;
            categoryRows.put(categoryRow, category);
            butterflyApi$addEmptySlots(itemList, GRID_COLUMNS);

            butterflyApi$appendSection(
                    itemList,
                    categoryEntries,
                    matcher,
                    searching,
                    slotOverrides
            );

            int minimumSize = (categoryRow + CATEGORY_TRAILING_ROWS + 1) * GRID_COLUMNS;
            while (itemList.size() < minimumSize) {
                itemList.add(ItemStack.EMPTY);
            }
        }

        butterflyApi$categoryRows = Map.copyOf(categoryRows);
        butterflyApi$slotOverrides = Map.copyOf(slotOverrides);
    }

    @Unique
    private static void butterflyApi$appendVanillaLayout(
            DefaultedList<ItemStack> itemList,
            TabProperties.Definition definition,
            ResolvedLayout resolved,
            SearchMatcher matcher,
            boolean searching
    ) {
        butterflyApi$appendStacksOnly(itemList, resolved.uncategorized(), matcher, searching);
        for (TabCategory category : definition.categories()) {
            butterflyApi$appendStacksOnly(itemList, resolved.category(category.id()), matcher, searching);
        }
    }

    @Unique
    private static ResolvedLayout butterflyApi$resolveLayout(
            TabProperties.Definition definition,
            Collection<ItemStack> displayStacks,
            Collection<ItemStack> searchStacks
    ) {
        TabLayout layout = definition.layout();
        List<ItemStack> remainingDisplay = butterflyApi$copyStacks(displayStacks);
        List<ItemStack> remainingSearch = butterflyApi$copyStacks(searchStacks);

        List<TabLayout.Element> uncategorized = butterflyApi$resolveSection(
                layout.uncategorized(),
                remainingDisplay,
                remainingSearch
        );
        Map<String, List<TabLayout.Element>> categories = new HashMap<>();

        for (TabCategory category : definition.categories()) {
            categories.put(
                    category.id(),
                    butterflyApi$resolveSection(
                            layout.category(category.id()),
                            remainingDisplay,
                            remainingSearch
                    )
            );
        }

        for (ItemStack extra : remainingDisplay) {
            uncategorized.add(new TabLayout.StackElement(extra, false));
            butterflyApi$removeEquivalent(remainingSearch, extra);
        }
        for (ItemStack extra : remainingSearch) {
            uncategorized.add(new TabLayout.StackElement(extra, true));
        }

        return new ResolvedLayout(List.copyOf(uncategorized), Map.copyOf(categories));
    }

    @Unique
    private static List<ItemStack> butterflyApi$copyStacks(Collection<ItemStack> stacks) {
        List<ItemStack> copied = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            copied.add(stack.copy());
        }
        return copied;
    }

    @Unique
    private static List<TabLayout.Element> butterflyApi$resolveSection(
            List<TabLayout.Element> source,
            List<ItemStack> remainingDisplay,
            List<ItemStack> remainingSearch
    ) {
        List<TabLayout.Element> resolved = new ArrayList<>();
        for (TabLayout.Element element : source) {
            if (element instanceof TabLayout.StackElement stackElement) {
                ItemStack expected = stackElement.stack();
                List<ItemStack> sourceStacks = stackElement.searchOnly()
                        ? remainingSearch
                        : remainingDisplay;
                int matchingIndex = butterflyApi$findEquivalent(sourceStacks, expected);
                if (matchingIndex < 0) {
                    continue;
                }

                ItemStack actual = sourceStacks.remove(matchingIndex);
                if (!stackElement.searchOnly()) {
                    butterflyApi$removeEquivalent(remainingSearch, actual);
                }
                resolved.add(new TabLayout.StackElement(
                        actual,
                        stackElement.searchOnly(),
                        stackElement.slotBackground()
                ));
            } else {
                resolved.add(element);
            }
        }
        return resolved;
    }

    @Unique
    private static int butterflyApi$findEquivalent(List<ItemStack> stacks, ItemStack expected) {
        for (int index = 0; index < stacks.size(); index++) {
            if (ItemStack.canCombine(stacks.get(index), expected)) {
                return index;
            }
        }
        return -1;
    }

    @Unique
    private static void butterflyApi$removeEquivalent(List<ItemStack> stacks, ItemStack expected) {
        int index = butterflyApi$findEquivalent(stacks, expected);
        if (index >= 0) {
            stacks.remove(index);
        }
    }

    @Unique
    private static SearchMatcher butterflyApi$createMatcher(String query) {
        if (query.isEmpty()) {
            return stack -> true;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        boolean tagSearch = query.startsWith("#");
        String searchText = tagSearch ? query.substring(1) : query;
        List<ItemStack> globalMatches = List.of();

        if ((Object) client instanceof MinecraftClientAccessor accessor) {
            try {
                SearchManager searchManager = accessor.butterflyApi$getSearchManager();
                globalMatches = searchManager
                        .get(tagSearch ? SearchManager.ITEM_TAG : SearchManager.ITEM_TOOLTIP)
                        .findAll(searchText);
            } catch (RuntimeException ignored) {
                globalMatches = List.of();
            }
        }

        List<ItemStack> safeGlobalMatches = globalMatches;
        return stack -> {
            if (butterflyApi$containsEquivalent(safeGlobalMatches, stack)) {
                return true;
            }
            if (tagSearch) {
                return false;
            }
            Identifier itemId = Registries.ITEM.getId(stack.getItem());
            String searchableText = (stack.getName().getString() + " " + itemId).toLowerCase(Locale.ROOT);
            return searchableText.contains(searchText);
        };
    }

    @Unique
    private static boolean butterflyApi$containsEquivalent(List<ItemStack> candidates, ItemStack stack) {
        for (ItemStack candidate : candidates) {
            if (ItemStack.canCombine(candidate, stack)) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private static boolean butterflyApi$containsMatchingStack(
            List<TabLayout.Element> elements,
            SearchMatcher matcher,
            boolean searching
    ) {
        for (TabLayout.Element element : elements) {
            if (element instanceof TabLayout.StackElement stackElement
                    && butterflyApi$isVisible(stackElement, searching)
                    && matcher.matches(stackElement.stack())) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private static boolean butterflyApi$isVisible(TabLayout.StackElement element, boolean searching) {
        return !element.searchOnly() || searching;
    }

    @Unique
    private static void butterflyApi$appendSection(
            DefaultedList<ItemStack> itemList,
            List<TabLayout.Element> elements,
            SearchMatcher matcher,
            boolean searching,
            Map<Integer, TabSurface> slotOverrides
    ) {
        if (!butterflyApi$containsMatchingStack(elements, matcher, searching)) {
            return;
        }

        for (TabLayout.Element element : elements) {
            if (element instanceof TabLayout.StackElement stackElement) {
                ItemStack stack = stackElement.stack();
                if (butterflyApi$isVisible(stackElement, searching) && matcher.matches(stack)) {
                    int logicalIndex = itemList.size();
                    itemList.add(stack);
                    if (stackElement.slotBackground() != null) {
                        slotOverrides.put(logicalIndex, stackElement.slotBackground());
                    }
                }
            } else if (element == TabLayout.EmptySlot.INSTANCE) {
                itemList.add(ItemStack.EMPTY);
            } else if (element == TabLayout.EmptyRow.INSTANCE) {
                butterflyApi$padToNextRow(itemList);
                butterflyApi$addEmptySlots(itemList, GRID_COLUMNS);
            }
        }
    }

    @Unique
    private static void butterflyApi$appendStacksOnly(
            DefaultedList<ItemStack> itemList,
            List<TabLayout.Element> elements,
            SearchMatcher matcher,
            boolean searching
    ) {
        for (TabLayout.Element element : elements) {
            if (element instanceof TabLayout.StackElement stackElement) {
                ItemStack stack = stackElement.stack();
                if (butterflyApi$isVisible(stackElement, searching) && matcher.matches(stack)) {
                    itemList.add(stack);
                }
            }
        }
    }

    @Unique
    private static void butterflyApi$padToNextRow(DefaultedList<ItemStack> itemList) {
        int remainder = itemList.size() % GRID_COLUMNS;
        if (remainder != 0) {
            butterflyApi$addEmptySlots(itemList, GRID_COLUMNS - remainder);
        }
    }

    @Unique
    private static void butterflyApi$addEmptySlots(DefaultedList<ItemStack> itemList, int count) {
        for (int slot = 0; slot < count; slot++) {
            itemList.add(ItemStack.EMPTY);
        }
    }

    @Unique
    private static void butterflyApi$drawCategory(
            DrawContext context,
            TabCategory category,
            int x,
            int y
    ) {
        butterflyApi$drawSurface(context, category.background(), x, y, CATEGORY_WIDTH, CATEGORY_HEIGHT);
        if (category.borderColor() != null) {
            context.drawBorder(x, y, CATEGORY_WIDTH, CATEGORY_HEIGHT, category.borderColor());
        }

        MinecraftClient client = MinecraftClient.getInstance();
        int color = category.textColor() & 0x00FFFFFF;
        int textY = y + 4;
        context.enableScissor(x + 2, y + 1, x + CATEGORY_WIDTH - 2, y + CATEGORY_HEIGHT - 1);

        switch (category.textAlignment()) {
            case LEFT -> context.drawTextWithShadow(
                    client.textRenderer,
                    category.text(),
                    x + CATEGORY_TEXT_PADDING,
                    textY,
                    color
            );
            case CENTER -> context.drawCenteredTextWithShadow(
                    client.textRenderer,
                    category.text(),
                    x + CATEGORY_WIDTH / 2,
                    textY,
                    color
            );
            case RIGHT -> context.drawTextWithShadow(
                    client.textRenderer,
                    category.text(),
                    x + CATEGORY_WIDTH - CATEGORY_TEXT_PADDING - client.textRenderer.getWidth(category.text()),
                    textY,
                    color
            );
        }

        context.disableScissor();
    }

    @Unique
    private static void butterflyApi$drawSurface(
            DrawContext context,
            TabSurface surface,
            int x,
            int y,
            int width,
            int height
    ) {
        if (surface.texture() != null) {
            context.drawTexture(surface.texture(), x, y, 0.0F, 0.0F, width, height, width, height);
        } else if (surface.color() != null) {
            context.fill(x, y, x + width, y + height, surface.color());
        }
    }

    @Override
    public boolean butterflyApi$usesCustomGridOverlay() {
        TabProperties.Definition definition = butterflyApi$getSelectedDefinition();
        return definition != null && definition.usesCustomGridOverlay();
    }

    @Override
    public boolean butterflyApi$isCategorySlotFocused() {
        if (!butterflyApi$usesCustomGridOverlay()) {
            return false;
        }

        if (!((Object) this instanceof HandledScreenAccessor handledScreen)) {
            return false;
        }
        net.minecraft.screen.slot.Slot focusedSlot = handledScreen.butterflyApi$getFocusedSlot();
        if (focusedSlot == null || focusedSlot.id < 0 || focusedSlot.id >= GRID_COLUMNS * VISIBLE_ROWS) {
            return false;
        }

        CreativeInventoryScreen screen = (CreativeInventoryScreen) (Object) this;
        Object rawHandler = screen.getScreenHandler();
        if (!(rawHandler instanceof CreativeScreenHandlerAccessor handler)) {
            return false;
        }
        int firstVisibleRow = handler.butterflyApi$getVisibleRow(scrollPosition);
        int focusedLogicalRow = firstVisibleRow + focusedSlot.id / GRID_COLUMNS;
        return butterflyApi$categoryRows.containsKey(focusedLogicalRow);
    }

    @Unique
    private String butterflyApi$getNormalizedQuery() {
        return searchBox == null ? "" : searchBox.getText().trim().toLowerCase(Locale.ROOT);
    }

    @Unique
    @Nullable
    private static TabProperties.Definition butterflyApi$getSelectedDefinition() {
        return butterflyApi$getDefinition(selectedTab);
    }

    @Unique
    @Nullable
    private static TabProperties.Definition butterflyApi$getDefinition(ItemGroup group) {
        RegistryKey<ItemGroup> key = butterflyApi$getTabKey(group);
        return key == null ? null : TabProperties.get(key);
    }

    @Unique
    @Nullable
    private static RegistryKey<ItemGroup> butterflyApi$getTabKey(ItemGroup group) {
        if (group == null) {
            return null;
        }
        Optional<RegistryKey<ItemGroup>> key = Registries.ITEM_GROUP.getKey(group);
        return key.orElse(null);
    }

    private interface SearchMatcher {
        boolean matches(ItemStack stack);
    }

    private record ResolvedLayout(
            List<TabLayout.Element> uncategorized,
            Map<String, List<TabLayout.Element>> categories
    ) {
        private List<TabLayout.Element> category(String id) {
            return categories.getOrDefault(id, List.of());
        }
    }
}
