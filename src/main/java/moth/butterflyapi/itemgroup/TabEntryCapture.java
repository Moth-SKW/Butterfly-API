package moth.butterflyapi.itemgroup;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

final class TabEntryCapture {
    private static final String SEARCH_ONLY_VISIBILITY = "SEARCH_TAB_ONLY";

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
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return invoke(method, delegate, args);
            }
            if (method.isDefault()) {
                return InvocationHandler.invokeDefault(proxy, method, args);
            }
            if (isStackAdd(method, args)) {
                Object[] effectiveArgs = args.clone();
                if (forceSearchOnly) {
                    effectiveArgs[1] = searchOnlyVisibility(effectiveArgs[1]);
                }

                Object result = invoke(method, delegate, effectiveArgs);
                ItemStack stack = (ItemStack) effectiveArgs[0];
                captured.add(new TabLayout.StackElement(
                        stack,
                        isSearchOnlyVisibility(effectiveArgs[1]),
                        slotBackground
                ));
                return result;
            }
            return invoke(method, delegate, args);
        };

        return (ItemGroup.Entries) Proxy.newProxyInstance(
                ItemGroup.Entries.class.getClassLoader(),
                new Class<?>[]{ItemGroup.Entries.class},
                handler
        );
    }

    private static boolean isStackAdd(Method method, Object[] args) {
        return method.getName().equals("add")
                && args != null
                && args.length == 2
                && args[0] instanceof ItemStack;
    }

    private static Object searchOnlyVisibility(Object currentVisibility) {
        if (!(currentVisibility instanceof Enum<?> current)) {
            throw new IllegalStateException("Creative-tab visibility was not an enum");
        }
        for (Object constant : current.getDeclaringClass().getEnumConstants()) {
            if (constant instanceof Enum<?> enumConstant
                    && SEARCH_ONLY_VISIBILITY.equals(enumConstant.name())) {
                return constant;
            }
        }
        throw new IllegalStateException("Minecraft did not expose SEARCH_TAB_ONLY visibility");
    }

    private static boolean isSearchOnlyVisibility(Object visibility) {
        return visibility instanceof Enum<?> enumValue
                && SEARCH_ONLY_VISIBILITY.equals(enumValue.name());
    }

    private static Object invoke(Method method, Object target, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException exception) {
            throw exception.getCause();
        }
    }
}
