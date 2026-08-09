package moth.butterflyapi.content.data;

import com.mojang.serialization.Codec;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BigRedButtonClicksState extends PersistentState {
    private static final String DATA_NAME = "butterfly_api_big_red_button_clicks";
    private static final Codec<BigRedButtonClicksState> CODEC = Codec.unboundedMap(Codec.STRING, Codec.LONG)
            .xmap(BigRedButtonClicksState::fromMap, BigRedButtonClicksState::toMap);
    private static final PersistentStateType<BigRedButtonClicksState> TYPE = new PersistentStateType<>(
            DATA_NAME,
            BigRedButtonClicksState::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final Map<UUID, Long> clicks = new HashMap<>();

    public static BigRedButtonClicksState get(ServerWorld overworld) {
        PersistentStateManager manager = overworld.getPersistentStateManager();
        return manager.getOrCreate(TYPE);
    }

    public long getClicks(UUID uuid) {
        return clicks.getOrDefault(uuid, 0L);
    }

    public long increment(UUID uuid) {
        long next = getClicks(uuid) + 1L;
        clicks.put(uuid, next);
        markDirty();
        return next;
    }

    private static BigRedButtonClicksState fromMap(Map<String, Long> values) {
        BigRedButtonClicksState state = new BigRedButtonClicksState();
        values.forEach((key, value) -> {
            try {
                state.clicks.put(UUID.fromString(key), value);
            } catch (IllegalArgumentException ignored) {
            }
        });
        return state;
    }

    private static Map<String, Long> toMap(BigRedButtonClicksState state) {
        Map<String, Long> values = new HashMap<>();
        state.clicks.forEach((uuid, count) -> values.put(uuid.toString(), count));
        return values;
    }
}
