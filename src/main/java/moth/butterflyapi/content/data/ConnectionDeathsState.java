package moth.butterflyapi.content.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ConnectionDeathsState extends PersistentState {
    private static final String DATA_NAME = "butterfly_api_connection_deaths";
    private static final String KEY_ITEMS = "items";
    private static final String KEY_ITEM_UUID = "item_uuid";
    private static final String KEY_DEATHS = "deaths";

    private final Map<UUID, Integer> deathsByItem = new HashMap<>();

    public static ConnectionDeathsState get(MinecraftServer server) {
        return get(server.getOverworld());
    }

    public static ConnectionDeathsState get(ServerWorld overworld) {
        return overworld.getPersistentStateManager().getOrCreate(
                ConnectionDeathsState::fromNbt,
                ConnectionDeathsState::new,
                DATA_NAME
        );
    }

    public int getDeaths(UUID itemUuid) {
        return deathsByItem.getOrDefault(itemUuid, 0);
    }

    public int incrementDeaths(UUID itemUuid) {
        int next = getDeaths(itemUuid) + 1;
        deathsByItem.put(itemUuid, next);
        markDirty();
        return next;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList items = new NbtList();
        for (var entry : deathsByItem.entrySet()) {
            NbtCompound itemNbt = new NbtCompound();
            itemNbt.putUuid(KEY_ITEM_UUID, entry.getKey());
            itemNbt.putInt(KEY_DEATHS, entry.getValue());
            items.add(itemNbt);
        }

        nbt.put(KEY_ITEMS, items);
        return nbt;
    }

    public static ConnectionDeathsState fromNbt(NbtCompound nbt) {
        ConnectionDeathsState state = new ConnectionDeathsState();
        if (!nbt.contains(KEY_ITEMS, NbtElement.LIST_TYPE)) {
            return state;
        }

        NbtList items = nbt.getList(KEY_ITEMS, NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < items.size(); i++) {
            NbtCompound itemNbt = items.getCompound(i);
            if (!itemNbt.containsUuid(KEY_ITEM_UUID)) {
                continue;
            }

            state.deathsByItem.put(itemNbt.getUuid(KEY_ITEM_UUID), itemNbt.getInt(KEY_DEATHS));
        }

        return state;
    }
}
