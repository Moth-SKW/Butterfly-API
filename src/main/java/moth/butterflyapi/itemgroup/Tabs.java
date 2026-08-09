package moth.butterflyapi.itemgroup;

import moth.butterflyapi.mod.ModContext;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Tabs {
    private final ModContext context;

    public Tabs(ModContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public RegistryKey<ItemGroup> key(String path) {
        return RegistryKey.of(RegistryKeys.ITEM_GROUP, context.id(path));
    }

    public String translationKey(String path) {
        return "itemGroup." + context.modId() + "." + path;
    }

    public TabBuilder builder(String path) {
        return new TabBuilder(context, path);
    }

    public ItemGroup create(String path, ItemConvertible icon, ItemConvertible... entries) {
        return create(path, icon, translationKey(path), entries);
    }

    public ItemGroup create(String path, ItemConvertible icon, String translationKey, ItemConvertible... entries) {
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(translationKey, "translationKey");
        return builder(path)
                .icon(icon)
                .translationKey(translationKey)
                .add(entries)
                .build();
    }

    public ItemGroup create(String path, Supplier<ItemStack> icon, Text displayName, Consumer<ItemGroup.Entries> entries) {
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(entries, "entries");
        return builder(path)
                .icon(icon)
                .displayName(displayName)
                .entries(entries)
                .build();
    }

    public void addTo(RegistryKey<ItemGroup> group, ItemConvertible... entries) {
        addTo(group, Arrays.asList(entries));
    }

    public void addTo(RegistryKey<ItemGroup> group, Collection<? extends ItemConvertible> entries) {
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(entries, "entries");
        ItemGroupEvents.modifyEntriesEvent(group).register(itemGroupEntries -> addAll(itemGroupEntries, entries));
    }

    private static void addAll(ItemGroup.Entries itemGroupEntries, Collection<? extends ItemConvertible> entries) {
        for (ItemConvertible entry : entries) {
            itemGroupEntries.add(Objects.requireNonNull(entry, "entry"));
        }
    }
}
