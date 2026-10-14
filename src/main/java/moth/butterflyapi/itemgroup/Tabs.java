package moth.butterflyapi.itemgroup;

import moth.butterflyapi.mod.ModContext;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
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

    public ResourceKey<CreativeModeTab> key(String path) {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, context.id(path));
    }

    public String translationKey(String path) {
        return "itemGroup." + context.modId() + "." + path;
    }

    public TabBuilder builder(String path) {
        return new TabBuilder(context, path);
    }

    public CreativeModeTab create(String path, ItemLike icon, ItemLike... entries) {
        return create(path, icon, translationKey(path), entries);
    }

    public CreativeModeTab create(String path, ItemLike icon, String translationKey, ItemLike... entries) {
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(translationKey, "translationKey");
        return builder(path)
                .icon(icon)
                .translationKey(translationKey)
                .add(entries)
                .build();
    }

    public CreativeModeTab create(String path, Supplier<ItemStack> icon, Component displayName, Consumer<CreativeModeTab.Output> entries) {
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(entries, "entries");
        return builder(path)
                .icon(icon)
                .displayName(displayName)
                .entries(entries)
                .build();
    }

    public void addTo(ResourceKey<CreativeModeTab> group, ItemLike... entries) {
        addTo(group, Arrays.asList(entries));
    }

    public void addTo(ResourceKey<CreativeModeTab> group, Collection<? extends ItemLike> entries) {
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(entries, "entries");
        ItemGroupEvents.modifyEntriesEvent(group).register(itemGroupEntries -> addAll(itemGroupEntries, entries));
    }

    private static void addAll(CreativeModeTab.Output itemGroupEntries, Collection<? extends ItemLike> entries) {
        for (ItemLike entry : entries) {
            itemGroupEntries.accept(Objects.requireNonNull(entry, "entry"));
        }
    }
}
