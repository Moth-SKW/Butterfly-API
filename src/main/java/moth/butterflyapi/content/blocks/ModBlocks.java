package moth.butterflyapi.content.blocks;

import moth.butterflyapi.content.ContentBootstrap;
import moth.butterflyapi.content.sounds.ModSounds;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.Collection;

public final class ModBlocks {
    private static boolean initialized = false;

    public static Block BIG_RED_BUTTON;
    public static MatchstickFireBlock MATCHSTICK_FIRE;
    public static MatchstickFireBlock MATCHSTICK_SOUL_FIRE;

    private ModBlocks() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        ModSounds.init();

        BIG_RED_BUTTON = registerSimple("big_red_button", new BigRedButtonBlock());
        MATCHSTICK_FIRE = (MatchstickFireBlock) registerSimple(
                "matchstick_fire",
                new MatchstickFireBlock(AbstractBlock.Settings.copy(Blocks.FIRE).registryKey(blockKey("matchstick_fire")), 1.0f, false)
        );
        MATCHSTICK_SOUL_FIRE = (MatchstickFireBlock) registerSimple(
                "matchstick_soul_fire",
                new MatchstickFireBlock(AbstractBlock.Settings.copy(Blocks.SOUL_FIRE).registryKey(blockKey("matchstick_soul_fire")), 2.0f, true)
        );
    }

    private static Block registerSimple(String idPath, Block block) {
        Identifier id = Identifier.of(ContentBootstrap.MOD_ID, idPath);
        return Registry.register(Registries.BLOCK, id, block);
    }

    private static RegistryKey<Block> blockKey(String idPath) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(ContentBootstrap.MOD_ID, idPath));
    }

    public static Block plush(String plushId) {
        return Plushes.registered(plushId).block();
    }

    public static Collection<Block> allPlushBlocks() {
        return Plushes.all().stream()
                .map(ModBlocks::plush)
                .toList();
    }

    public static Block[] allPlushBlocksArray() {
        return Plushes.all().stream()
                .map(ModBlocks::plush)
                .toArray(Block[]::new);
    }
}
