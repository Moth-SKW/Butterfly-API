package moth.butterflyapi.content.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BigRedButtonClicksState extends PersistentState {

    private static final String DATA_NAME = "butterfly_api_big_red_button_clicks";
    private static final String KEY_CLICKS = "clicks";
    private static final String KEY_UUID = "uuid";
    private static final String KEY_COUNT = "count";

    private final Map<UUID, Long> clicks = new HashMap<>();

    public static BigRedButtonClicksState get(ServerWorld overworld) {
        return overworld.getPersistentStateManager().getOrCreate(
                BigRedButtonClicksState::fromNbt,
                BigRedButtonClicksState::new,
                DATA_NAME
        );
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

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (var e : clicks.entrySet()) {
            NbtCompound entry = new NbtCompound();
            entry.putUuid(KEY_UUID, e.getKey());
            entry.putLong(KEY_COUNT, e.getValue());
            list.add(entry);
        }
        nbt.put(KEY_CLICKS, list);
        return nbt;
    }

    public static BigRedButtonClicksState fromNbt(NbtCompound nbt) {
        BigRedButtonClicksState state = new BigRedButtonClicksState();
        if (nbt.contains(KEY_CLICKS)) {
            NbtList list = nbt.getList(KEY_CLICKS, net.minecraft.nbt.NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                NbtCompound entry = list.getCompound(i);
                if (!entry.containsUuid(KEY_UUID)) continue;
                UUID uuid = entry.getUuid(KEY_UUID);
                long count = entry.getLong(KEY_COUNT);
                state.clicks.put(uuid, count);
            }
        }
        return state;
    }
}
