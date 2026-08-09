package moth.butterflyapi.content;

import moth.butterflyapi.ButterflyApi;
import moth.butterflyapi.plush.ButterflyPlushes;
import moth.butterflyapi.content.loot.ContentLootTableHooks;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import moth.butterflyapi.content.items.ModGroups;
import moth.butterflyapi.content.blocks.ModBlockEntities;
import moth.butterflyapi.content.blocks.ModBlocks;
import moth.butterflyapi.content.enchantment.ConnectionEnchantmentDeathHandler;
import moth.butterflyapi.content.enchantment.ModEnchantments;
import moth.butterflyapi.content.items.ModItems;
import moth.butterflyapi.content.sounds.ModSounds;
import moth.butterflyapi.content.paintings.ModPaintings;
import moth.butterflyapi.content.advancements.PlushCollectorAdvancement;

public final class ContentBootstrap implements ModInitializer {
    public static final String MOD_ID = ButterflyApi.MOD_ID;

    public static Identifier id(String path) {
        return ButterflyApi.id(path);
    }

    @Override
    public void onInitialize() {
        ModEnchantments.init();
        ConnectionEnchantmentDeathHandler.init();
        ContentLootTableHooks.init();
        ButterflyPlushes.bootstrap();
        ModSounds.init();
        ModBlocks.init();
        ModItems.init();
        ModBlockEntities.init();
        ModGroups.init();
        ModPaintings.init();
        PlushCollectorAdvancement.init();

        ButterflyApi.LOGGER.info("Initialized bundled Butterfly API content");
    }
}
