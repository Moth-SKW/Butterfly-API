package moth.butterflyapi.content.items;

import moth.butterflyapi.content.ContentBootstrap;
import moth.butterflyapi.content.blocks.ModBlocks;
import moth.butterflyapi.content.blocks.Plushes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModItems {
    private static boolean initialized = false;

    public static Item BIG_RED_BUTTON;
    public static Item MATCHSTICK;

    private ModItems() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        ModBlocks.init();

        BIG_RED_BUTTON = registerSimple("big_red_button", new BlockItem(ModBlocks.BIG_RED_BUTTON, itemSettings("big_red_button")));
        MATCHSTICK = registerSimple("matchstick", new MatchstickItem(itemSettings("matchstick").maxCount(16)));
    }

    private static Item registerSimple(String idPath, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(ContentBootstrap.MOD_ID, idPath), item);
    }

    private static Item.Settings itemSettings(String idPath) {
        return new Item.Settings().registryKey(RegistryKey.of(
                RegistryKeys.ITEM,
                Identifier.of(ContentBootstrap.MOD_ID, idPath)
        ));
    }

    public static Item plushItem(String plushId) {
        return Plushes.registered(plushId).item();
    }

    public static Map<String, Item> allPlushItems() {
        Map<String, Item> plushItems = new LinkedHashMap<>();
        for (String plushId : Plushes.all()) {
            plushItems.put(plushId, plushItem(plushId));
        }
        return Collections.unmodifiableMap(plushItems);
    }
}
