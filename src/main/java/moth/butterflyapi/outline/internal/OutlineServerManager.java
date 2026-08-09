package moth.butterflyapi.outline.internal;

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
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
            State state = existing(player.getEntityWorld().getServer());
            if (state != null) {
                state.startTracking(entity, player);
            }
        });
        EntityTrackingEvents.STOP_TRACKING.register((entity, player) -> {
            State state = existing(player.getEntityWorld().getServer());
            if (state != null) {
                state.stopTracking(entity, player);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            State state = existing(server);
            if (state != null) {
                state.syncWorld(handler.player, handler.player.getEntityWorld());
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
        ServerWorld world = requireServerWorld(entity);
        if (entity.isRemoved()) {
            throw new IllegalArgumentException("Cannot outline a removed entity");
        }
        requireServerThread(world.getServer());
        state(world.getServer()).set(entity, Objects.requireNonNull(style, "style"));
    }

    public static void setFor(ServerPlayerEntity viewer, Entity entity, OutlineStyle style) {
        ServerWorld world = requireServerWorld(entity);
        requireSameServer(viewer, world.getServer());
        if (entity.isRemoved()) {
            throw new IllegalArgumentException("Cannot outline a removed entity");
        }
        requireServerThread(world.getServer());
        state(world.getServer()).setFor(viewer, entity, Objects.requireNonNull(style, "style"));
    }

    public static void clear(Entity entity) {
        ServerWorld world = requireServerWorld(entity);
        requireServerThread(world.getServer());
        State state = existing(world.getServer());
        if (state != null) {
            state.clear(entity);
        }
    }

    public static void clearFor(ServerPlayerEntity viewer, Entity entity) {
        ServerWorld world = requireServerWorld(entity);
        requireSameServer(viewer, world.getServer());
        requireServerThread(world.getServer());
        State state = existing(world.getServer());
        if (state != null) {
            state.clearFor(viewer, entity);
        }
    }

    @Nullable
    public static OutlineStyle get(Entity entity) {
        ServerWorld world = requireServerWorld(entity);
        requireServerThread(world.getServer());
        State state = existing(world.getServer());
        return state == null ? null : state.get(entity);
    }

    @Nullable
    public static OutlineStyle getFor(ServerPlayerEntity viewer, Entity entity) {
        ServerWorld world = requireServerWorld(entity);
        requireSameServer(viewer, world.getServer());
        requireServerThread(world.getServer());
        State state = existing(world.getServer());
        return state == null ? null : state.getFor(viewer, entity);
    }

    public static void set(BlockEntity blockEntity, OutlineStyle style) {
        ServerWorld world = requireServerWorld(blockEntity);
        if (blockEntity.isRemoved()) {
            throw new IllegalArgumentException("Cannot outline a removed block entity");
        }
        requireServerThread(world.getServer());
        state(world.getServer()).set(blockEntity, world, Objects.requireNonNull(style, "style"));
    }

    public static void setFor(ServerPlayerEntity viewer, BlockEntity blockEntity, OutlineStyle style) {
        ServerWorld world = requireServerWorld(blockEntity);
        requireSameServer(viewer, world.getServer());
        if (blockEntity.isRemoved()) {
            throw new IllegalArgumentException("Cannot outline a removed block entity");
        }
        requireServerThread(world.getServer());
        state(world.getServer()).setFor(viewer, blockEntity, world, Objects.requireNonNull(style, "style"));
    }

    public static void clear(BlockEntity blockEntity) {
        ServerWorld world = requireServerWorld(blockEntity);
        requireServerThread(world.getServer());
        State state = existing(world.getServer());
        if (state != null) {
            state.clear(blockEntity, world);
        }
    }

    public static void clearFor(ServerPlayerEntity viewer, BlockEntity blockEntity) {
        ServerWorld world = requireServerWorld(blockEntity);
        requireSameServer(viewer, world.getServer());
        requireServerThread(world.getServer());
        State state = existing(world.getServer());
        if (state != null) {
            state.clearFor(viewer, blockEntity, world);
        }
    }

    @Nullable
    public static OutlineStyle get(BlockEntity blockEntity) {
        ServerWorld world = requireServerWorld(blockEntity);
        requireServerThread(world.getServer());
        State state = existing(world.getServer());
        return state == null ? null : state.get(blockEntity, world);
    }

    @Nullable
    public static OutlineStyle getFor(ServerPlayerEntity viewer, BlockEntity blockEntity) {
        ServerWorld world = requireServerWorld(blockEntity);
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

    private static ServerWorld requireServerWorld(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        if (!(entity.getEntityWorld() instanceof ServerWorld world)) {
            throw new IllegalArgumentException("Entity outlines must target an entity in a server world");
        }
        return world;
    }

    private static ServerWorld requireServerWorld(BlockEntity blockEntity) {
        Objects.requireNonNull(blockEntity, "blockEntity");
        if (!blockEntity.hasWorld() || !(blockEntity.getWorld() instanceof ServerWorld world)) {
            throw new IllegalArgumentException("Block entity outlines must target a block entity in a server world");
        }
        return world;
    }

    private static void requireSameServer(ServerPlayerEntity viewer, MinecraftServer server) {
        Objects.requireNonNull(viewer, "viewer");
        if (viewer.getEntityWorld().getServer() != server) {
            throw new IllegalArgumentException("Viewer and outline target must belong to the same server");
        }
    }

    private static void requireServerThread(MinecraftServer server) {
        if (!server.isOnThread()) {
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

        void set(Entity entity, OutlineStyle style) {
            EntityEntry entry = entityEntry(entity);
            entry.global = style;
            syncEntityViewers(entity, entry);
        }

        void setFor(ServerPlayerEntity viewer, Entity entity, OutlineStyle style) {
            EntityEntry entry = entityEntry(entity);
            entry.viewerStyles.put(viewer.getUuid(), style);
            send(viewer, entry.target, style);
        }

        void clear(Entity entity) {
            EntityEntry entry = entityEntries.get(entity.getUuid());
            if (entry == null) {
                return;
            }
            updateEntityTarget(entry, EntityTarget.of(entity));
            entry.global = null;
            syncEntityViewers(entity, entry);
            pruneEntity(entry);
        }

        void clearFor(ServerPlayerEntity viewer, Entity entity) {
            EntityEntry entry = entityEntries.get(entity.getUuid());
            if (entry == null) {
                return;
            }
            updateEntityTarget(entry, EntityTarget.of(entity));
            entry.viewerStyles.remove(viewer.getUuid());
            send(viewer, entry.target, entry.global);
            pruneEntity(entry);
        }

        @Nullable
        OutlineStyle get(Entity entity) {
            EntityEntry entry = entityEntries.get(entity.getUuid());
            return entry == null ? null : entry.global;
        }

        @Nullable
        OutlineStyle getFor(ServerPlayerEntity viewer, Entity entity) {
            EntityEntry entry = entityEntries.get(entity.getUuid());
            return entry == null ? null : entry.effective(viewer.getUuid());
        }

        void set(BlockEntity blockEntity, ServerWorld world, OutlineStyle style) {
            BlockEntry entry = blockEntry(blockEntity, world);
            entry.global = style;
            syncBlockWorld(world, entry);
        }

        void setFor(ServerPlayerEntity viewer, BlockEntity blockEntity, ServerWorld world, OutlineStyle style) {
            BlockEntry entry = blockEntry(blockEntity, world);
            entry.viewerStyles.put(viewer.getUuid(), style);
            send(viewer, entry.target, style);
        }

        void clear(BlockEntity blockEntity, ServerWorld world) {
            BlockTarget target = BlockTarget.of(blockEntity, world);
            BlockEntry entry = blockEntries.get(target);
            if (entry == null) {
                return;
            }
            entry.global = null;
            syncBlockWorld(world, entry);
            pruneBlock(entry);
        }

        void clearFor(ServerPlayerEntity viewer, BlockEntity blockEntity, ServerWorld world) {
            BlockTarget target = BlockTarget.of(blockEntity, world);
            BlockEntry entry = blockEntries.get(target);
            if (entry == null) {
                return;
            }
            entry.viewerStyles.remove(viewer.getUuid());
            send(viewer, entry.target, entry.global);
            pruneBlock(entry);
        }

        @Nullable
        OutlineStyle get(BlockEntity blockEntity, ServerWorld world) {
            BlockEntry entry = blockEntries.get(BlockTarget.of(blockEntity, world));
            return entry == null ? null : entry.global;
        }

        @Nullable
        OutlineStyle getFor(ServerPlayerEntity viewer, BlockEntity blockEntity, ServerWorld world) {
            BlockEntry entry = blockEntries.get(BlockTarget.of(blockEntity, world));
            return entry == null ? null : entry.effective(viewer.getUuid());
        }

        void startTracking(Entity entity, ServerPlayerEntity player) {
            EntityEntry entry = entityEntries.get(entity.getUuid());
            if (entry == null) {
                return;
            }
            updateEntityTarget(entry, EntityTarget.of(entity));
            OutlineStyle style = entry.effective(player.getUuid());
            if (style != null) {
                send(player, entry.target, style);
            }
        }

        void stopTracking(Entity entity, ServerPlayerEntity player) {
            EntityEntry entry = entityEntries.get(entity.getUuid());
            if (entry != null && entry.effective(player.getUuid()) != null) {
                send(player, entry.target, null);
            }
        }

        void syncWorld(ServerPlayerEntity player, ServerWorld world) {
            Identifier dimension = dimension(world);
            UUID viewer = player.getUuid();
            for (EntityEntry entry : entityEntries.values()) {
                if (entry.target.dimension().equals(dimension)) {
                    OutlineStyle style = entry.effective(viewer);
                    if (style != null) {
                        send(player, entry.target, style);
                    }
                }
            }
            for (BlockEntry entry : blockEntries.values()) {
                if (entry.target.dimension().equals(dimension)) {
                    OutlineStyle style = entry.effective(viewer);
                    if (style != null) {
                        send(player, entry.target, style);
                    }
                }
            }
        }

        void playerDisconnected(ServerPlayerEntity player) {
            UUID viewer = player.getUuid();
            EntityEntry playerEntry = entityEntries.remove(viewer);
            if (playerEntry != null) {
                clearEntityFromWorld(playerEntry, player.getEntityWorld());
            }
            entityEntries.values().forEach(entry -> entry.viewerStyles.remove(viewer));
            blockEntries.values().forEach(entry -> entry.viewerStyles.remove(viewer));
            entityEntries.values().removeIf(entry -> entry.isEmpty());
            blockEntries.values().removeIf(entry -> entry.isEmpty());
        }

        void entityDestroyed(Entity entity, ServerWorld world) {
            EntityEntry entry = entityEntries.remove(entity.getUuid());
            if (entry != null) {
                clearEntityFromWorld(entry, world);
            }
        }

        void blockEntityUnloaded(BlockEntity blockEntity, ServerWorld world) {
            BlockEntry entry = blockEntries.remove(BlockTarget.of(blockEntity, world));
            if (entry != null) {
                clearBlockFromWorld(entry, world);
            }
        }

        void entityChangedWorld(Entity original, Entity moved, ServerWorld origin, ServerWorld destination) {
            EntityEntry entry = entityEntries.remove(original.getUuid());
            if (entry == null) {
                return;
            }

            entry.target = EntityTarget.of(original, origin);
            clearEntityFromWorld(entry, origin);
            entry.target = EntityTarget.of(moved);
            entityEntries.put(moved.getUuid(), entry);
            syncEntityWorld(destination, entry);
        }

        void playerChangedWorld(ServerPlayerEntity player, ServerWorld origin, ServerWorld destination) {
            clearWorldFor(player, origin);

            EntityEntry playerEntry = entityEntries.get(player.getUuid());
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

        private BlockEntry blockEntry(BlockEntity blockEntity, ServerWorld world) {
            BlockTarget target = BlockTarget.of(blockEntity, world);
            return blockEntries.computeIfAbsent(target, BlockEntry::new);
        }

        private void updateEntityTarget(EntityEntry entry, EntityTarget target) {
            if (!entry.target.dimension().equals(target.dimension())) {
                ServerWorld oldWorld = world(entry.target.dimension());
                if (oldWorld != null) {
                    clearEntityFromWorld(entry, oldWorld);
                }
            }
            entry.target = target;
        }

        private void syncEntityViewers(Entity entity, EntityEntry entry) {
            for (ServerPlayerEntity player : entityViewers(entity)) {
                send(player, entry.target, entry.effective(player.getUuid()));
            }
        }

        private void syncEntityWorld(ServerWorld world, EntityEntry entry) {
            for (ServerPlayerEntity player : PlayerLookup.world(world)) {
                send(player, entry.target, entry.effective(player.getUuid()));
            }
        }

        private void syncBlockWorld(ServerWorld world, BlockEntry entry) {
            for (ServerPlayerEntity player : PlayerLookup.world(world)) {
                send(player, entry.target, entry.effective(player.getUuid()));
            }
        }

        private void clearWorldFor(ServerPlayerEntity player, ServerWorld world) {
            Identifier dimension = dimension(world);
            UUID viewer = player.getUuid();
            for (EntityEntry entry : entityEntries.values()) {
                if (entry.target.dimension().equals(dimension) && entry.effective(viewer) != null) {
                    send(player, entry.target, null);
                }
            }
            for (BlockEntry entry : blockEntries.values()) {
                if (entry.target.dimension().equals(dimension) && entry.effective(viewer) != null) {
                    send(player, entry.target, null);
                }
            }
        }

        private void clearEntityFromWorld(EntityEntry entry, ServerWorld world) {
            for (ServerPlayerEntity player : PlayerLookup.world(world)) {
                if (entry.effective(player.getUuid()) != null) {
                    send(player, entry.target, null);
                }
            }
        }

        private void clearBlockFromWorld(BlockEntry entry, ServerWorld world) {
            for (ServerPlayerEntity player : PlayerLookup.world(world)) {
                if (entry.effective(player.getUuid()) != null) {
                    send(player, entry.target, null);
                }
            }
        }

        private Collection<ServerPlayerEntity> entityViewers(Entity entity) {
            Set<ServerPlayerEntity> players = new LinkedHashSet<>(PlayerLookup.tracking(entity));
            if (entity instanceof ServerPlayerEntity player) {
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
        private ServerWorld world(Identifier dimension) {
            RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, dimension);
            return server.getWorld(key);
        }

        private static void send(
                ServerPlayerEntity player,
                EntityTarget target,
                @Nullable OutlineStyle style
        ) {
            if (ServerPlayNetworking.canSend(player, EntityOutlineUpdateS2CPacket.ID)) {
                ServerPlayNetworking.send(
                        player,
                        new EntityOutlineUpdateS2CPacket(
                                target.dimension(),
                                target.entityId(),
                                target.entityUuid(),
                                style
                        )
                );
            }
        }

        private static void send(
                ServerPlayerEntity player,
                BlockTarget target,
                @Nullable OutlineStyle style
        ) {
            if (ServerPlayNetworking.canSend(player, BlockEntityOutlineUpdateS2CPacket.ID)) {
                ServerPlayNetworking.send(
                        player,
                        new BlockEntityOutlineUpdateS2CPacket(target.dimension(), target.pos(), style)
                );
            }
        }
    }

    private abstract static class Entry {
        @Nullable
        OutlineStyle global;
        final Map<UUID, OutlineStyle> viewerStyles = new HashMap<>();

        @Nullable
        final OutlineStyle effective(UUID viewer) {
            OutlineStyle viewerStyle = viewerStyles.get(viewer);
            return viewerStyle == null ? global : viewerStyle;
        }

        final boolean isEmpty() {
            return global == null && viewerStyles.isEmpty();
        }
    }

    private static final class EntityEntry extends Entry {
        EntityTarget target;

        EntityEntry(EntityTarget target) {
            this.target = target;
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
                    OutlineServerManager.dimension(entity.getEntityWorld()),
                    entity.getId(),
                    entity.getUuid()
            );
        }

        static EntityTarget of(Entity entity, ServerWorld world) {
            return new EntityTarget(OutlineServerManager.dimension(world), entity.getId(), entity.getUuid());
        }
    }

    private record BlockTarget(Identifier dimension, BlockPos pos) {
        BlockTarget {
            Objects.requireNonNull(dimension, "dimension");
            pos = Objects.requireNonNull(pos, "pos").toImmutable();
        }

        static BlockTarget of(BlockEntity blockEntity, ServerWorld world) {
            return new BlockTarget(OutlineServerManager.dimension(world), blockEntity.getPos());
        }
    }

    private static Identifier dimension(World world) {
        return world.getRegistryKey().getValue();
    }
}
