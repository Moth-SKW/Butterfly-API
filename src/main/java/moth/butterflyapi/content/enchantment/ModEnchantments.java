package moth.butterflyapi.content.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;

public final class ModEnchantments {
    public static final RegistryKey<Enchantment> CONNECTION = ConnectionEnchantment.KEY;

    private ModEnchantments() {
    }
}
