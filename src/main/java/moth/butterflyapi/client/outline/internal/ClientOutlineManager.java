package moth.butterflyapi.client.outline.internal;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import moth.butterflyapi.client.outline.OutlineRenderBridge;
import moth.butterflyapi.outline.OutlineStyle;
import moth.butterflyapi.outline.internal.network.BlockEntityOutlineUpdateS2CPacket;
import moth.butterflyapi.outline.internal.network.EntityOutlineUpdateS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public final class ClientOutlineManager {
    private static final long PENDING_LIFETIME_TICKS = 200L;

    private static final Map<UUID, OutlineStyle> LOCAL_ENTITY_STYLES = new HashMap<>();
    private static final Map<UUID, OutlineStyle> SYNCED_ENTITY_STYLES = new HashMap<>();
    private static final Map<UUID, PendingEntityStyle> PENDING_ENTITY_STYLES = new HashMap<>();

    private static final Long2ObjectMap<OutlineStyle> LOCAL_BLOCK_ENTITY_STYLES = new Long2ObjectOpenHashMap<>();
    private static final Long2ObjectMap<OutlineStyle> SYNCED_BLOCK_ENTITY_STYLES = new Long2ObjectOpenHashMap<>();
    private static final Long2ObjectMap<PendingBlockEntityStyle> PENDING_BLOCK_ENTITY_STYLES =
            new Long2ObjectOpenHashMap<>();

    @Nullable
    private static ClientWorld activeWorld;
    @Nullable
    private static Identifier activeDimension;
    private static long clientTicks;

    private ClientOutlineManager() {
    }

    public static void setLocal(Entity target, OutlineStyle style) {
        ClientWorld world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        LOCAL_ENTITY_STYLES.put(target.getUuid(), Objects.requireNonNull(style, "style"));
    }

    public static void setLocal(BlockEntity target, OutlineStyle style) {
        ClientWorld world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        LOCAL_BLOCK_ENTITY_STYLES.put(target.getPos().asLong(), Objects.requireNonNull(style, "style"));
    }

    public static void clearLocal(Entity target) {
        ClientWorld world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        LOCAL_ENTITY_STYLES.remove(target.getUuid());
    }

    public static void clearLocal(BlockEntity target) {
        ClientWorld world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        LOCAL_BLOCK_ENTITY_STYLES.remove(target.getPos().asLong());
    }

    public static void clearAllLocal() {
        requireClientThread();
        observeWorld(MinecraftClient.getInstance().world);
        LOCAL_ENTITY_STYLES.clear();
        LOCAL_BLOCK_ENTITY_STYLES.clear();
    }

    @Nullable
    public static OutlineStyle getLocal(Entity target) {
        ClientWorld world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        return LOCAL_ENTITY_STYLES.get(target.getUuid());
    }

    @Nullable
    public static OutlineStyle getLocal(BlockEntity target) {
        ClientWorld world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        return LOCAL_BLOCK_ENTITY_STYLES.get(target.getPos().asLong());
    }

    @Nullable
    public static OutlineStyle getEffective(Entity target) {
        ClientWorld world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        return resolve(target);
    }

    @Nullable
    public static OutlineStyle getEffective(BlockEntity target) {
        ClientWorld world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        return resolve(target);
    }

    @Nullable
    public static OutlineStyle resolve(Entity target) {
        if (target == null || target.isRemoved() || target.getEntityWorld() != activeWorld) {
            return null;
        }

        OutlineStyle local = LOCAL_ENTITY_STYLES.get(target.getUuid());
        return local != null ? local : SYNCED_ENTITY_STYLES.get(target.getUuid());
    }

    @Nullable
    public static OutlineStyle resolve(BlockEntity target) {
        if (target == null || target.isRemoved() || target.getWorld() != activeWorld) {
            return null;
        }

        long pos = target.getPos().asLong();
        OutlineStyle local = LOCAL_BLOCK_ENTITY_STYLES.get(pos);
        return local != null ? local : SYNCED_BLOCK_ENTITY_STYLES.get(pos);
    }

    public static boolean hasAnyCustomOutlines() {
        return !LOCAL_ENTITY_STYLES.isEmpty()
                || !SYNCED_ENTITY_STYLES.isEmpty()
                || !LOCAL_BLOCK_ENTITY_STYLES.isEmpty()
                || !SYNCED_BLOCK_ENTITY_STYLES.isEmpty();
    }

    public static void accept(EntityOutlineUpdateS2CPacket packet) {
        Objects.requireNonNull(packet, "packet");
        requireClientThread();
        ClientWorld world = currentWorldForPacket(packet.dimension());
        if (world == null) {
            return;
        }

        UUID uuid = packet.entityUuid();
        OutlineStyle style = packet.style();
        if (style == null) {
            SYNCED_ENTITY_STYLES.remove(uuid);
            PENDING_ENTITY_STYLES.remove(uuid);
            return;
        }

        Entity entity = world.getEntityById(packet.entityId());
        if (entity != null && uuid.equals(entity.getUuid())) {
            SYNCED_ENTITY_STYLES.put(uuid, style);
            PENDING_ENTITY_STYLES.remove(uuid);
            return;
        }

        SYNCED_ENTITY_STYLES.remove(uuid);
        PENDING_ENTITY_STYLES.put(
                uuid,
                new PendingEntityStyle(packet.entityId(), style, clientTicks + PENDING_LIFETIME_TICKS)
        );
    }

    public static void accept(BlockEntityOutlineUpdateS2CPacket packet) {
        Objects.requireNonNull(packet, "packet");
        requireClientThread();
        ClientWorld world = currentWorldForPacket(packet.dimension());
        if (world == null) {
            return;
        }

        long pos = packet.pos().asLong();
        OutlineStyle style = packet.style();
        if (style == null) {
            SYNCED_BLOCK_ENTITY_STYLES.remove(pos);
            PENDING_BLOCK_ENTITY_STYLES.remove(pos);
            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(packet.pos());
        if (blockEntity != null && !blockEntity.isRemoved()) {
            SYNCED_BLOCK_ENTITY_STYLES.put(pos, style);
            PENDING_BLOCK_ENTITY_STYLES.remove(pos);
            return;
        }

        SYNCED_BLOCK_ENTITY_STYLES.remove(pos);
        PENDING_BLOCK_ENTITY_STYLES.put(
                pos,
                new PendingBlockEntityStyle(style)
        );
    }

    static void onEntityLoad(Entity entity, ClientWorld world) {
        if (!activateEventWorld(world)) {
            return;
        }

        UUID uuid = entity.getUuid();
        PendingEntityStyle pending = PENDING_ENTITY_STYLES.remove(uuid);
        if (pending != null
                && pending.expiresAtTick() >= clientTicks
                && pending.entityId() == entity.getId()) {
            SYNCED_ENTITY_STYLES.put(uuid, pending.style());
        }
    }

    static void onEntityUnload(Entity entity, ClientWorld world) {
        if (world != activeWorld) {
            return;
        }

        UUID uuid = entity.getUuid();
        LOCAL_ENTITY_STYLES.remove(uuid);
        SYNCED_ENTITY_STYLES.remove(uuid);
        PENDING_ENTITY_STYLES.remove(uuid);
        OutlineRenderBridge.clearAdaptiveThickness(entity);
    }

    static void onBlockEntityLoad(BlockEntity blockEntity, ClientWorld world) {
        if (!activateEventWorld(world)) {
            return;
        }

        long pos = blockEntity.getPos().asLong();
        PendingBlockEntityStyle pending = PENDING_BLOCK_ENTITY_STYLES.remove(pos);
        if (pending != null) {
            SYNCED_BLOCK_ENTITY_STYLES.put(pos, pending.style());
        }
    }

    static void onBlockEntityUnload(BlockEntity blockEntity, ClientWorld world) {
        if (world != activeWorld) {
            return;
        }

        long pos = blockEntity.getPos().asLong();
        LOCAL_BLOCK_ENTITY_STYLES.remove(pos);
        OutlineStyle synced = SYNCED_BLOCK_ENTITY_STYLES.remove(pos);
        if (synced != null) {
            PENDING_BLOCK_ENTITY_STYLES.put(pos, new PendingBlockEntityStyle(synced));
        }
        OutlineRenderBridge.clearAdaptiveThickness(blockEntity);
    }

    static void onJoin(@Nullable ClientWorld world) {
        requireClientThread();
        resetWorld(world);
    }

    static void onDisconnect() {
        requireClientThread();
        resetWorld(null);
    }

    static void onEndClientTick(MinecraftClient client) {
        requireClientThread();
        observeWorld(client.world);
        clientTicks++;
        if ((clientTicks % 20L) == 0L) {
            removeExpiredPendingStyles();
        }
    }

    private static ClientWorld requireCurrentWorld() {
        requireClientThread();
        MinecraftClient client = MinecraftClient.getInstance();
        observeWorld(client.world);
        if (activeWorld == null) {
            throw new IllegalStateException("Client outlines require an active client world");
        }
        return activeWorld;
    }

    private static void requireCurrentTarget(Entity target, ClientWorld world) {
        Objects.requireNonNull(target, "target");
        if (target.getEntityWorld() != world || target.isRemoved()) {
            throw new IllegalArgumentException("Entity target must be loaded in the current client world");
        }
    }

    private static void requireCurrentTarget(BlockEntity target, ClientWorld world) {
        Objects.requireNonNull(target, "target");
        if (target.getWorld() != world || target.isRemoved()) {
            throw new IllegalArgumentException("Block entity target must be loaded in the current client world");
        }
    }

    @Nullable
    private static ClientWorld currentWorldForPacket(Identifier dimension) {
        MinecraftClient client = MinecraftClient.getInstance();
        observeWorld(client.world);
        if (activeWorld == null || !Objects.equals(activeDimension, dimension)) {
            return null;
        }
        return activeWorld;
    }

    private static boolean activateEventWorld(ClientWorld world) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (world != client.world) {
            return false;
        }
        observeWorld(world);
        return true;
    }

    private static void observeWorld(@Nullable ClientWorld world) {
        if (activeWorld != world) {
            resetWorld(world);
        }
    }

    private static void resetWorld(@Nullable ClientWorld world) {
        clearAllState();
        OutlineRenderBridge.clearAdaptiveThickness();
        activeWorld = world;
        activeDimension = world == null ? null : world.getRegistryKey().getValue();
    }

    private static void clearAllState() {
        LOCAL_ENTITY_STYLES.clear();
        SYNCED_ENTITY_STYLES.clear();
        PENDING_ENTITY_STYLES.clear();
        LOCAL_BLOCK_ENTITY_STYLES.clear();
        SYNCED_BLOCK_ENTITY_STYLES.clear();
        PENDING_BLOCK_ENTITY_STYLES.clear();
    }

    private static void removeExpiredPendingStyles() {
        Iterator<PendingEntityStyle> entityStyles = PENDING_ENTITY_STYLES.values().iterator();
        while (entityStyles.hasNext()) {
            if (entityStyles.next().expiresAtTick() < clientTicks) {
                entityStyles.remove();
            }
        }
    }

    private static void requireClientThread() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.isOnThread()) {
            throw new IllegalStateException("Client outline state must be accessed from the render thread");
        }
    }

    private record PendingEntityStyle(int entityId, OutlineStyle style, long expiresAtTick) {
    }

    private record PendingBlockEntityStyle(OutlineStyle style) {
    }
}
