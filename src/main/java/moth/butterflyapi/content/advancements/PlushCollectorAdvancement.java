package moth.butterflyapi.content.advancements;

import moth.butterflyapi.content.ContentBootstrap;
import moth.butterflyapi.content.blocks.Plushes;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PlushCollectorAdvancement {
    private static final int CHECK_EVERY_TICKS = 40;

    private static final Identifier ADV_ID = new Identifier(ContentBootstrap.MOD_ID, "collect_all_plushes");
    private static final String CRITERION = "impossible";

    private PlushCollectorAdvancement() {}

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(PlushCollectorAdvancement::onEndServerTick);
    }

    private static void onEndServerTick(MinecraftServer server) {
        if ((server.getTicks() % CHECK_EVERY_TICKS) != 0) return;

        Advancement adv = server.getAdvancementLoader().get(ADV_ID);
        if (adv == null) return;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            tryGrant(player, adv);
        }
    }

    private static void tryGrant(ServerPlayerEntity player, Advancement adv) {
        AdvancementProgress progress = player.getAdvancementTracker().getProgress(adv);
        if (progress.isDone()) return;

        if (!hasAllPlushes(player)) return;

        if (!progress.isDone()) {
            player.getAdvancementTracker().grantCriterion(adv, CRITERION);
        }
    }

    private static boolean hasAllPlushes(ServerPlayerEntity player) {
        List<String> plushIds = Plushes.all();
        if (plushIds.isEmpty()) return false;

        Set<Item> required = new HashSet<>();
        for (String id : plushIds) {
            required.add(Plushes.registered(id).item());
        }

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                required.remove(stack.getItem());
                if (required.isEmpty()) return true;
            }
        }

        return required.isEmpty();
    }
}
