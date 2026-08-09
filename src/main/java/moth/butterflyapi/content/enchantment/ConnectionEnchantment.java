package moth.butterflyapi.content.enchantment;

import moth.butterflyapi.content.ContentBootstrap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/** Identifier for the data-driven Connection enchantment. */
public final class ConnectionEnchantment {
    public static final RegistryKey<Enchantment> KEY = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of(ContentBootstrap.MOD_ID, "connection")
    );

    private ConnectionEnchantment() {
    }
}
