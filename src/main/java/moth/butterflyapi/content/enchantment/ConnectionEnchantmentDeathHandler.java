package moth.butterflyapi.content.enchantment;

import moth.butterflyapi.content.compat.ConnectionDeathCompat;
import moth.butterflyapi.content.data.ConnectionDeathsState;
import moth.butterflyapi.content.util.ConnectionEnchantmentUtil;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;

import java.util.Map;
import java.util.UUID;

public final class ConnectionEnchantmentDeathHandler {
    private ConnectionEnchantmentDeathHandler() {
    }

    public static void init() {
        ServerPlayerEvents.AFTER_RESPAWN.register(
                ConnectionEnchantmentDeathHandler::afterRespawn
        );
    }

    private static void afterRespawn(
            ServerPlayerEntity oldPlayer,
            ServerPlayerEntity newPlayer,
            boolean alive
    ) {
        if (alive) {
            return;
        }

        MinecraftServer server = newPlayer.getServer();

        if (server == null) {
            return;
        }

        ConnectionDeathsState deathsState = ConnectionDeathsState.get(server);
        PlayerInventory newInventory = newPlayer.getInventory();

        Map<Integer, ItemStack> preservedStacks =
                ConnectionDeathCompat.takePreservedStacks(oldPlayer);

        if (!preservedStacks.isEmpty()) {
            for (Map.Entry<Integer, ItemStack> entry : preservedStacks.entrySet()) {
                int slot = entry.getKey();
                ItemStack stack = entry.getValue();

                UUID itemUuid =
                        ConnectionEnchantmentUtil.getOrCreateConnectionItemUuid(stack);

                int deaths = deathsState.incrementDeaths(itemUuid);

                if (deaths >= ConnectionEnchantmentUtil.MAX_CONNECTION_DEATHS) {
                    continue;
                }

                newInventory.setStack(slot, stack.copy());
            }

            return;
        }

        boolean keepInventory = oldPlayer
                .getServerWorld()
                .getGameRules()
                .getBoolean(GameRules.KEEP_INVENTORY);

        PlayerInventory oldInventory = oldPlayer.getInventory();

        for (int slot = 0; slot < oldInventory.size(); slot++) {
            ItemStack oldStack = oldInventory.getStack(slot);

            if (!ConnectionEnchantmentUtil.hasConnection(oldStack)) {
                continue;
            }

            UUID itemUuid =
                    ConnectionEnchantmentUtil.getOrCreateConnectionItemUuid(oldStack);

            int deaths = deathsState.incrementDeaths(itemUuid);

            if (deaths >= ConnectionEnchantmentUtil.MAX_CONNECTION_DEATHS) {
                if (keepInventory) {
                    newInventory.setStack(slot, ItemStack.EMPTY);
                }

                continue;
            }

            if (!keepInventory) {
                newInventory.setStack(slot, oldStack.copy());
            }
        }
    }
}