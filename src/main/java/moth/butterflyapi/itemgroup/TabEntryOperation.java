package moth.butterflyapi.itemgroup;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.world.item.CreativeModeTab;

interface TabEntryOperation {
}

record TabEntriesOperation(
        Consumer<CreativeModeTab.Output> consumer,
        boolean searchOnly,
        @Nullable TabSurface slotBackground
) implements TabEntryOperation {
    TabEntriesOperation {
        Objects.requireNonNull(consumer, "consumer");
    }

    TabEntriesOperation(Consumer<CreativeModeTab.Output> consumer, boolean searchOnly) {
        this(consumer, searchOnly, null);
    }
}

enum TabEmptySlotOperation implements TabEntryOperation {
    INSTANCE
}

enum TabEmptyRowOperation implements TabEntryOperation {
    INSTANCE
}
