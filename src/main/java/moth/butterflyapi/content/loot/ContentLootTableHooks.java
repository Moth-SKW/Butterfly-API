package moth.butterflyapi.content.loot;

import moth.butterflyapi.content.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;

public final class ContentLootTableHooks {
    private static final Identifier TRAIL_RUINS_COMMON = new Identifier("minecraft", "archaeology/trail_ruins_common");
    private static final Identifier TRAIL_RUINS_RARE = new Identifier("minecraft", "archaeology/trail_ruins_rare");

    private ContentLootTableHooks() {}

    public static void init() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (!source.isBuiltin()) {
                return;
            }

            if (TRAIL_RUINS_COMMON.equals(id)) {
                tableBuilder.pool(connectionBookPool(0.05f));
            } else if (TRAIL_RUINS_RARE.equals(id)) {
                tableBuilder.pool(connectionBookPool(0.14f));
            }
        });
    }

    private static LootPool.Builder connectionBookPool(float chance) {
        return LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1.0f))
                .conditionally(RandomChanceLootCondition.builder(chance))
                .with(ItemEntry.builder(Items.BOOK)
                        .apply(EnchantRandomlyLootFunction.create()
                                .add(ModEnchantments.CONNECTION)));
    }
}
