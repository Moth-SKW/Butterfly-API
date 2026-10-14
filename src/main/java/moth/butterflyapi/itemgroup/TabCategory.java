package moth.butterflyapi.itemgroup;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class TabCategory {
    private final String id;
    private final Component text;
    private final TabSurface background;
    @Nullable
    private final Integer borderColor;
    private final int textColor;
    private final TextAlignment textAlignment;
    private final List<TabEntryOperation> operations;

    private TabCategory(Builder builder) {
        this.id = builder.id;
        this.text = builder.text;
        this.background = builder.background;
        this.borderColor = builder.borderColor;
        this.textColor = builder.textColor;
        this.textAlignment = builder.textAlignment;
        this.operations = List.copyOf(builder.operations);
    }

    static Builder builder(String id, Component text) {
        return new Builder(id, text);
    }

    public String id() {
        return id;
    }

    public Component label() {
        return text;
    }

    public TabSurface background() {
        return background;
    }

    @Nullable
    public Integer borderColor() {
        return borderColor;
    }

    public int textColor() {
        return textColor;
    }

    public TextAlignment textAlignment() {
        return textAlignment;
    }

    List<TabEntryOperation> operations() {
        return operations;
    }

    public enum TextAlignment {
        LEFT,
        CENTER,
        RIGHT
    }

    public static final class Builder {
        private final String id;
        private final Component text;
        private final List<TabEntryOperation> operations = new ArrayList<>();
        private TabSurface background = TabSurface.color("#404040");
        @Nullable
        private Integer borderColor;
        private int textColor = HexColor.parse("#FFFFFF");
        private TextAlignment textAlignment = TextAlignment.CENTER;

        private Builder(String id, Component text) {
            this.id = requireId(id);
            this.text = Objects.requireNonNull(text, "text");
        }

        public Builder backgroundColor(String hexColor) {
            background = TabSurface.color(hexColor);
            return this;
        }

        public Builder backgroundTexture(Identifier texture) {
            background = TabSurface.texture(texture);
            return this;
        }

        public Builder borderColor(String hexColor) {
            borderColor = HexColor.parse(hexColor);
            return this;
        }

        public Builder noBorder() {
            borderColor = null;
            return this;
        }

        public Builder textColor(String hexColor) {
            textColor = HexColor.parse(hexColor);
            return this;
        }

        public Builder textAlignment(TextAlignment alignment) {
            textAlignment = Objects.requireNonNull(alignment, "alignment");
            return this;
        }

        public Builder alignLeft() {
            return textAlignment(TextAlignment.LEFT);
        }

        public Builder alignCenter() {
            return textAlignment(TextAlignment.CENTER);
        }

        public Builder alignRight() {
            return textAlignment(TextAlignment.RIGHT);
        }

        public Builder entries(Consumer<CreativeModeTab.Output> entries) {
            operations.add(new TabEntriesOperation(
                    Objects.requireNonNull(entries, "entries"),
                    false
            ));
            return this;
        }

        public Builder searchOnlyEntries(Consumer<CreativeModeTab.Output> entries) {
            operations.add(new TabEntriesOperation(
                    Objects.requireNonNull(entries, "entries"),
                    true
            ));
            return this;
        }

        public Builder add(ItemLike... entries) {
            Objects.requireNonNull(entries, "entries");
            return entries(groupEntries -> addItems(groupEntries, entries));
        }

        public Builder addStacks(ItemStack... stacks) {
            Objects.requireNonNull(stacks, "stacks");
            return entries(groupEntries -> addStacks(groupEntries, stacks));
        }

        public Builder entry(ItemLike entry, Consumer<TabEntryStyle.Builder> appearance) {
            Objects.requireNonNull(entry, "entry");
            return styledEntry(new ItemStack(entry), false, appearance);
        }

        public Builder entry(ItemStack stack, Consumer<TabEntryStyle.Builder> appearance) {
            return styledEntry(Objects.requireNonNull(stack, "stack"), false, appearance);
        }

        public Builder searchOnly(ItemLike... entries) {
            Objects.requireNonNull(entries, "entries");
            return searchOnlyEntries(groupEntries -> addItems(groupEntries, entries));
        }

        public Builder searchOnlyStacks(ItemStack... stacks) {
            Objects.requireNonNull(stacks, "stacks");
            return searchOnlyEntries(groupEntries -> addStacks(groupEntries, stacks));
        }

        public Builder searchOnlyEntry(
                ItemLike entry,
                Consumer<TabEntryStyle.Builder> appearance
        ) {
            Objects.requireNonNull(entry, "entry");
            return styledEntry(new ItemStack(entry), true, appearance);
        }

        public Builder searchOnlyEntry(
                ItemStack stack,
                Consumer<TabEntryStyle.Builder> appearance
        ) {
            return styledEntry(Objects.requireNonNull(stack, "stack"), true, appearance);
        }

        public Builder emptySlot() {
            operations.add(TabEmptySlotOperation.INSTANCE);
            return this;
        }

        public Builder emptyRow() {
            operations.add(TabEmptyRowOperation.INSTANCE);
            return this;
        }

        private Builder styledEntry(
                ItemStack stack,
                boolean searchOnly,
                Consumer<TabEntryStyle.Builder> appearance
        ) {
            ItemStack capturedStack = Objects.requireNonNull(stack, "stack").copy();
            TabEntryStyle style = TabEntryStyle.configure(appearance);
            operations.add(new TabEntriesOperation(
                    entries -> entries.accept(capturedStack.copy()),
                    searchOnly,
                    style.slotBackground()
            ));
            return this;
        }

        TabCategory build() {
            return new TabCategory(this);
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

        private static String requireId(String id) {
            Objects.requireNonNull(id, "id");
            if (id.isBlank()) {
                throw new IllegalArgumentException("Category id cannot be blank");
            }
            return id;
        }
    }
}
