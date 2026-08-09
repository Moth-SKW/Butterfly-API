package moth.butterflyapi.content.data;

import com.mojang.serialization.Codec;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ConnectionDeathsState extends PersistentState {
    private static final String DATA_NAME = "butterfly_api_connection_deaths";
    private static final Codec<ConnectionDeathsState> CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT)
            .xmap(ConnectionDeathsState::fromMap, ConnectionDeathsState::toMap);
    private static final PersistentStateType<ConnectionDeathsState> TYPE = new PersistentStateType<>(
            DATA_NAME,
            ConnectionDeathsState::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final Map<UUID, Integer> deathsByItem = new HashMap<>();

    public static ConnectionDeathsState get(MinecraftServer server) {
        return get(server.getOverworld());
    }

    public static ConnectionDeathsState get(ServerWorld overworld) {
        PersistentStateManager manager = overworld.getPersistentStateManager();
        return manager.getOrCreate(TYPE);
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

    private static ConnectionDeathsState fromMap(Map<String, Integer> values) {
        ConnectionDeathsState state = new ConnectionDeathsState();
        values.forEach((key, value) -> {
            try {
                state.deathsByItem.put(UUID.fromString(key), value);
            } catch (IllegalArgumentException ignored) {
            }
        });
        return state;
    }

    private static Map<String, Integer> toMap(ConnectionDeathsState state) {
        Map<String, Integer> values = new HashMap<>();
        state.deathsByItem.forEach((uuid, count) -> values.put(uuid.toString(), count));
        return values;
    }
}
