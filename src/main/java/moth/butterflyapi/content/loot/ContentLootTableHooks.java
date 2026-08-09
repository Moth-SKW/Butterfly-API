package moth.butterflyapi.content.loot;

import moth.butterflyapi.content.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class ContentLootTableHooks {
    private static final RegistryKey<LootTable> TRAIL_RUINS_COMMON = RegistryKey.of(
            RegistryKeys.LOOT_TABLE,
            Identifier.of("minecraft", "archaeology/trail_ruins_common")
    );
    private static final RegistryKey<LootTable> TRAIL_RUINS_RARE = RegistryKey.of(
            RegistryKeys.LOOT_TABLE,
            Identifier.of("minecraft", "archaeology/trail_ruins_rare")
    );

    private ContentLootTableHooks() {}

    public static void init() {
        LootTableEvents.MODIFY.register((id, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) {
                return;
            }

            if (TRAIL_RUINS_COMMON.equals(id)) {
                tableBuilder.pool(connectionBookPool(0.05f, registries));
            } else if (TRAIL_RUINS_RARE.equals(id)) {
                tableBuilder.pool(connectionBookPool(0.14f, registries));
            }
        });
    }

    private static LootPool.Builder connectionBookPool(float chance, RegistryWrapper.WrapperLookup registries) {
        var connection = registries.getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(ModEnchantments.CONNECTION);
        return LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1.0f))
                .conditionally(RandomChanceLootCondition.builder(chance))
                .with(ItemEntry.builder(Items.BOOK)
                        .apply(EnchantRandomlyLootFunction.create().option(connection)));
    }
}
