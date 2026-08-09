package moth.butterflyapi.content.util;

import moth.butterflyapi.content.enchantment.ModEnchantments;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
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
        if (stack.isEmpty()) {
            return false;
        }
        ItemEnchantmentsComponent enchantments = stack.getEnchantments();
        return enchantments.getEnchantmentEntries().stream().anyMatch(entry ->
                entry.getKey().getKey().map(ModEnchantments.CONNECTION::equals).orElse(false)
                        && entry.getIntValue() > 0
        );
    }

    @Nullable
    public static UUID getConnectionOwnerUuid(ItemStack stack) {
        NbtCompound nbt = butterflyApi$customData(stack);
        if (nbt == null) {
            return null;
        }
        return butterflyApi$readUuid(nbt, CONNECTION_OWNER_UUID_KEY);
    }

    @Nullable
    public static UUID getConnectionItemUuid(ItemStack stack) {
        NbtCompound nbt = butterflyApi$customData(stack);
        if (nbt == null) {
            return null;
        }
        return butterflyApi$readUuid(nbt, CONNECTION_ITEM_UUID_KEY);
    }

    public static UUID getOrCreateConnectionItemUuid(ItemStack stack) {
        NbtCompound nbt = butterflyApi$customData(stack);
        if (nbt == null) {
            nbt = new NbtCompound();
        }
        UUID existing = butterflyApi$readUuid(nbt, CONNECTION_ITEM_UUID_KEY);
        if (existing != null) {
            return existing;
        }

        UUID itemUuid = UUID.randomUUID();
        nbt.putString(CONNECTION_ITEM_UUID_KEY, itemUuid.toString());
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt);
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

        NbtCompound nbt = butterflyApi$customData(stack);
        if (nbt == null) {
            nbt = new NbtCompound();
        }
        getOrCreateConnectionItemUuid(stack);
        nbt.putString(CONNECTION_OWNER_UUID_KEY, playerUuid.toString());
        nbt.putString(CONNECTION_OWNER_NAME_KEY, player.getName().getString());
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt);
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

    @Nullable
    private static NbtCompound butterflyApi$customData(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        return component == null ? null : component.copyNbt();
    }

    @Nullable
    private static UUID butterflyApi$readUuid(NbtCompound nbt, String key) {
        String value = nbt.getString(key, "");
        if (value.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
