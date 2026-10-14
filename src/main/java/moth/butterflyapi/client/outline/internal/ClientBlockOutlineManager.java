package moth.butterflyapi.client.outline.internal;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Objects;
import moth.butterflyapi.outline.OutlineStyle;
import moth.butterflyapi.outline.OutlinePlacement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class ClientBlockOutlineManager {
    private static final Long2ObjectMap<OutlineStyle> STYLES = new Long2ObjectOpenHashMap<>();
    private static final Long2ObjectMap<Long2ObjectMap<OutlineStyle>> STYLES_BY_CHUNK = new Long2ObjectOpenHashMap<>();
    private static final Object2IntOpenHashMap<OutlineStyle> STYLE_COUNTS = new Object2IntOpenHashMap<>();
    @Nullable
    private static ClientLevel activeWorld;

    private ClientBlockOutlineManager() {
    }

    public static void set(BlockPos position, OutlineStyle style) {
        requireCurrentWorld();
        put(Objects.requireNonNull(position, "position").asLong(), requireGeometryStyle(style));
    }

    public static void setAll(Iterable<BlockPos> positions, OutlineStyle style) {
        Objects.requireNonNull(positions, "positions");
        style = requireGeometryStyle(style);
        requireCurrentWorld();
        for (BlockPos position : positions) {
            put(Objects.requireNonNull(position, "position").asLong(), style);
        }
    }

    public static void clear(BlockPos position) {
        requireCurrentWorld();
        remove(Objects.requireNonNull(position, "position").asLong());
    }

    public static void clearAll() {
        requireClientThread();
        clearStyles();
    }

    @Nullable
    public static OutlineStyle get(BlockPos position) {
        requireCurrentWorld();
        return STYLES.get(Objects.requireNonNull(position, "position").asLong());
    }

    static Long2ObjectMap<OutlineStyle> styles() {
        return STYLES;
    }

    static Long2ObjectMap<OutlineStyle> stylesInChunk(int chunkX, int chunkZ) {
        return STYLES_BY_CHUNK.get(chunkKey(chunkX, chunkZ));
    }

    static boolean hasStyle(OutlineStyle style) {
        return STYLE_COUNTS.getInt(style) > 0;
    }

    public static void onEndClientTick(Minecraft client) {
        requireClientThread();
        if (activeWorld != client.level) {
            clearStyles();
            activeWorld = client.level;
        }
    }

    public static void onDisconnect() {
        requireClientThread();
        clearStyles();
        activeWorld = null;
    }

    @Nullable
    static ClientLevel world() {
        return activeWorld;
    }

    private static void requireCurrentWorld() {
        requireClientThread();
        Minecraft client = Minecraft.getInstance();
        if (activeWorld != client.level) {
            clearStyles();
            activeWorld = client.level;
        }
        if (activeWorld == null) {
            throw new IllegalStateException("Client block outlines require an active client world");
        }
    }

    private static void requireClientThread() {
        if (!Minecraft.getInstance().isSameThread()) {
            throw new IllegalStateException("Client block outline state must be accessed from the render thread");
        }
    }

    private static OutlineStyle requireGeometryStyle(OutlineStyle style) {
        OutlineStyle checked = Objects.requireNonNull(style, "style");
        if (checked.thickness() != 1
                || checked.placement() != OutlinePlacement.CENTERED
                || checked.blackBorder()
                || checked.visibleThroughWalls()
                || checked.matchModelLayer()
                || !checked.consistentThickness()
                || checked.thicknessAdaptationRate() != OutlineStyle.DEFAULT_THICKNESS_ADAPTATION_RATE) {
            throw new IllegalArgumentException(
                    "Block-position outlines support centered, one-pixel, wall-occluded styles without borders or model-layer matching"
            );
        }
        return checked;
    }

    private static void put(long position, OutlineStyle style) {
        OutlineStyle previous = STYLES.put(position, style);
        if (previous != null) {
            decrementStyle(previous);
        }
        STYLE_COUNTS.addTo(style, 1);
        int chunkX = BlockPos.getX(position) >> 4;
        int chunkZ = BlockPos.getZ(position) >> 4;
        STYLES_BY_CHUNK.computeIfAbsent(chunkKey(chunkX, chunkZ), ignored -> new Long2ObjectOpenHashMap<>())
                .put(position, style);
    }

    private static void remove(long position) {
        OutlineStyle previous = STYLES.remove(position);
        if (previous == null) {
            return;
        }
        decrementStyle(previous);
        int chunkX = BlockPos.getX(position) >> 4;
        int chunkZ = BlockPos.getZ(position) >> 4;
        long chunk = chunkKey(chunkX, chunkZ);
        Long2ObjectMap<OutlineStyle> positions = STYLES_BY_CHUNK.get(chunk);
        if (positions == null) {
            return;
        }
        positions.remove(position);
        if (positions.isEmpty()) {
            STYLES_BY_CHUNK.remove(chunk);
        }
        if (STYLES.isEmpty()) {
            BlockOutlineGeometryRenderer.clearCachedGeometry();
        }
    }

    private static void decrementStyle(OutlineStyle style) {
        int count = STYLE_COUNTS.getInt(style) - 1;
        if (count <= 0) {
            STYLE_COUNTS.removeInt(style);
        } else {
            STYLE_COUNTS.put(style, count);
        }
    }

    private static void clearStyles() {
        STYLES.clear();
        STYLES_BY_CHUNK.clear();
        STYLE_COUNTS.clear();
        BlockOutlineGeometryRenderer.clearCachedGeometry();
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX & 0xFFFFFFFFL) | (((long) chunkZ & 0xFFFFFFFFL) << 32);
    }
}
