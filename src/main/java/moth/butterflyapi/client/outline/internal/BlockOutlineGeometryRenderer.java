package moth.butterflyapi.client.outline.internal;

//? if >=1.21.1 {
import com.mojang.blaze3d.vertex.PoseStack;
//? if <26.2 {
import com.mojang.blaze3d.vertex.VertexConsumer;
//?}
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import moth.butterflyapi.outline.OutlineStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//? if <1.21.11 {
/*
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
*///?} else {
//? if <26.2 {
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
//?} else {
/*
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
*///?}
//?}
//? if >=1.21.11 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
//?}
import net.minecraft.client.multiplayer.ClientLevel;
//? if <1.21.11 {
/*
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
*///?} else {
//? if <26.2 {
import net.minecraft.client.renderer.ShapeRenderer;
//?}
//?}
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public final class BlockOutlineGeometryRenderer {
    private static final double MAX_DISTANCE_SQUARED = 256.0D * 256.0D;
    private static final double MAX_DISTANCE = Math.sqrt(MAX_DISTANCE_SQUARED);
    private static final int MAX_CACHED_GEOMETRIES = 1024;
    private static final long CACHE_IDLE_FRAMES = 120L;
    private static final Map<OutlineStyle, LongOpenHashSet> VISIBLE_POSITIONS_BY_STYLE = new java.util.HashMap<>();
    private static final LongArrayList GROUP = new LongArrayList();
    private static final Long2ObjectMap<List<CachedGeometry>> CACHED_GEOMETRIES = new Long2ObjectOpenHashMap<>();
    private static boolean initialized;
    private static int cachedGeometryCount;
    private static long renderFrame;

    private BlockOutlineGeometryRenderer() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
//? if <1.21.11 {
/*
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(BlockOutlineGeometryRenderer::renderLegacy);
*///?} else {
//? if >=26.2 {
/*
        LevelRenderEvents.COLLECT_SUBMITS.register(BlockOutlineGeometryRenderer::collectSubmits);
*///?} else {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(BlockOutlineGeometryRenderer::render);
//?}
//?}
        initialized = true;
    }

    static void clearCachedGeometry() {
        VISIBLE_POSITIONS_BY_STYLE.clear();
        GROUP.clear();
        CACHED_GEOMETRIES.clear();
        cachedGeometryCount = 0;
        renderFrame = 0L;
    }

    private static Map<OutlineStyle, LongOpenHashSet> selectVisiblePositions(ClientLevel world, Vec3 camera) {
        clearVisibleSelection();
        if (ClientBlockOutlineManager.styles().isEmpty()) {
            return VISIBLE_POSITIONS_BY_STYLE;
        }

        int minChunkX = (int) Math.floor((camera.x - MAX_DISTANCE) / 16.0D);
        int maxChunkX = (int) Math.floor((camera.x + MAX_DISTANCE) / 16.0D);
        int minChunkZ = (int) Math.floor((camera.z - MAX_DISTANCE) / 16.0D);
        int maxChunkZ = (int) Math.floor((camera.z + MAX_DISTANCE) / 16.0D);
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                Long2ObjectMap<OutlineStyle> positions = ClientBlockOutlineManager.stylesInChunk(chunkX, chunkZ);
                if (positions == null || !world.hasChunk(chunkX, chunkZ)) {
                    continue;
                }
                for (Long2ObjectMap.Entry<OutlineStyle> entry : positions.long2ObjectEntrySet()) {
                    long position = entry.getLongKey();
                    double x = BlockPos.getX(position) - camera.x;
                    double y = BlockPos.getY(position) - camera.y;
                    double z = BlockPos.getZ(position) - camera.z;
                    if (x * x + y * y + z * z <= MAX_DISTANCE_SQUARED) {
                        VISIBLE_POSITIONS_BY_STYLE
                                .computeIfAbsent(entry.getValue(), ignored -> new LongOpenHashSet())
                                .add(position);
                    }
                }
            }
        }
        return VISIBLE_POSITIONS_BY_STYLE;
    }

    private static void clearVisibleSelection() {
        Iterator<Map.Entry<OutlineStyle, LongOpenHashSet>> iterator = VISIBLE_POSITIONS_BY_STYLE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<OutlineStyle, LongOpenHashSet> entry = iterator.next();
            if (!ClientBlockOutlineManager.hasStyle(entry.getKey())) {
                iterator.remove();
            } else {
                entry.getValue().clear();
            }
        }
    }

    private static CachedGeometry cachedGeometry(LongArrayList group) {
        long signature = signature(group);
        List<CachedGeometry> candidates = CACHED_GEOMETRIES.get(signature);
        if (candidates != null) {
            for (CachedGeometry candidate : candidates) {
                if (candidate.matches(group)) {
                    candidate.lastUsedFrame = renderFrame;
                    return candidate;
                }
            }
        }

        CachedGeometry geometry = CachedGeometry.create(group, renderFrame);
        if (cachedGeometryCount >= MAX_CACHED_GEOMETRIES) {
            pruneCachedGeometry();
        }
        if (cachedGeometryCount < MAX_CACHED_GEOMETRIES) {
            CACHED_GEOMETRIES.computeIfAbsent(signature, ignored -> new ArrayList<>()).add(geometry);
            cachedGeometryCount++;
        }
        return geometry;
    }

    private static long signature(LongArrayList group) {
        long result = group.size();
        for (int index = 0; index < group.size(); index++) {
            long value = mix(group.getLong(index));
            result += value;
            result ^= Long.rotateLeft(value, 23);
        }
        return result;
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static void pruneCachedGeometry() {
        Iterator<List<CachedGeometry>> buckets = CACHED_GEOMETRIES.values().iterator();
        while (buckets.hasNext()) {
            List<CachedGeometry> geometries = buckets.next();
            for (Iterator<CachedGeometry> iterator = geometries.iterator(); iterator.hasNext(); ) {
                if (renderFrame - iterator.next().lastUsedFrame > CACHE_IDLE_FRAMES) {
                    iterator.remove();
                    cachedGeometryCount--;
                }
            }
            if (geometries.isEmpty()) {
                buckets.remove();
            }
        }
    }

    private static final class CachedGeometry {
        private final LongOpenHashSet positions;
        private final VoxelShape shape;
        private final int originX;
        private final int originY;
        private final int originZ;
        private long lastUsedFrame;

        private CachedGeometry(
                LongOpenHashSet positions,
                VoxelShape shape,
                int originX,
                int originY,
                int originZ,
                long lastUsedFrame
        ) {
            this.positions = positions;
            this.shape = shape;
            this.originX = originX;
            this.originY = originY;
            this.originZ = originZ;
            this.lastUsedFrame = lastUsedFrame;
        }

        static CachedGeometry create(LongArrayList group, long frame) {
            int originX = Integer.MAX_VALUE;
            int originY = Integer.MAX_VALUE;
            int originZ = Integer.MAX_VALUE;
            for (int index = 0; index < group.size(); index++) {
                long position = group.getLong(index);
                originX = Math.min(originX, BlockPos.getX(position));
                originY = Math.min(originY, BlockPos.getY(position));
                originZ = Math.min(originZ, BlockPos.getZ(position));
            }

            VoxelShape shape = Shapes.empty();
            LongOpenHashSet positions = new LongOpenHashSet(group);
            for (int index = 0; index < group.size(); index++) {
                long position = group.getLong(index);
                int x = BlockPos.getX(position) - originX;
                int y = BlockPos.getY(position) - originY;
                int z = BlockPos.getZ(position) - originZ;
                shape = Shapes.or(shape, Shapes.box(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D));
            }
            return new CachedGeometry(positions, shape.optimize(), originX, originY, originZ, frame);
        }

        boolean matches(LongArrayList group) {
            if (positions.size() != group.size()) {
                return false;
            }
            for (int index = 0; index < group.size(); index++) {
                if (!positions.contains(group.getLong(index))) {
                    return false;
                }
            }
            return true;
        }
    }

//? if <1.21.11 {
/*
    private static void renderLegacy(WorldRenderContext context) {
        ClientLevel world = ClientBlockOutlineManager.world();
        if (world == null || ClientBlockOutlineManager.styles().isEmpty()) {
            return;
        }

        Vec3 camera = context.camera().getPosition();
        PoseStack matrices = context.matrixStack();
        VertexConsumer vertices = context.consumers().getBuffer(RenderType.lines());
        renderFrame++;
        Map<OutlineStyle, LongOpenHashSet> selectedPositions = selectVisiblePositions(world, camera);
        for (Map.Entry<OutlineStyle, LongOpenHashSet> entry : selectedPositions.entrySet()) {
            OutlineStyle style = entry.getKey();
            LongOpenHashSet positions = entry.getValue();
            while (!positions.isEmpty()) {
                BlockOutlineGeometryGroups.takeConnectedGroup(positions, GROUP);
                LongArrayList group = GROUP;
                if (group.size() <= BlockOutlineGeometryGroups.MAX_MERGED_BLOCKS_PER_GROUP) {
                    CachedGeometry geometry = group.size() == 1 ? null : cachedGeometry(group);
                    if (geometry == null) {
                        long position = group.getLong(0);
                        renderLegacyShape(
                                matrices,
                                vertices,
                                Shapes.block(),
                                BlockPos.getX(position) - camera.x,
                                BlockPos.getY(position) - camera.y,
                                BlockPos.getZ(position) - camera.z,
                                style
                        );
                        GROUP.clear();
                        continue;
                    }
                    renderLegacyShape(
                            matrices,
                            vertices,
                            geometry.shape,
                            geometry.originX - camera.x,
                            geometry.originY - camera.y,
                            geometry.originZ - camera.z,
                            style
                    );
                } else {
                    for (int index = 0; index < group.size(); index++) {
                        long position = group.getLong(index);
                        renderLegacyShape(
                                matrices,
                                vertices,
                                Shapes.block(),
                                BlockPos.getX(position) - camera.x,
                                BlockPos.getY(position) - camera.y,
                                BlockPos.getZ(position) - camera.z,
                                style
                        );
                    }
                }
                GROUP.clear();
            }
        }
        if ((renderFrame % CACHE_IDLE_FRAMES) == 0L) {
            pruneCachedGeometry();
        }
    }

    private static void renderLegacyShape(
            PoseStack matrices,
            VertexConsumer vertices,
            VoxelShape shape,
            double x,
            double y,
            double z,
            OutlineStyle style
    ) {
        int color = style.rgb();
        LevelRenderer.renderVoxelShape(
                matrices,
                vertices,
                shape,
                x,
                y,
                z,
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F,
                1.0F,
                false
        );
    }
*///?} else {
//? if <26.2 {
    private static void render(WorldRenderContext context) {
        ClientLevel world = ClientBlockOutlineManager.world();
        if (world == null || ClientBlockOutlineManager.styles().isEmpty()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        Vec3 camera = client.gameRenderer.getMainCamera().position();
        PoseStack matrices = context.matrices();
        VertexConsumer vertices = context.consumers().getBuffer(RenderTypes.lines());
        renderFrame++;
        Map<OutlineStyle, LongOpenHashSet> selectedPositions = selectVisiblePositions(world, camera);
        for (Map.Entry<OutlineStyle, LongOpenHashSet> entry : selectedPositions.entrySet()) {
            OutlineStyle style = entry.getKey();
            LongOpenHashSet positions = entry.getValue();
            while (!positions.isEmpty()) {
                BlockOutlineGeometryGroups.takeConnectedGroup(positions, GROUP);
                LongArrayList group = GROUP;
                if (group.size() <= BlockOutlineGeometryGroups.MAX_MERGED_BLOCKS_PER_GROUP) {
                    CachedGeometry geometry = group.size() == 1 ? null : cachedGeometry(group);
                    if (geometry == null) {
                        long position = group.getLong(0);
                        ShapeRenderer.renderShape(
                                matrices,
                                vertices,
                                Shapes.block(),
                                BlockPos.getX(position) - camera.x,
                                BlockPos.getY(position) - camera.y,
                                BlockPos.getZ(position) - camera.z,
                                0xFF000000 | (style.rgb() & 0xFFFFFF),
                                1.0F
                        );
                        GROUP.clear();
                        continue;
                    }
                    ShapeRenderer.renderShape(
                            matrices,
                            vertices,
                            geometry.shape,
                            geometry.originX - camera.x,
                            geometry.originY - camera.y,
                            geometry.originZ - camera.z,
                            0xFF000000 | (style.rgb() & 0xFFFFFF),
                            1.0F
                    );
                } else {
                    for (int index = 0; index < group.size(); index++) {
                        long position = group.getLong(index);
                        ShapeRenderer.renderShape(
                                matrices,
                                vertices,
                                Shapes.block(),
                                BlockPos.getX(position) - camera.x,
                                BlockPos.getY(position) - camera.y,
                                BlockPos.getZ(position) - camera.z,
                                0xFF000000 | (style.rgb() & 0xFFFFFF),
                                1.0F
                        );
                    }
                }
                GROUP.clear();
            }
        }
        if ((renderFrame % CACHE_IDLE_FRAMES) == 0L) {
            pruneCachedGeometry();
        }
    }
//?} else {
/*
    private static void collectSubmits(LevelRenderContext context) {
        ClientLevel world = ClientBlockOutlineManager.world();
        if (world == null) {
            return;
        }

        Long2ObjectMap<OutlineStyle> styles = ClientBlockOutlineManager.styles();
        if (styles.isEmpty()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        Vec3 camera = client.gameRenderer.mainCamera().position();
        PoseStack matrices = context.poseStack();
        renderFrame++;
        Map<OutlineStyle, LongOpenHashSet> selectedPositions = selectVisiblePositions(world, camera);
        for (Map.Entry<OutlineStyle, LongOpenHashSet> entry : selectedPositions.entrySet()) {
            OutlineStyle style = entry.getKey();
            LongOpenHashSet positions = entry.getValue();
            while (!positions.isEmpty()) {
                BlockOutlineGeometryGroups.takeConnectedGroup(positions, GROUP);
                LongArrayList group = GROUP;
                if (group.size() <= BlockOutlineGeometryGroups.MAX_MERGED_BLOCKS_PER_GROUP) {
                    CachedGeometry geometry = group.size() == 1 ? null : cachedGeometry(group);
                    if (geometry == null) {
                        long position = group.getLong(0);
                        submitBlockPositionOutline(
                                context,
                                matrices,
                                Shapes.block(),
                                BlockPos.getX(position) - camera.x,
                                BlockPos.getY(position) - camera.y,
                                BlockPos.getZ(position) - camera.z,
                                style
                        );
                        GROUP.clear();
                        continue;
                    }
                    submitBlockPositionOutline(
                            context,
                            matrices,
                            geometry.shape,
                            geometry.originX - camera.x,
                            geometry.originY - camera.y,
                            geometry.originZ - camera.z,
                            style
                    );
                } else {
                    for (int index = 0; index < group.size(); index++) {
                        long position = group.getLong(index);
                        submitBlockPositionOutline(
                                context,
                                matrices,
                                Shapes.block(),
                                BlockPos.getX(position) - camera.x,
                                BlockPos.getY(position) - camera.y,
                                BlockPos.getZ(position) - camera.z,
                                style
                        );
                    }
                }
                GROUP.clear();
            }
        }
        if ((renderFrame % CACHE_IDLE_FRAMES) == 0L) {
            pruneCachedGeometry();
        }

        Long2ObjectMap<OutlineStyle> localBlockEntityStyles = ClientOutlineManager.localBlockEntityStyles();
        for (Long2ObjectMap.Entry<OutlineStyle> entry : ClientOutlineManager.syncedBlockEntityStyles().long2ObjectEntrySet()) {
            if (!localBlockEntityStyles.containsKey(entry.getLongKey())) {
                submitBlockEntityOutline(context, world, camera, matrices, entry);
            }
        }
        for (Long2ObjectMap.Entry<OutlineStyle> entry : localBlockEntityStyles.long2ObjectEntrySet()) {
            submitBlockEntityOutline(context, world, camera, matrices, entry);
        }
    }

    private static void submitBlockPositionOutline(
            LevelRenderContext context,
            PoseStack matrices,
            VoxelShape shape,
            double x,
            double y,
            double z,
            OutlineStyle style
    ) {
        matrices.pushPose();
        matrices.translate(x, y, z);
        context.submitNodeCollector().submitShapeOutline(
                matrices,
                shape,
                RenderTypes.lines(),
                0xFF000000 | (style.rgb() & 0xFFFFFF),
                1.0F,
                false
        );
        matrices.popPose();
    }

    private static void submitBlockEntityOutline(
            LevelRenderContext context,
            ClientLevel world,
            Vec3 camera,
            PoseStack matrices,
            Long2ObjectMap.Entry<OutlineStyle> entry
    ) {
        BlockPos position = BlockPos.of(entry.getLongKey());
        if (!world.hasChunk(position.getX() >> 4, position.getZ() >> 4)
                || position.distToLowCornerSqr(camera.x, camera.y, camera.z) > MAX_DISTANCE_SQUARED) {
            return;
        }

        matrices.pushPose();
        matrices.translate(position.getX() - camera.x, position.getY() - camera.y, position.getZ() - camera.z);
        OutlineStyle style = entry.getValue();
        context.submitNodeCollector().submitShapeOutline(
                matrices,
                Shapes.block(),
                RenderTypes.lines(),
                0xFF000000 | (style.rgb() & 0xFFFFFF),
                style.thickness(),
                false
        );
        matrices.popPose();
    }
*///?}
//?}
}
//?} else {
/*
public final class BlockOutlineGeometryRenderer {
    private BlockOutlineGeometryRenderer() {
    }

    public static void initialize() {
    }
}
*///?}
