package moth.butterflyapi.itemgroup;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

final class TabEntryCapture {
    private TabEntryCapture() {
    }

    static List<TabLayout.Element> capture(
            List<TabEntryOperation> operations,
            ItemGroup.Entries targetEntries
    ) {
        List<TabLayout.Element> elements = new ArrayList<>();
        for (TabEntryOperation operation : operations) {
            if (operation instanceof TabEntriesOperation entriesOperation) {
                entriesOperation.consumer().accept(recordingEntries(
                        targetEntries,
                        elements,
                        entriesOperation.searchOnly(),
                        entriesOperation.slotBackground()
                ));
            } else if (operation == TabEmptySlotOperation.INSTANCE) {
                elements.add(TabLayout.EmptySlot.INSTANCE);
            } else if (operation == TabEmptyRowOperation.INSTANCE) {
                elements.add(TabLayout.EmptyRow.INSTANCE);
            }
        }
        return List.copyOf(elements);
    }

    private static ItemGroup.Entries recordingEntries(
            ItemGroup.Entries delegate,
            List<TabLayout.Element> captured,
            boolean forceSearchOnly,
            @Nullable TabSurface slotBackground
    ) {
        return new ItemGroup.Entries() {
            @Override
            public void add(ItemStack stack, ItemGroup.StackVisibility visibility) {
                ItemGroup.StackVisibility effectiveVisibility = forceSearchOnly
                        ? ItemGroup.StackVisibility.SEARCH_TAB_ONLY
                        : visibility;

                delegate.add(stack, effectiveVisibility);
                captured.add(new TabLayout.StackElement(
                        stack,
                        effectiveVisibility == ItemGroup.StackVisibility.SEARCH_TAB_ONLY,
                        slotBackground
                ));
            }
        };
    }
}
