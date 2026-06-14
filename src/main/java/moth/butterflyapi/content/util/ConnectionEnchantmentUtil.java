package moth.butterflyapi.content.util;

import moth.butterflyapi.content.enchantment.ModEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class ConnectionEnchantmentUtil {
    public static final int MAX_CONNECTION_DEATHS = 4;
    public static final String CONNECTION_ITEM_UUID_KEY = "ButterflyApiConnectionItemUuid";
    public static final String CONNECTION_OWNER_UUID_KEY = "ButterflyApiConnectionOwnerUuid";
    public static final String CONNECTION_OWNER_NAME_KEY = "ButterflyApiConnectionOwnerName";

    private ConnectionEnchantmentUtil() {}

    public static boolean hasConnection(ItemStack stack) {
        return !stack.isEmpty()
                && ModEnchantments.CONNECTION != null
                && EnchantmentHelper.getLevel(ModEnchantments.CONNECTION, stack) > 0;
    }

    @Nullable
    public static UUID getConnectionOwnerUuid(ItemStack stack) {
        if (!stack.hasNbt()) {
            return null;
        }
        NbtCompound nbt = stack.getNbt();
        return nbt.containsUuid(CONNECTION_OWNER_UUID_KEY) ? nbt.getUuid(CONNECTION_OWNER_UUID_KEY) : null;
    }

    @Nullable
    public static UUID getConnectionItemUuid(ItemStack stack) {
        if (!stack.hasNbt()) {
            return null;
        }
        NbtCompound nbt = stack.getNbt();
        return nbt.containsUuid(CONNECTION_ITEM_UUID_KEY) ? nbt.getUuid(CONNECTION_ITEM_UUID_KEY) : null;
    }

    public static UUID getOrCreateConnectionItemUuid(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (nbt.containsUuid(CONNECTION_ITEM_UUID_KEY)) {
            return nbt.getUuid(CONNECTION_ITEM_UUID_KEY);
        }

        UUID itemUuid = UUID.randomUUID();
        nbt.putUuid(CONNECTION_ITEM_UUID_KEY, itemUuid);
        return itemUuid;
    }

    public static void bindConnectionOwnerIfMissingOrSelf(ItemStack stack, @Nullable PlayerEntity player) {
        if (player == null || !hasConnection(stack)) {
            return;
        }

        UUID playerUuid = player.getUuid();
        UUID existingOwner = getConnectionOwnerUuid(stack);
        if (existingOwner != null && !existingOwner.equals(playerUuid)) {
            return;
        }

        NbtCompound nbt = stack.getOrCreateNbt();
        getOrCreateConnectionItemUuid(stack);
        nbt.putUuid(CONNECTION_OWNER_UUID_KEY, playerUuid);
        nbt.putString(CONNECTION_OWNER_NAME_KEY, player.getName().getString());
    }

    public static boolean canPlayerPickup(ItemStack stack, @Nullable PlayerEntity player) {
        if (!hasConnection(stack)) {
            return true;
        }

        UUID ownerUuid = getConnectionOwnerUuid(stack);
        if (ownerUuid == null) {
            return true;
        }

        return player != null && ownerUuid.equals(player.getUuid());
    }
}
