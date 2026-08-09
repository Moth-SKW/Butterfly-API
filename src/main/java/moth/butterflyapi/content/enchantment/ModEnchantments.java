package moth.butterflyapi.content.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;

public final class ModEnchantments {
    public static final RegistryKey<Enchantment> CONNECTION = ConnectionEnchantment.KEY;

    private ModEnchantments() {
    }

    public static void init() {
        // Enchantments are data-driven dynamic registry entries in 1.21.11.
    }
}
