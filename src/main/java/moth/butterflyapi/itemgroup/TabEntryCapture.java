package moth.butterflyapi.itemgroup;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

final class TabEntryCapture {
    private TabEntryCapture() {
    }

    static List<TabLayout.Element> capture(
            List<TabEntryOperation> operations,
            CreativeModeTab.Output targetEntries
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

    private static CreativeModeTab.Output recordingEntries(
            CreativeModeTab.Output delegate,
            List<TabLayout.Element> captured,
            boolean forceSearchOnly,
            @Nullable TabSurface slotBackground
    ) {
        return new CreativeModeTab.Output() {
            @Override
            public void accept(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
                CreativeModeTab.TabVisibility effectiveVisibility = forceSearchOnly
                        ? CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY
                        : visibility;

                delegate.accept(stack, effectiveVisibility);
                captured.add(new TabLayout.StackElement(
                        stack,
                        effectiveVisibility == CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY,
                        slotBackground
                ));
            }
        };
    }
}
