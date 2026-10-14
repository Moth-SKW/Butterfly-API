package moth.butterflyapi.client.mixin;

//? if >=1.21.1 {

import moth.butterflyapi.ButterflyApi;
import moth.butterflyapi.client.CreativeInventoryScreenExtension;
import moth.butterflyapi.itemgroup.TabCategory;
import moth.butterflyapi.itemgroup.TabLayout;
import moth.butterflyapi.itemgroup.TabProperties;
import moth.butterflyapi.itemgroup.TabSurface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
//? if >=1.21.11 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
//?}
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.SessionSearchTrees;
//? if >=1.21.11 {
import net.minecraft.client.renderer.RenderPipelines;
//?}
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
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

@Mixin(value = CreativeModeInventoryScreen.class, priority = 900)
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
    private static CreativeModeTab selectedTab;

    @Shadow
    private EditBox searchBox;

    @Shadow
    private float scrollOffs;

    @Unique
    private Map<Integer, TabCategory> butterflyApi$categoryRows = Map.of();

    @Unique
    private Map<Integer, TabSurface> butterflyApi$slotOverrides = Map.of();

    @Unique
    private static boolean butterflyApi$warnedHandledScreenAccessor;

    @Unique
    private static boolean butterflyApi$warnedCreativeHandlerAccessor;

    @Unique
    private static boolean butterflyApi$warnedSearchManager;

//? if >=26.1 {
/*
    @Redirect(
            method = "extractBackground",
*///?} else {
    @Redirect(
            method = "renderBg",
//?}
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/CreativeModeTab;getBackgroundTexture()Lnet/minecraft/resources/Identifier;"
            ),
            require = 1
    )
    private Identifier butterflyApi$replaceCreativeBackground(CreativeModeTab group) {
        TabProperties.Definition definition = butterflyApi$getDefinition(group);
        if (definition != null
                && definition.customOverlay()
                && definition.backgroundTexture() != null) {
            return definition.backgroundTexture();
        }
        return group.getBackgroundTexture();
    }

//? if >=26.1 {
/*
    @Redirect(
            method = "extractLabels",
*///?} else {
    @Redirect(
            method = "renderLabels",
//?}
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/CreativeModeTab;getDisplayName()Lnet/minecraft/network/chat/Component;"
            ),
            require = 1
    )
    private Component butterflyApi$colorTabName(CreativeModeTab group) {
        Component displayName = group.getDisplayName();
        ResourceKey<CreativeModeTab> key = butterflyApi$getTabKey(group);
        TabProperties.Definition definition = key == null ? null : TabProperties.get(key);
        if (definition == null || definition.tabNameColor() == null) {
            return displayName;
        }
        int rgb = definition.tabNameColor() & 0x00FFFFFF;
        return displayName.copy().withStyle(style -> style.withColor(rgb));
    }

