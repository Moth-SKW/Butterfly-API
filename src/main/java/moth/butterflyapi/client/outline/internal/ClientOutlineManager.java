package moth.butterflyapi.client.outline.internal;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import moth.butterflyapi.client.outline.OutlineRenderBridge;
import moth.butterflyapi.outline.ItemOutlineTarget;
import moth.butterflyapi.outline.EntityOutline;
import moth.butterflyapi.outline.EntityOutlinePart;
import moth.butterflyapi.outline.OutlineOnlyBlockEntity;
import moth.butterflyapi.outline.OutlineStyle;
import moth.butterflyapi.outline.internal.network.BlockEntityOutlineUpdateS2CPacket;
import moth.butterflyapi.outline.internal.network.EntityOutlineUpdateS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public final class ClientOutlineManager {
    private static final long PENDING_LIFETIME_TICKS = 200L;

    private static final Map<UUID, EntityOutline> LOCAL_ENTITY_OUTLINES = new HashMap<>();
    private static final Map<UUID, EntityOutline> SYNCED_ENTITY_OUTLINES = new HashMap<>();
    private static final Map<UUID, PendingEntityOutline> PENDING_ENTITY_OUTLINES = new HashMap<>();
    private static final Map<Object, RenderOutlineState> RENDER_STATE_OUTLINES = new IdentityHashMap<>();

    private static final Long2ObjectMap<OutlineStyle> LOCAL_BLOCK_ENTITY_STYLES = new Long2ObjectOpenHashMap<>();
    private static final Long2ObjectMap<OutlineStyle> SYNCED_BLOCK_ENTITY_STYLES = new Long2ObjectOpenHashMap<>();
    private static final Long2ObjectMap<PendingBlockEntityStyle> PENDING_BLOCK_ENTITY_STYLES =
            new Long2ObjectOpenHashMap<>();
    private static final LongOpenHashSet OUTLINE_ONLY_BLOCK_ENTITY_POSITIONS = new LongOpenHashSet();

    @Nullable
    private static ClientLevel activeWorld;
    @Nullable
    private static Identifier activeDimension;
    private static long clientTicks;

    private ClientOutlineManager() {
    }

    public static void setLocal(Entity target, OutlineStyle style) {
        setLocal(target, EntityOutline.attached(style));
    }

    public static void setLocal(Entity target, EntityOutline outline) {
        ClientLevel world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        LOCAL_ENTITY_OUTLINES.put(target.getUUID(), Objects.requireNonNull(outline, "outline"));
    }

    public static void setLocalPart(Entity target, EntityOutlinePart part, OutlineStyle style) {
        ClientLevel world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        EntityOutline current = LOCAL_ENTITY_OUTLINES.getOrDefault(target.getUUID(), EntityOutline.empty());
        LOCAL_ENTITY_OUTLINES.put(target.getUUID(), current.with(part, style));
    }

    public static void setLocal(BlockEntity target, OutlineStyle style) {
        ClientLevel world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        LOCAL_BLOCK_ENTITY_STYLES.put(target.getBlockPos().asLong(), Objects.requireNonNull(style, "style"));
        refreshOutlineOnlyBlockEntity(target);
    }

    public static void clearLocal(Entity target) {
        ClientLevel world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        LOCAL_ENTITY_OUTLINES.remove(target.getUUID());
    }

    public static void clearLocalPart(Entity target, EntityOutlinePart part) {
        ClientLevel world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        EntityOutline current = LOCAL_ENTITY_OUTLINES.get(target.getUUID());
        if (current == null) {
            return;
        }
        EntityOutline updated = current.without(part);
        if (updated.isEmpty()) {
            LOCAL_ENTITY_OUTLINES.remove(target.getUUID());
        } else {
            LOCAL_ENTITY_OUTLINES.put(target.getUUID(), updated);
        }
    }

    public static void clearLocal(BlockEntity target) {
        ClientLevel world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        LOCAL_BLOCK_ENTITY_STYLES.remove(target.getBlockPos().asLong());
        refreshOutlineOnlyBlockEntity(target);
    }

    public static void clearAllLocal() {
        requireClientThread();
        observeWorld(Minecraft.getInstance().level);
        LOCAL_ENTITY_OUTLINES.clear();
        LOCAL_BLOCK_ENTITY_STYLES.clear();
        discardLocalOutlineOnlyBlockEntityPositions();
    }

    @Nullable
    public static OutlineStyle getLocal(Entity target) {
        ClientLevel world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        return primary(LOCAL_ENTITY_OUTLINES.get(target.getUUID()));
    }

    @Nullable
    public static OutlineStyle getLocal(BlockEntity target) {
        ClientLevel world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        return LOCAL_BLOCK_ENTITY_STYLES.get(target.getBlockPos().asLong());
    }

    @Nullable
    public static OutlineStyle getEffective(Entity target) {
        ClientLevel world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        return resolve(target);
    }

    @Nullable
    public static EntityOutline getLocalOutline(Entity target) {
        ClientLevel world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        return LOCAL_ENTITY_OUTLINES.get(target.getUUID());
    }

    @Nullable
    public static EntityOutline getEffectiveOutline(Entity target) {
        ClientLevel world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        return resolveOutline(target);
    }

    @Nullable
    public static OutlineStyle getEffective(BlockEntity target) {
        ClientLevel world = requireCurrentWorld();
        requireCurrentTarget(target, world);
        return resolve(target);
    }

    @Nullable
    public static OutlineStyle resolve(Entity target) {
        return primary(resolveOutline(target));
    }

    @Nullable
    public static EntityOutline resolveOutline(Entity target) {
        if (target == null || target.isRemoved() || target.level() != activeWorld) {
            return null;
        }

        EntityOutline local = LOCAL_ENTITY_OUTLINES.get(target.getUUID());
        if (local != null) {
            return local;
        }
        EntityOutline synced = SYNCED_ENTITY_OUTLINES.get(target.getUUID());
        return synced != null ? synced : itemOutline(target);
    }

    public static void bindRenderState(Object state, Entity entity, @Nullable EntityOutline outline) {
        if (outline == null) {
            RENDER_STATE_OUTLINES.remove(state);
        } else {
            RENDER_STATE_OUTLINES.put(state, new RenderOutlineState(entity, outline));
        }
    }

    @Nullable
    public static OutlineStyle resolveRenderPart(Object state, EntityOutlinePart part) {
        RenderOutlineState renderState = RENDER_STATE_OUTLINES.get(state);
        return renderState == null ? null : renderState.outline().style(part).orElse(null);
    }

    public static int resolveRenderPartColor(Object state, EntityOutlinePart part, int fallbackColor) {
        RenderOutlineState renderState = RENDER_STATE_OUTLINES.get(state);
        if (renderState == null) {
            return fallbackColor;
        }
        OutlineStyle style = renderState.outline().style(part).orElse(null);
        if (style == null) {
            return 0;
        }
        Vec3 camera;
//? if <1.21.11 {
/*
        camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
*///?} else {
        camera = Minecraft.getInstance().gameRenderer
//? if >=26.2 {
/*
                .mainCamera()
*///?} else {
                .getMainCamera()
//?}
                .position();
//?}
        return OutlineRenderBridge.encodedColor(renderState.entity(), style, camera);
    }

    public static boolean hasRenderOutline(Object state) {
        return RENDER_STATE_OUTLINES.containsKey(state);
    }

    @Nullable
    public static OutlineStyle resolve(BlockEntity target) {
        if (target == null || target.isRemoved() || target.getLevel() != activeWorld) {
            return null;
        }

        long pos = target.getBlockPos().asLong();
        OutlineStyle local = LOCAL_BLOCK_ENTITY_STYLES.get(pos);
        return local != null ? local : SYNCED_BLOCK_ENTITY_STYLES.get(pos);
    }

    public static boolean hasAnyCustomOutlines() {
        return !LOCAL_ENTITY_OUTLINES.isEmpty()
                || !SYNCED_ENTITY_OUTLINES.isEmpty()
                || !LOCAL_BLOCK_ENTITY_STYLES.isEmpty()
                || !SYNCED_BLOCK_ENTITY_STYLES.isEmpty();
    }

    public static boolean hasOutlineOnlyBlockEntityOutline(BlockPos position) {
        return OUTLINE_ONLY_BLOCK_ENTITY_POSITIONS.contains(Objects.requireNonNull(position, "position").asLong());
    }

    static Long2ObjectMap<OutlineStyle> localBlockEntityStyles() {
        return LOCAL_BLOCK_ENTITY_STYLES;
    }

    static Long2ObjectMap<OutlineStyle> syncedBlockEntityStyles() {
        return SYNCED_BLOCK_ENTITY_STYLES;
    }

    public static void accept(EntityOutlineUpdateS2CPacket packet) {
        Objects.requireNonNull(packet, "packet");
        requireClientThread();
        ClientLevel world = currentWorldForPacket(packet.dimension());
        if (world == null) {
            return;
        }

        UUID uuid = packet.entityUuid();
        EntityOutline outline = packet.outline();
        if (outline == null) {
            SYNCED_ENTITY_OUTLINES.remove(uuid);
            PENDING_ENTITY_OUTLINES.remove(uuid);
            return;
        }

        Entity entity = world.getEntity(packet.entityId());
        if (entity != null && uuid.equals(entity.getUUID())) {
            SYNCED_ENTITY_OUTLINES.put(uuid, outline);
            PENDING_ENTITY_OUTLINES.remove(uuid);
            return;
        }

        SYNCED_ENTITY_OUTLINES.remove(uuid);
        PENDING_ENTITY_OUTLINES.put(
                uuid,
                new PendingEntityOutline(packet.entityId(), outline, clientTicks + PENDING_LIFETIME_TICKS)
        );
    }

    public static void accept(BlockEntityOutlineUpdateS2CPacket packet) {
        Objects.requireNonNull(packet, "packet");
        requireClientThread();
        ClientLevel world = currentWorldForPacket(packet.dimension());
        if (world == null) {
            return;
        }

        long pos = packet.pos().asLong();
        OutlineStyle style = packet.style();
        BlockEntity blockEntity = world.getBlockEntity(packet.pos());
        if (style == null) {
            SYNCED_BLOCK_ENTITY_STYLES.remove(pos);
            PENDING_BLOCK_ENTITY_STYLES.remove(pos);
            if (blockEntity != null) {
                refreshOutlineOnlyBlockEntity(blockEntity);
            }
            return;
        }

        if (blockEntity != null && !blockEntity.isRemoved()) {
            SYNCED_BLOCK_ENTITY_STYLES.put(pos, style);
            PENDING_BLOCK_ENTITY_STYLES.remove(pos);
            refreshOutlineOnlyBlockEntity(blockEntity);
            return;
        }

        SYNCED_BLOCK_ENTITY_STYLES.remove(pos);
        PENDING_BLOCK_ENTITY_STYLES.put(
                pos,
                new PendingBlockEntityStyle(style)
        );
    }

    static void onEntityLoad(Entity entity, ClientLevel world) {
        if (!activateEventWorld(world)) {
            return;
        }

        UUID uuid = entity.getUUID();
        PendingEntityOutline pending = PENDING_ENTITY_OUTLINES.remove(uuid);
        if (pending != null
                && pending.expiresAtTick() >= clientTicks
                && pending.entityId() == entity.getId()) {
            SYNCED_ENTITY_OUTLINES.put(uuid, pending.outline());
        }
    }

    static void onEntityUnload(Entity entity, ClientLevel world) {
        if (world != activeWorld) {
            return;
        }

        UUID uuid = entity.getUUID();
        LOCAL_ENTITY_OUTLINES.remove(uuid);
        SYNCED_ENTITY_OUTLINES.remove(uuid);
        PENDING_ENTITY_OUTLINES.remove(uuid);
        Iterator<RenderOutlineState> renderStates = RENDER_STATE_OUTLINES.values().iterator();
        while (renderStates.hasNext()) {
            if (renderStates.next().entity() == entity) {
                renderStates.remove();
            }
        }
        OutlineRenderBridge.clearAdaptiveThickness(entity);
    }

    static void onBlockEntityLoad(BlockEntity blockEntity, ClientLevel world) {
        if (!activateEventWorld(world)) {
            return;
        }

        long pos = blockEntity.getBlockPos().asLong();
        PendingBlockEntityStyle pending = PENDING_BLOCK_ENTITY_STYLES.remove(pos);
        if (pending != null) {
            SYNCED_BLOCK_ENTITY_STYLES.put(pos, pending.style());
        }
        refreshOutlineOnlyBlockEntity(blockEntity);
    }

    static void onBlockEntityUnload(BlockEntity blockEntity, ClientLevel world) {
        if (world != activeWorld) {
            return;
        }

        long pos = blockEntity.getBlockPos().asLong();
        LOCAL_BLOCK_ENTITY_STYLES.remove(pos);
        OutlineStyle synced = SYNCED_BLOCK_ENTITY_STYLES.remove(pos);
        if (synced != null) {
            PENDING_BLOCK_ENTITY_STYLES.put(pos, new PendingBlockEntityStyle(synced));
        }
        OUTLINE_ONLY_BLOCK_ENTITY_POSITIONS.remove(pos);
        OutlineRenderBridge.clearAdaptiveThickness(blockEntity);
    }

    static void onJoin(@Nullable ClientLevel world) {
        requireClientThread();
        resetWorld(world);
    }

    static void onDisconnect() {
        requireClientThread();
        resetWorld(null);
    }

    static void onEndClientTick(Minecraft client) {
        requireClientThread();
        observeWorld(client.level);
        clientTicks++;
        if ((clientTicks % 20L) == 0L) {
            removeExpiredPendingStyles();
        }
    }

    private static ClientLevel requireCurrentWorld() {
        requireClientThread();
        Minecraft client = Minecraft.getInstance();
        observeWorld(client.level);
        if (activeWorld == null) {
            throw new IllegalStateException("Client outlines require an active client world");
        }
        return activeWorld;
    }

    private static void requireCurrentTarget(Entity target, ClientLevel world) {
        Objects.requireNonNull(target, "target");
        if (target.level() != world || target.isRemoved()) {
            throw new IllegalArgumentException("Entity target must be loaded in the current client world");
        }
    }

    private static void requireCurrentTarget(BlockEntity target, ClientLevel world) {
        Objects.requireNonNull(target, "target");
        if (target.getLevel() != world || target.isRemoved()) {
            throw new IllegalArgumentException("Block entity target must be loaded in the current client world");
        }
    }

    @Nullable
    private static ClientLevel currentWorldForPacket(Identifier dimension) {
        Minecraft client = Minecraft.getInstance();
        observeWorld(client.level);
        if (activeWorld == null || !Objects.equals(activeDimension, dimension)) {
            return null;
        }
        return activeWorld;
    }

    private static boolean activateEventWorld(ClientLevel world) {
        Minecraft client = Minecraft.getInstance();
        if (world != client.level) {
            return false;
        }
        observeWorld(world);
        return true;
    }

    private static void observeWorld(@Nullable ClientLevel world) {
        if (activeWorld != world) {
            resetWorld(world);
        }
    }

    private static void resetWorld(@Nullable ClientLevel world) {
        clearAllState();
        OutlineRenderBridge.clearAdaptiveThickness();
        activeWorld = world;
        activeDimension = world == null ? null : world.dimension().
//? if <1.21.11 {
/*
                location()
*///?} else {
                identifier()
//?}
                ;
    }

    private static void clearAllState() {
        LOCAL_ENTITY_OUTLINES.clear();
        SYNCED_ENTITY_OUTLINES.clear();
        PENDING_ENTITY_OUTLINES.clear();
        RENDER_STATE_OUTLINES.clear();
        LOCAL_BLOCK_ENTITY_STYLES.clear();
        SYNCED_BLOCK_ENTITY_STYLES.clear();
        PENDING_BLOCK_ENTITY_STYLES.clear();
        OUTLINE_ONLY_BLOCK_ENTITY_POSITIONS.clear();
    }

    private static void refreshOutlineOnlyBlockEntity(BlockEntity blockEntity) {
        if (!(blockEntity instanceof OutlineOnlyBlockEntity)) {
            return;
        }
        long position = blockEntity.getBlockPos().asLong();
        if (LOCAL_BLOCK_ENTITY_STYLES.containsKey(position) || SYNCED_BLOCK_ENTITY_STYLES.containsKey(position)) {
            OUTLINE_ONLY_BLOCK_ENTITY_POSITIONS.add(position);
        } else {
            OUTLINE_ONLY_BLOCK_ENTITY_POSITIONS.remove(position);
        }
    }

    private static void discardLocalOutlineOnlyBlockEntityPositions() {
        LongIterator positions = OUTLINE_ONLY_BLOCK_ENTITY_POSITIONS.iterator();
        while (positions.hasNext()) {
            if (!SYNCED_BLOCK_ENTITY_STYLES.containsKey(positions.nextLong())) {
                positions.remove();
            }
        }
    }

    private static void removeExpiredPendingStyles() {
        Iterator<PendingEntityOutline> entityStyles = PENDING_ENTITY_OUTLINES.values().iterator();
        while (entityStyles.hasNext()) {
            if (entityStyles.next().expiresAtTick() < clientTicks) {
                entityStyles.remove();
            }
        }
    }

    @Nullable
    private static EntityOutline itemOutline(Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            OutlineStyle style = ClientItemOutlineManager.resolve(itemEntity.getItem(), ItemOutlineTarget.DROPPED);
            return style == null ? null : EntityOutline.body(style);
        }
        if (!(entity instanceof LivingEntity livingEntity)) {
            return null;
        }
        OutlineStyle mainHand = ClientItemOutlineManager.resolve(
                livingEntity.getMainHandItem(),
                ItemOutlineTarget.MAIN_HAND
        );
        OutlineStyle offHand = ClientItemOutlineManager.resolve(
                livingEntity.getOffhandItem(),
                ItemOutlineTarget.OFF_HAND
        );
        if (mainHand == null && offHand == null) {
            return null;
        }
        EntityOutline.Builder builder = EntityOutline.builder();
        if (mainHand != null) {
            builder.mainHand(mainHand);
        }
        if (offHand != null) {
            builder.offHand(offHand);
        }
        return builder.build();
    }

    private static void requireClientThread() {
        Minecraft client = Minecraft.getInstance();
        if (!client.isSameThread()) {
            throw new IllegalStateException("Client outline state must be accessed from the render thread");
        }
    }

    @Nullable
    private static OutlineStyle primary(@Nullable EntityOutline outline) {
        return outline == null ? null : outline.primaryStyle();
    }

    private record PendingEntityOutline(int entityId, EntityOutline outline, long expiresAtTick) {
    }

    private record RenderOutlineState(Entity entity, EntityOutline outline) {
    }

    private record PendingBlockEntityStyle(OutlineStyle style) {
    }
}
