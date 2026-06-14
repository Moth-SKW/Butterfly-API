package moth.butterflyapi.itemgroup;

import net.minecraft.item.ItemGroup;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

interface TabEntryOperation {
}

record TabEntriesOperation(
        Consumer<ItemGroup.Entries> consumer,
        boolean searchOnly,
        @Nullable TabSurface slotBackground
) implements TabEntryOperation {
    TabEntriesOperation {
        Objects.requireNonNull(consumer, "consumer");
    }

    TabEntriesOperation(Consumer<ItemGroup.Entries> consumer, boolean searchOnly) {
        this(consumer, searchOnly, null);
    }
}

enum TabEmptySlotOperation implements TabEntryOperation {
    INSTANCE
}

enum TabEmptyRowOperation implements TabEntryOperation {
    INSTANCE
}
