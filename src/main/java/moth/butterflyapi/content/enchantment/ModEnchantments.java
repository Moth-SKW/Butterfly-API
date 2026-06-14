package moth.butterflyapi.content.enchantment;

import moth.butterflyapi.content.ContentBootstrap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEnchantments {
    private static boolean initialized = false;

    public static Enchantment CONNECTION;

    private ModEnchantments() {}

    public static void init() {
        if (initialized) return;
        initialized = true;

        CONNECTION = Registry.register(
                Registries.ENCHANTMENT,
                new Identifier(ContentBootstrap.MOD_ID, "connection"),
                new ConnectionEnchantment()
        );
    }
}
