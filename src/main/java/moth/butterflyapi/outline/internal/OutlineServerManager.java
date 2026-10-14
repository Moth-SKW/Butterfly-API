package moth.butterflyapi.outline.internal;

import moth.butterflyapi.outline.EntityOutline;
import moth.butterflyapi.outline.EntityOutlinePart;
import moth.butterflyapi.outline.OutlineStyle;
import moth.butterflyapi.outline.internal.network.BlockEntityOutlineUpdateS2CPacket;
import moth.butterflyapi.outline.internal.network.EntityOutlineUpdateS2CPacket;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class OutlineServerManager {
    private static final Map<MinecraftServer, State> STATES = new IdentityHashMap<>();
    private static boolean initialized;

    private OutlineServerManager() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        PayloadTypeRegistry.playS2C().register(
                EntityOutlineUpdateS2CPacket.ID,
                EntityOutlineUpdateS2CPacket.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                BlockEntityOutlineUpdateS2CPacket.ID,
                BlockEntityOutlineUpdateS2CPacket.CODEC
        );

        EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
            State state = existing(player.level().getServer());
            if (state != null) {
                state.startTracking(entity, player);
            }
        });
        EntityTrackingEvents.STOP_TRACKING.register((entity, player) -> {
            State state = existing(player.level().getServer());
            if (state != null) {
                state.stopTracking(entity, player);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            State state = existing(server);
            if (state != null) {
                state.syncWorld(handler.player,
//? if <1.21.11 {
/*
                        handler.player.serverLevel()
*///?} else {
                        handler.player.level()
//?}
                );
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            State state = existing(server);
            if (state != null) {
                state.playerDisconnected(handler.player);
            }
        });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            Entity.RemovalReason reason = entity.getRemovalReason();
            if (reason != null && reason.shouldDestroy()) {
                State state = existing(world.getServer());
                if (state != null) {
                    state.entityDestroyed(entity, world);
                }
            }
        });
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, world) -> {
            State state = existing(world.getServer());
            if (state != null) {
                state.blockEntityUnloaded(blockEntity, world);
            }
        });

        ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register(
                (original, moved, origin, destination) -> {
                    State state = existing(destination.getServer());
                    if (state != null) {
                        state.entityChangedWorld(original, moved, origin, destination);
                    }
                }
        );
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            State state = existing(destination.getServer());
            if (state != null) {
                state.playerChangedWorld(player, origin, destination);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            synchronized (OutlineServerManager.class) {
                STATES.remove(server);
            }
        });

        initialized = true;
    }

    public static void set(Entity entity, OutlineStyle style) {
        set(entity, EntityOutline.attached(style));
    }

    public static void set(Entity entity, EntityOutline outline) {
        ServerLevel world = requireServerWorld(entity);
        if (entity.isRemoved()) {
            throw new IllegalArgumentException("Cannot outline a removed entity");
        }
        requireServerThread(world.getServer());
        state(world.getServer()).set(entity, Objects.requireNonNull(outline, "outline"));
    }

    public static void setFor(ServerPlayer viewer, Entity entity, OutlineStyle style) {
        setFor(viewer, entity, EntityOutline.attached(style));
    }

    public static void setFor(ServerPlayer viewer, Entity entity, EntityOutline outline) {
        ServerLevel world = requireServerWorld(entity);
        requireSameServer(viewer, world.getServer());
        if (entity.isRemoved()) {
            throw new IllegalArgumentException("Cannot outline a removed entity");
        }
        requireServerThread(world.getServer());
        state(world.getServer()).setFor(viewer, entity, Objects.requireNonNull(outline, "outline"));
    }

    public static void setPart(Entity entity, EntityOutlinePart part, OutlineStyle style) {
        ServerLevel world = requireServerWorld(entity);
        if (entity.isRemoved()) {
            throw new IllegalArgumentException("Cannot outline a removed entity");
        }
        requireServerThread(world.getServer());
        state(world.getServer()).setPart(entity, part, style);
    }

    public static void setPartFor(ServerPlayer viewer, Entity entity, EntityOutlinePart part, OutlineStyle style) {
        ServerLevel world = requireServerWorld(entity);
        requireSameServer(viewer, world.getServer());
        if (entity.isRemoved()) {
            throw new IllegalArgumentException("Cannot outline a removed entity");
        }
        requireServerThread(world.getServer());
        state(world.getServer()).setPartFor(viewer, entity, part, style);
    }

    public static void clear(Entity entity) {
        ServerLevel world = requireServerWorld(entity);
        requireServerThread(world.getServer());
        State state = existing(world.getServer());
        if (state != null) {
            state.clear(entity);
        }
    }

    public static void clearPart(Entity entity, EntityOutlinePart part) {
        ServerLevel world = requireServerWorld(entity);
        requireServerThread(world.getServer());
        State state = existing(world.getServer());
        if (state != null) {
            state.clearPart(entity, part);
        }
    }

    public static void clearFor(ServerPlayer viewer, Entity entity) {
        ServerLevel world = requireServerWorld(entity);
        requireSameServer(viewer, world.getServer());
        requireServerThread(world.getServer());
        State state = existing(world.getServer());
        if (state != null) {
            state.clearFor(viewer, entity);
        }
    }

    public static void clearPartFor(ServerPlayer viewer, Entity entity, EntityOutlinePart part) {
        ServerLevel world = requireServerWorld(entity);
        requireSameServer(viewer, world.getServer());
        requireServerThread(world.getServer());
        State state = existing(world.getServer());
        if (state != null) {
            state.clearPartFor(viewer, entity, part);
        }
    }

    @Nullable
    public static OutlineStyle get(Entity entity) {
        ServerLevel world = requireServerWorld(entity);
        requireServerThread(world.getServer());
        State state = existing(world.getServer());
        return state == null ? null : state.get(entity);
    }

    @Nullable
    public static EntityOutline getOutline(Entity entity) {
        ServerLevel world = requireServerWorld(entity);
        requireServerThread(world.getServer());
        State state = existing(world.getServer());
        return state == null ? null : state.getOutline(entity);
    }

    @Nullable
    public static EntityOutline getOutlineFor(ServerPlayer viewer, Entity entity) {
        ServerLevel world = requireServerWorld(entity);
        requireSameServer(viewer, world.getServer());
        requireServerThread(world.getServer());
        State state = existing(world.getServer());
        return state == null ? null : state.getOutlineFor(viewer, entity);
    }

    @Nullable
    public static OutlineStyle getFor(ServerPlayer viewer, Entity entity) {
        ServerLevel world = requireServerWorld(entity);
        requireSameServer(viewer, world.getServer());
        requireServerThread(world.getServer());
        State state = existing(world.getServer());
        return state == null ? null : state.getFor(viewer, entity);
    }

    public static void set(BlockEntity blockEntity, OutlineStyle style) {
        ServerLevel world = requireServerWorld(blockEntity);
        if (blockEntity.isRemoved()) {
            throw new IllegalArgumentException("Cannot outline a removed block entity");
        }
        requireServerThread(world.getServer());
        state(world.getServer()).set(blockEntity, world, Objects.requireNonNull(style, "style"));
    }

    public static void setFor(ServerPlayer viewer, BlockEntity blockEntity, OutlineStyle style) {
        ServerLevel world = requireServerWorld(blockEntity);
        requireSameServer(viewer, world.getServer());
        if (blockEntity.isRemoved()) {
            throw new IllegalArgumentException("Cannot outline a removed block entity");
        }
        requireServerThread(world.getServer());
        state(world.getServer()).setFor(viewer, blockEntity, world, Objects.requireNonNull(style, "style"));
    }

    public static void clear(BlockEntity blockEntity) {
        ServerLevel world = requireServerWorld(blockEntity);
        requireServerThread(world.getServer());
        State state = existing(world.getServer());
        if (state != null) {
            state.clear(blockEntity, world);
        }
    }

    public static void clearFor(ServerPlayer viewer, BlockEntity blockEntity) {
        ServerLevel world = requireServerWorld(blockEntity);
        requireSameServer(viewer, world.getServer());
        requireServerThread(world.getServer());
        State state = existing(world.getServer());
        if (state != null) {
            state.clearFor(viewer, blockEntity, world);
        }
    }

    @Nullable
    public static OutlineStyle get(BlockEntity blockEntity) {
        ServerLevel world = requireServerWorld(blockEntity);
        requireServerThread(world.getServer());
        State state = existing(world.getServer());
        return state == null ? null : state.get(blockEntity, world);
    }

    @Nullable
    public static OutlineStyle getFor(ServerPlayer viewer, BlockEntity blockEntity) {
        ServerLevel world = requireServerWorld(blockEntity);
        requireSameServer(viewer, world.getServer());
        requireServerThread(world.getServer());
        State state = existing(world.getServer());
        return state == null ? null : state.getFor(viewer, blockEntity, world);
    }

    private static synchronized State state(MinecraftServer server) {
        return STATES.computeIfAbsent(server, State::new);
    }

    @Nullable
    private static synchronized State existing(MinecraftServer server) {
        return STATES.get(server);
    }

    private static ServerLevel requireServerWorld(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        if (!(entity.level() instanceof ServerLevel world)) {
            throw new IllegalArgumentException("Entity outlines must target an entity in a server world");
        }
        return world;
    }

    private static ServerLevel requireServerWorld(BlockEntity blockEntity) {
        Objects.requireNonNull(blockEntity, "blockEntity");
        if (!blockEntity.hasLevel() || !(blockEntity.getLevel() instanceof ServerLevel world)) {
            throw new IllegalArgumentException("Block entity outlines must target a block entity in a server world");
        }
        return world;
    }

    private static void requireSameServer(ServerPlayer viewer, MinecraftServer server) {
        Objects.requireNonNull(viewer, "viewer");
        if (viewer.level().getServer() != server) {
            throw new IllegalArgumentException("Viewer and outline target must belong to the same server");
        }
    }

    private static void requireServerThread(MinecraftServer server) {
        if (!server.isSameThread()) {
            throw new IllegalStateException("ServerOutlines must be used from the logical server thread");
        }
    }

    private static final class State {
        private final MinecraftServer server;
        private final Map<UUID, EntityEntry> entityEntries = new HashMap<>();
        private final Map<BlockTarget, BlockEntry> blockEntries = new HashMap<>();

        private State(MinecraftServer server) {
            this.server = server;
        }

        void set(Entity entity, EntityOutline outline) {
            EntityEntry entry = entityEntry(entity);
            if (outline.equals(entry.global)) {
                return;
            }
            entry.global = outline;
            syncEntityViewers(entity, entry);
        }

        void setFor(ServerPlayer viewer, Entity entity, EntityOutline outline) {
            EntityEntry entry = entityEntry(entity);
            entry.viewerOutlines.put(viewer.getUUID(), outline);
            send(viewer, entry, outline);
        }

        void setPart(Entity entity, EntityOutlinePart part, OutlineStyle style) {
            EntityEntry entry = entityEntry(entity);
            EntityOutline current = entry.global == null ? EntityOutline.empty() : entry.global;
            entry.global = current.with(part, style);
            syncEntityViewers(entity, entry);
        }

        void setPartFor(ServerPlayer viewer, Entity entity, EntityOutlinePart part, OutlineStyle style) {
            EntityEntry entry = entityEntry(entity);
            EntityOutline current = entry.effective(viewer.getUUID());
            entry.viewerOutlines.put(viewer.getUUID(), (current == null ? EntityOutline.empty() : current).with(part, style));
            send(viewer, entry, entry.effective(viewer.getUUID()));
        }

        void clear(Entity entity) {
            EntityEntry entry = entityEntries.get(entity.getUUID());
            if (entry == null) {
                return;
            }
            updateEntityTarget(entry, EntityTarget.of(entity));
            if (entry.global == null) {
                return;
            }
            entry.global = null;
            syncEntityViewers(entity, entry);
            pruneEntity(entry);
        }

        void clearPart(Entity entity, EntityOutlinePart part) {
            EntityEntry entry = entityEntries.get(entity.getUUID());
            if (entry == null || entry.global == null) {
                return;
            }
            updateEntityTarget(entry, EntityTarget.of(entity));
            entry.global = entry.global.without(part);
            if (entry.global.isEmpty()) {
                entry.global = null;
            }
            syncEntityViewers(entity, entry);
            pruneEntity(entry);
        }

        void clearFor(ServerPlayer viewer, Entity entity) {
            EntityEntry entry = entityEntries.get(entity.getUUID());
            if (entry == null) {
                return;
            }
            updateEntityTarget(entry, EntityTarget.of(entity));
            entry.viewerOutlines.remove(viewer.getUUID());
            send(viewer, entry, entry.global);
            pruneEntity(entry);
        }

        void clearPartFor(ServerPlayer viewer, Entity entity, EntityOutlinePart part) {
            EntityEntry entry = entityEntries.get(entity.getUUID());
            if (entry == null) {
                return;
            }
            updateEntityTarget(entry, EntityTarget.of(entity));
            EntityOutline viewerOutline = entry.viewerOutlines.get(viewer.getUUID());
            if (viewerOutline == null) {
                return;
            }
            EntityOutline updated = viewerOutline.without(part);
            if (updated.isEmpty() && entry.global == null) {
                entry.viewerOutlines.remove(viewer.getUUID());
            } else {
                entry.viewerOutlines.put(viewer.getUUID(), updated);
            }
            send(viewer, entry, entry.effective(viewer.getUUID()));
            pruneEntity(entry);
        }

        @Nullable
        OutlineStyle get(Entity entity) {
            EntityEntry entry = entityEntries.get(entity.getUUID());
            return entry == null || entry.global == null ? null : entry.global.primaryStyle();
        }

        @Nullable
        OutlineStyle getFor(ServerPlayer viewer, Entity entity) {
            EntityEntry entry = entityEntries.get(entity.getUUID());
            EntityOutline outline = entry == null ? null : entry.effective(viewer.getUUID());
            return outline == null ? null : outline.primaryStyle();
        }

        @Nullable
        EntityOutline getOutline(Entity entity) {
            EntityEntry entry = entityEntries.get(entity.getUUID());
            return entry == null ? null : entry.global;
        }

        @Nullable
        EntityOutline getOutlineFor(ServerPlayer viewer, Entity entity) {
            EntityEntry entry = entityEntries.get(entity.getUUID());
            return entry == null ? null : entry.effective(viewer.getUUID());
        }

        void set(BlockEntity blockEntity, ServerLevel world, OutlineStyle style) {
            BlockEntry entry = blockEntry(blockEntity, world);
            if (style.equals(entry.global)) {
                return;
            }
            entry.global = style;
            syncBlockWorld(world, entry);
        }

        void setFor(ServerPlayer viewer, BlockEntity blockEntity, ServerLevel world, OutlineStyle style) {
            BlockEntry entry = blockEntry(blockEntity, world);
            entry.viewerStyles.put(viewer.getUUID(), style);
            send(viewer, entry, style);
        }

        void clear(BlockEntity blockEntity, ServerLevel world) {
            BlockTarget target = BlockTarget.of(blockEntity, world);
            BlockEntry entry = blockEntries.get(target);
            if (entry == null) {
                return;
            }
            if (entry.global == null) {
                return;
            }
            entry.global = null;
            syncBlockWorld(world, entry);
            pruneBlock(entry);
        }

        void clearFor(ServerPlayer viewer, BlockEntity blockEntity, ServerLevel world) {
            BlockTarget target = BlockTarget.of(blockEntity, world);
            BlockEntry entry = blockEntries.get(target);
            if (entry == null) {
                return;
            }
            entry.viewerStyles.remove(viewer.getUUID());
            send(viewer, entry, entry.global);
            pruneBlock(entry);
        }

        @Nullable
        OutlineStyle get(BlockEntity blockEntity, ServerLevel world) {
            BlockEntry entry = blockEntries.get(BlockTarget.of(blockEntity, world));
            return entry == null ? null : entry.global;
        }

        @Nullable
        OutlineStyle getFor(ServerPlayer viewer, BlockEntity blockEntity, ServerLevel world) {
            BlockEntry entry = blockEntries.get(BlockTarget.of(blockEntity, world));
            return entry == null ? null : entry.effective(viewer.getUUID());
        }

        void startTracking(Entity entity, ServerPlayer player) {
            EntityEntry entry = entityEntries.get(entity.getUUID());
            if (entry == null) {
                return;
            }
            updateEntityTarget(entry, EntityTarget.of(entity));
            EntityOutline outline = entry.effective(player.getUUID());
            if (outline != null) {
                send(player, entry, outline);
            }
        }

        void stopTracking(Entity entity, ServerPlayer player) {
            EntityEntry entry = entityEntries.get(entity.getUUID());
            if (entry != null && entry.effective(player.getUUID()) != null) {
                send(player, entry, null);
                entry.sentOutlines.remove(player.getUUID());
            }
        }

        void syncWorld(ServerPlayer player, ServerLevel world) {
            Identifier dimension = dimension(world);
            UUID viewer = player.getUUID();
            for (EntityEntry entry : entityEntries.values()) {
                if (entry.target.dimension().equals(dimension)) {
                    EntityOutline outline = entry.effective(viewer);
                    if (outline != null) {
                        send(player, entry, outline);
                    }
                }
            }
            for (BlockEntry entry : blockEntries.values()) {
                if (entry.target.dimension().equals(dimension)) {
                    OutlineStyle style = entry.effective(viewer);
                    if (style != null) {
                        send(player, entry, style);
                    }
                }
            }
        }

        void playerDisconnected(ServerPlayer player) {
            UUID viewer = player.getUUID();
            EntityEntry playerEntry = entityEntries.remove(viewer);
            if (playerEntry != null) {
                clearEntityFromWorld(playerEntry,
//? if <1.21.11 {
/*
                        player.serverLevel()
*///?} else {
                        player.level()
//?}
                );
            }
            entityEntries.values().forEach(entry -> entry.viewerOutlines.remove(viewer));
            blockEntries.values().forEach(entry -> entry.viewerStyles.remove(viewer));
            entityEntries.values().forEach(entry -> entry.sentOutlines.remove(viewer));
            blockEntries.values().forEach(entry -> entry.sentStyles.remove(viewer));
            entityEntries.values().removeIf(entry -> entry.isEmpty());
            blockEntries.values().removeIf(entry -> entry.isEmpty());
        }

        void entityDestroyed(Entity entity, ServerLevel world) {
            EntityEntry entry = entityEntries.remove(entity.getUUID());
            if (entry != null) {
                clearEntityFromWorld(entry, world);
            }
        }

        void blockEntityUnloaded(BlockEntity blockEntity, ServerLevel world) {
            BlockEntry entry = blockEntries.remove(BlockTarget.of(blockEntity, world));
            if (entry != null) {
                clearBlockFromWorld(entry, world);
            }
        }

        void entityChangedWorld(Entity original, Entity moved, ServerLevel origin, ServerLevel destination) {
            EntityEntry entry = entityEntries.remove(original.getUUID());
            if (entry == null) {
                return;
            }

            entry.target = EntityTarget.of(original, origin);
            clearEntityFromWorld(entry, origin);
            entry.target = EntityTarget.of(moved);
            entityEntries.put(moved.getUUID(), entry);
            syncEntityWorld(destination, entry);
        }

        void playerChangedWorld(ServerPlayer player, ServerLevel origin, ServerLevel destination) {
            clearWorldFor(player, origin);

            EntityEntry playerEntry = entityEntries.get(player.getUUID());
            if (playerEntry != null) {
                playerEntry.target = EntityTarget.of(player, origin);
                clearEntityFromWorld(playerEntry, origin);
                playerEntry.target = EntityTarget.of(player);
                syncEntityWorld(destination, playerEntry);
            }

            syncWorld(player, destination);
        }

        private EntityEntry entityEntry(Entity entity) {
            EntityTarget target = EntityTarget.of(entity);
            EntityEntry entry = entityEntries.computeIfAbsent(
                    target.entityUuid(),
                    ignored -> new EntityEntry(target)
            );
            updateEntityTarget(entry, target);
            return entry;
        }

        private BlockEntry blockEntry(BlockEntity blockEntity, ServerLevel world) {
            BlockTarget target = BlockTarget.of(blockEntity, world);
            return blockEntries.computeIfAbsent(target, BlockEntry::new);
        }

        private void updateEntityTarget(EntityEntry entry, EntityTarget target) {
            if (!entry.target.dimension().equals(target.dimension())) {
                ServerLevel oldWorld = world(entry.target.dimension());
                if (oldWorld != null) {
                    clearEntityFromWorld(entry, oldWorld);
                }
            }
            entry.target = target;
        }

        private void syncEntityViewers(Entity entity, EntityEntry entry) {
            for (ServerPlayer player : entityViewers(entity)) {
                send(player, entry, entry.effective(player.getUUID()));
            }
        }

        private void syncEntityWorld(ServerLevel world, EntityEntry entry) {
            for (ServerPlayer player : PlayerLookup.world(world)) {
                send(player, entry, entry.effective(player.getUUID()));
            }
        }

        private void syncBlockWorld(ServerLevel world, BlockEntry entry) {
            for (ServerPlayer player : PlayerLookup.tracking(world, entry.target.pos())) {
                send(player, entry, entry.effective(player.getUUID()));
            }
        }

        private void clearWorldFor(ServerPlayer player, ServerLevel world) {
            Identifier dimension = dimension(world);
            UUID viewer = player.getUUID();
            for (EntityEntry entry : entityEntries.values()) {
                if (entry.target.dimension().equals(dimension) && entry.effective(viewer) != null) {
                    send(player, entry, null);
                    entry.sentOutlines.remove(viewer);
                }
            }
            for (BlockEntry entry : blockEntries.values()) {
                if (entry.target.dimension().equals(dimension) && entry.effective(viewer) != null) {
                    send(player, entry, null);
                    entry.sentStyles.remove(viewer);
                }
            }
        }

        private void clearEntityFromWorld(EntityEntry entry, ServerLevel world) {
            for (ServerPlayer player : PlayerLookup.world(world)) {
                if (entry.effective(player.getUUID()) != null) {
                    send(player, entry, null);
                    entry.sentOutlines.remove(player.getUUID());
                }
            }
        }

        private void clearBlockFromWorld(BlockEntry entry, ServerLevel world) {
            for (ServerPlayer player : PlayerLookup.world(world)) {
                if (entry.effective(player.getUUID()) != null) {
                    send(player, entry, null);
                    entry.sentStyles.remove(player.getUUID());
                }
            }
        }

        private Collection<ServerPlayer> entityViewers(Entity entity) {
            Set<ServerPlayer> players = new LinkedHashSet<>(PlayerLookup.tracking(entity));
            if (entity instanceof ServerPlayer player) {
                players.add(player);
            }
            return players;
        }

        private void pruneEntity(EntityEntry entry) {
            if (entry.isEmpty()) {
                entityEntries.remove(entry.target.entityUuid(), entry);
            }
        }

        private void pruneBlock(BlockEntry entry) {
            if (entry.isEmpty()) {
                blockEntries.remove(entry.target, entry);
            }
        }

        @Nullable
        private ServerLevel world(Identifier dimension) {
            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, dimension);
            return server.getLevel(key);
        }

        private void send(ServerPlayer player, EntityEntry entry, @Nullable EntityOutline outline) {
            UUID viewer = player.getUUID();
            if (entry.sentOutlines.containsKey(viewer) && Objects.equals(entry.sentOutlines.get(viewer), outline)) {
                return;
            }
            if (ServerPlayNetworking.canSend(player, EntityOutlineUpdateS2CPacket.ID)) {
                ServerPlayNetworking.send(
                        player,
                        new EntityOutlineUpdateS2CPacket(
                                entry.target.dimension(),
                                entry.target.entityId(),
                                entry.target.entityUuid(),
                                outline
                        )
                );
                entry.sentOutlines.put(viewer, outline);
            }
        }

        private void send(ServerPlayer player, BlockEntry entry, @Nullable OutlineStyle style) {
            UUID viewer = player.getUUID();
            if (entry.sentStyles.containsKey(viewer) && Objects.equals(entry.sentStyles.get(viewer), style)) {
                return;
            }
            if (ServerPlayNetworking.canSend(player, BlockEntityOutlineUpdateS2CPacket.ID)) {
                ServerPlayNetworking.send(
                        player,
                        new BlockEntityOutlineUpdateS2CPacket(entry.target.dimension(), entry.target.pos(), style)
                );
                entry.sentStyles.put(viewer, style);
            }
        }
    }

    private abstract static class Entry {
        @Nullable
        OutlineStyle global;
        final Map<UUID, OutlineStyle> viewerStyles = new HashMap<>();
        final Map<UUID, OutlineStyle> sentStyles = new HashMap<>();

        @Nullable
        final OutlineStyle effective(UUID viewer) {
            OutlineStyle viewerStyle = viewerStyles.get(viewer);
            return viewerStyle == null ? global : viewerStyle;
        }

        final boolean isEmpty() {
            return global == null && viewerStyles.isEmpty();
        }
    }

    private static final class EntityEntry {
        EntityTarget target;
        @Nullable
        EntityOutline global;
        final Map<UUID, EntityOutline> viewerOutlines = new HashMap<>();
        final Map<UUID, EntityOutline> sentOutlines = new HashMap<>();

        EntityEntry(EntityTarget target) {
            this.target = target;
        }

        @Nullable
        EntityOutline effective(UUID viewer) {
            EntityOutline viewerOutline = viewerOutlines.get(viewer);
            return viewerOutline == null ? global : viewerOutline;
        }

        boolean isEmpty() {
            return global == null && viewerOutlines.isEmpty();
        }
    }

    private static final class BlockEntry extends Entry {
        final BlockTarget target;

        BlockEntry(BlockTarget target) {
            this.target = target;
        }
    }

    private record EntityTarget(Identifier dimension, int entityId, UUID entityUuid) {
        EntityTarget {
            Objects.requireNonNull(dimension, "dimension");
            Objects.requireNonNull(entityUuid, "entityUuid");
        }

        static EntityTarget of(Entity entity) {
            return new EntityTarget(
                    OutlineServerManager.dimension(entity.level()),
                    entity.getId(),
                    entity.getUUID()
            );
        }

        static EntityTarget of(Entity entity, ServerLevel world) {
            return new EntityTarget(OutlineServerManager.dimension(world), entity.getId(), entity.getUUID());
        }
    }

    private record BlockTarget(Identifier dimension, BlockPos pos) {
        BlockTarget {
            Objects.requireNonNull(dimension, "dimension");
            pos = Objects.requireNonNull(pos, "pos").immutable();
        }

        static BlockTarget of(BlockEntity blockEntity, ServerLevel world) {
            return new BlockTarget(OutlineServerManager.dimension(world), blockEntity.getBlockPos());
        }
    }

    private static Identifier dimension(Level world) {
        return world.dimension().
//? if <1.21.11 {
/*
                location()
*///?} else {
                identifier()
//?}
                ;
    }
}