//? if >=26.1 {
/*
    @Inject(method = "extractBackground", at = @At("TAIL"), require = 1)
    private void butterflyApi$drawCustomAppearance(
            GuiGraphicsExtractor context,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
        TabProperties.Definition definition = butterflyApi$getSelectedDefinition();
        if (definition == null) {
            return;
        }

        if (!((Object) this instanceof HandledScreenAccessor handledScreen)) {
            butterflyApi$warnMissingHandledScreenAccessor();
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
*///?} else {
    @Inject(method = "renderBg", at = @At("TAIL"), require = 1)
    private void butterflyApi$drawCustomAppearance(
            GuiGraphics context,
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
            butterflyApi$warnMissingHandledScreenAccessor();
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
//?}

    @Unique
    private void butterflyApi$drawGridOverlay(
            GuiGraphics context,
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

        CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) (Object) this;
        Object rawHandler = screen.getMenu();
        if (!(rawHandler instanceof CreativeScreenHandlerAccessor handler)) {
            butterflyApi$warnMissingCreativeHandlerAccessor();
            return;
        }
        int firstVisibleRow = handler.butterflyApi$getVisibleRow(scrollOffs);

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

    @Inject(method = "getTabX", at = @At("RETURN"), cancellable = true, require = 1)
    private void butterflyApi$normalizeTabButtonPosition(
            CreativeModeTab group,
            CallbackInfoReturnable<Integer> cir
    ) {
        TabProperties.Definition definition = butterflyApi$getDefinition(group);
        if (definition == null || !definition.normalTabPosition()) {
            return;
        }

        int normalX = group.column() * 27;
        Integer currentX = cir.getReturnValue();
        if (currentX != null && currentX == normalX + 7) {
            cir.setReturnValue(normalX);
        }
    }

    @Inject(method = "refreshSearchResults", at = @At("HEAD"), cancellable = true, require = 1)
    private void butterflyApi$searchCurrentTab(CallbackInfo ci) {
        TabProperties.Definition definition = butterflyApi$getSelectedDefinition();
        if (definition == null) {
            return;
        }

        butterflyApi$rebuildVisibleItems(definition);
        ci.cancel();
    }

//? if >=1.21.11 {
    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true, require = 1)
    private void butterflyApi$typeInScopedSearch(
            CharacterEvent input,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!butterflyApi$usesInjectedSearchInput()) {
            return;
        }

        String previousText = searchBox.getValue();
        boolean handled = searchBox.charTyped(input);
        if (!Objects.equals(previousText, searchBox.getValue())) {
            butterflyApi$rebuildCurrentTab();
        }
        if (handled) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, require = 1)
    private void butterflyApi$pressKeyInScopedSearch(
            KeyEvent input,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!butterflyApi$usesInjectedSearchInput()) {
            return;
        }

        String previousText = searchBox.getValue();
        if (searchBox.keyPressed(input)) {
            if (!Objects.equals(previousText, searchBox.getValue())) {
                butterflyApi$rebuildCurrentTab();
            }
            cir.setReturnValue(true);
        }
    }
