package moth.butterflyapi.client.outline.internal;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a selected position set into face-connected components before a voxel
 * union is created. This prevents distant selections from creating an oversized
 * sparse voxel grid.
 */
final class BlockOutlineGeometryGroups {
    static final int MAX_MERGED_BLOCKS_PER_GROUP = 128;

    private BlockOutlineGeometryGroups() {
    }

    static List<LongArrayList> connectedGroups(LongOpenHashSet positions) {
        List<LongArrayList> groups = new ArrayList<>();
        while (!positions.isEmpty()) {
            LongArrayList group = new LongArrayList();
            takeConnectedGroup(positions, group);
            groups.add(group);
        }
        return groups;
    }

    static void takeConnectedGroup(LongOpenHashSet positions, LongArrayList result) {
        result.clear();
        long seed = positions.iterator().nextLong();
        result.add(seed);
        positions.remove(seed);

        for (int index = 0; index < result.size(); index++) {
            BlockPos position = BlockPos.of(result.getLong(index));
            for (Direction direction : Direction.values()) {
                long neighbor = position.relative(direction).asLong();
                if (positions.remove(neighbor)) {
                    result.add(neighbor);
                }
            }
        }
    }

    static VoxelShape mergedShape(LongArrayList group, Vec3 camera) {
        VoxelShape result = null;
        for (int index = 0; index < group.size(); index++) {
            BlockPos position = BlockPos.of(group.getLong(index));
            double x = position.getX() - camera.x;
            double y = position.getY() - camera.y;
            double z = position.getZ() - camera.z;
            VoxelShape cube = Shapes.box(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D);
            result = result == null ? cube : Shapes.or(result, cube);
        }
        return result == null ? Shapes.empty() : result.optimize();
    }
}
