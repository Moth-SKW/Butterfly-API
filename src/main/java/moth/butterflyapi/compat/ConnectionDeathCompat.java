package moth.butterflyapi.content.compat;

import moth.butterflyapi.content.util.ConnectionEnchantmentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameRules;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class ConnectionDeathCompat {
    private static final Map<PlayerEntity, Map<Integer, ItemStack>> PRESERVED_STACKS =
            new WeakHashMap<>();

    private ConnectionDeathCompat() {
    }

    public static void extract(PlayerEntity player) {
        if (player.getWorld().isClient()
                || player.isSpectator()
                || player.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)
                || PRESERVED_STACKS.containsKey(player)) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        Map<Integer, ItemStack> preservedStacks = new HashMap<>();

        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);

            if (!ConnectionEnchantmentUtil.hasConnection(stack)) {
                continue;
            }

            ConnectionEnchantmentUtil.getOrCreateConnectionItemUuid(stack);

            preservedStacks.put(slot, stack);
            inventory.setStack(slot, ItemStack.EMPTY);
        }

        if (!preservedStacks.isEmpty()) {
            PRESERVED_STACKS.put(player, preservedStacks);
        }
    }

    public static Map<Integer, ItemStack> takePreservedStacks(PlayerEntity player) {
        Map<Integer, ItemStack> preservedStacks = PRESERVED_STACKS.remove(player);

        if (preservedStacks == null) {
            return Map.of();
        }

        return preservedStacks;
    }
}