//?} else {
/*
    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true, require = 1)
    private void butterflyApi$typeInScopedSearch(
            char typedCharacter,
            int modifiers,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!butterflyApi$usesInjectedSearchInput()) {
            return;
        }

        String previousText = searchBox.getValue();
        boolean handled = searchBox.charTyped(typedCharacter, modifiers);
        if (!Objects.equals(previousText, searchBox.getValue())) {
            butterflyApi$rebuildCurrentTab();
        }
        if (handled) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, require = 1)
    private void butterflyApi$pressKeyInScopedSearch(
            int keyCode,
            int scanCode,
            int modifiers,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!butterflyApi$usesInjectedSearchInput()) {
            return;
        }

        String previousText = searchBox.getValue();
        if (searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            if (!Objects.equals(previousText, searchBox.getValue())) {
                butterflyApi$rebuildCurrentTab();
            }
            cir.setReturnValue(true);
        }
    }
*///?}

    @Inject(method = "init", at = @At("TAIL"), require = 1)
    private void butterflyApi$prepareInitialTab(CallbackInfo ci) {
        butterflyApi$prepareCurrentTab();
    }

    @Inject(method = "selectTab", at = @At("RETURN"), require = 1)
    private void butterflyApi$prepareSelectedTab(CreativeModeTab group, CallbackInfo ci) {
        butterflyApi$prepareTab(group);
    }

    @Inject(method = "refreshCurrentTabContents", at = @At("RETURN"), require = 1)
    private void butterflyApi$restoreLayoutAfterVanillaRefresh(
            Collection<ItemStack> displayStacks,
            CallbackInfo ci
    ) {
        butterflyApi$prepareCurrentTab();
    }

    @Unique
    private void butterflyApi$prepareCurrentTab() {
        butterflyApi$prepareTab(selectedTab);
    }

    @Unique
    private void butterflyApi$prepareTab(CreativeModeTab group) {
        TabProperties.Definition definition = butterflyApi$getDefinition(group);
        if (definition == null) {
            butterflyApi$categoryRows = Map.of();
            butterflyApi$slotOverrides = Map.of();
            return;
        }

        if (searchBox != null) {
            searchBox.setVisible(true);
            searchBox.setCanLoseFocus(false);
            searchBox.setFocused(true);
        }

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
        CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) (Object) this;
        CreativeModeInventoryScreen.ItemPickerMenu screenHandler = screen.getMenu();
        Object rawHandler = screenHandler;
        if (!(rawHandler instanceof CreativeScreenHandlerAccessor handler)) {
            butterflyApi$warnMissingCreativeHandlerAccessor();
            return;
        }

        NonNullList<ItemStack> itemList = handler.butterflyApi$getItemList();
        itemList.clear();

        ResolvedLayout resolved = butterflyApi$resolveLayout(
                definition,
                selectedTab.getDisplayItems(),
                selectedTab.getSearchTabDisplayItems()
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

        scrollOffs = 0.0F;
        screenHandler.scrollTo(scrollOffs);
    }

    @Unique
    private void butterflyApi$appendCustomLayout(
            NonNullList<ItemStack> itemList,
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
            NonNullList<ItemStack> itemList,
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
            if (ItemStack.isSameItemSameComponents(stacks.get(index), expected)) {
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

        Minecraft client = Minecraft.getInstance();
        boolean tagSearch = query.startsWith("#");
        String searchText = tagSearch ? query.substring(1) : query;
        List<ItemStack> globalMatches = List.of();

        ClientPacketListener networkHandler = client.getConnection();
        if (networkHandler != null) {
            try {
                SessionSearchTrees searchManager = networkHandler.searchTrees();
                globalMatches = (tagSearch
                        ? searchManager.creativeTagSearch()
                        : searchManager.creativeNameSearch())
                        .search(searchText);
            } catch (RuntimeException ignored) {
                globalMatches = List.of();
            }
        } else {
            butterflyApi$warnMissingSearchManager();
        }

        List<ItemStack> safeGlobalMatches = globalMatches;
        return stack -> {
            if (butterflyApi$containsEquivalent(safeGlobalMatches, stack)) {
                return true;
            }
            if (tagSearch) {
                return false;
            }
            Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            String searchableText = (stack.getHoverName().getString() + " " + itemId).toLowerCase(Locale.ROOT);
            return searchableText.contains(searchText);
        };
    }

    @Unique
    private static boolean butterflyApi$containsEquivalent(List<ItemStack> candidates, ItemStack stack) {
        for (ItemStack candidate : candidates) {
            if (ItemStack.isSameItemSameComponents(candidate, stack)) {
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
            NonNullList<ItemStack> itemList,
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
            NonNullList<ItemStack> itemList,
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
    private static void butterflyApi$padToNextRow(NonNullList<ItemStack> itemList) {
        int remainder = itemList.size() % GRID_COLUMNS;
        if (remainder != 0) {
            butterflyApi$addEmptySlots(itemList, GRID_COLUMNS - remainder);
        }
    }

    @Unique
    private static void butterflyApi$addEmptySlots(NonNullList<ItemStack> itemList, int count) {
        for (int slot = 0; slot < count; slot++) {
            itemList.add(ItemStack.EMPTY);
        }
    }

    @Unique
    private static void butterflyApi$drawCategory(
            GuiGraphics context,
            TabCategory category,
            int x,
            int y
    ) {
        butterflyApi$drawSurface(context, category.background(), x, y, CATEGORY_WIDTH, CATEGORY_HEIGHT);
        if (category.borderColor() != null) {
            int border = category.borderColor();
            context.hLine(x, x + CATEGORY_WIDTH - 1, y, border);
            context.hLine(x, x + CATEGORY_WIDTH - 1, y + CATEGORY_HEIGHT - 1, border);
            context.vLine(x, y, y + CATEGORY_HEIGHT - 1, border);
            context.vLine(x + CATEGORY_WIDTH - 1, y, y + CATEGORY_HEIGHT - 1, border);
        }

        Minecraft client = Minecraft.getInstance();
        int color = category.textColor();
        int textY = y + 4;
        context.enableScissor(x + 2, y + 1, x + CATEGORY_WIDTH - 2, y + CATEGORY_HEIGHT - 1);

        switch (category.textAlignment()) {
            case LEFT -> context.drawString(
                    client.font,
                    category.label(),
                    x + CATEGORY_TEXT_PADDING,
                    textY,
                    color
            );
            case CENTER -> context.drawCenteredString(
                    client.font,
                    category.label(),
                    x + CATEGORY_WIDTH / 2,
                    textY,
                    color
            );
            case RIGHT -> context.drawString(
                    client.font,
                    category.label(),
                    x + CATEGORY_WIDTH - CATEGORY_TEXT_PADDING - client.font.width(category.label()),
                    textY,
                    color
            );
        }

        context.disableScissor();
    }

    @Unique
    private static void butterflyApi$drawSurface(
            GuiGraphics context,
            TabSurface surface,
            int x,
            int y,
            int width,
            int height
    ) {
        if (surface.texture() != null) {
//? if >=1.21.11 {
            context.blit(
                    RenderPipelines.GUI_TEXTURED,
                    surface.texture(),
                    x,
                    y,
                    0.0F,
                    0.0F,
                    width,
                    height,
                    width,
                    height
            );
//?} else {
/*
            context.blit(
                    surface.texture(),
                    x,
                    y,
                    0,
                    0.0F,
                    0.0F,
                    width,
                    height,
                    width,
                    height
            );
*///?}
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
            butterflyApi$warnMissingHandledScreenAccessor();
            return false;
        }
        net.minecraft.world.inventory.Slot focusedSlot = handledScreen.butterflyApi$getFocusedSlot();
        if (focusedSlot == null || focusedSlot.index < 0 || focusedSlot.index >= GRID_COLUMNS * VISIBLE_ROWS) {
            return false;
        }

        CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) (Object) this;
        Object rawHandler = screen.getMenu();
        if (!(rawHandler instanceof CreativeScreenHandlerAccessor handler)) {
            butterflyApi$warnMissingCreativeHandlerAccessor();
            return false;
        }
        int firstVisibleRow = handler.butterflyApi$getVisibleRow(scrollOffs);
        int focusedLogicalRow = firstVisibleRow + focusedSlot.index / GRID_COLUMNS;
        return butterflyApi$categoryRows.containsKey(focusedLogicalRow);
    }

    @Unique
    private String butterflyApi$getNormalizedQuery() {
        return searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
    }

    @Unique
    private static void butterflyApi$warnMissingHandledScreenAccessor() {
        if (butterflyApi$warnedHandledScreenAccessor) {
            return;
        }
        butterflyApi$warnedHandledScreenAccessor = true;
        ButterflyApi.LOGGER.error(
                "HandledScreenAccessor was not applied; Butterfly creative-tab overlays cannot be positioned or focused correctly"
        );
    }

    @Unique
    private static void butterflyApi$warnMissingCreativeHandlerAccessor() {
        if (butterflyApi$warnedCreativeHandlerAccessor) {
            return;
        }
        butterflyApi$warnedCreativeHandlerAccessor = true;
        ButterflyApi.LOGGER.error(
                "CreativeScreenHandlerAccessor was not applied; Butterfly creative-tab categories cannot be constructed"
        );
    }

    @Unique
    private static void butterflyApi$warnMissingSearchManager() {
        if (butterflyApi$warnedSearchManager) {
            return;
        }
        butterflyApi$warnedSearchManager = true;
        ButterflyApi.LOGGER.warn(
                "The client search manager is not connected; Butterfly tab search will fall back to item names and registry ids"
        );
    }

    @Unique
    @Nullable
    private static TabProperties.Definition butterflyApi$getSelectedDefinition() {
        return butterflyApi$getDefinition(selectedTab);
    }

    @Unique
    @Nullable
    private static TabProperties.Definition butterflyApi$getDefinition(CreativeModeTab group) {
        ResourceKey<CreativeModeTab> key = butterflyApi$getTabKey(group);
        return key == null ? null : TabProperties.get(key);
    }

    @Unique
    @Nullable
    private static ResourceKey<CreativeModeTab> butterflyApi$getTabKey(CreativeModeTab group) {
        if (group == null) {
            return null;
        }
        Optional<ResourceKey<CreativeModeTab>> key = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(group);
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
//?}
