package moth.butterflyapi.client.outline.internal;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Package-local renderer checks invoked by the dependency-free logic runner. */
public final class BlockOutlineGeometryGroupsTestSupport {
    private BlockOutlineGeometryGroupsTestSupport() {
    }

    public static void verify() {
        LongOpenHashSet positions = new LongOpenHashSet();
        positions.add(BlockPos.ZERO.asLong());
        positions.add(new BlockPos(1, 0, 0).asLong());
        positions.add(new BlockPos(4, 0, 0).asLong());

        List<LongArrayList> groups = BlockOutlineGeometryGroups.connectedGroups(positions);
        require(groups.size() == 2, "disconnected block-outline selections must not share a voxel union");
        LongArrayList connected = groups.stream()
                .filter(group -> group.size() == 2)
                .findFirst()
                .orElseThrow(() -> new AssertionError("expected a two-block connected group"));
        require(
                BlockOutlineGeometryGroups.mergedShape(connected, Vec3.ZERO).toAabbs().size() == 1,
                "adjacent block-outline cubes should merge into one exterior shape"
        );

        LongOpenHashSet oversized = new LongOpenHashSet();
        for (int x = 0; x <= BlockOutlineGeometryGroups.MAX_MERGED_BLOCKS_PER_GROUP; x++) {
            oversized.add(new BlockPos(x, 0, 0).asLong());
        }
        List<LongArrayList> oversizedGroups = BlockOutlineGeometryGroups.connectedGroups(oversized);
        require(oversizedGroups.size() == 1, "a contiguous oversized selection must remain one group");
        require(
                oversizedGroups.getFirst().size() > BlockOutlineGeometryGroups.MAX_MERGED_BLOCKS_PER_GROUP,
                "oversized groups must select the bounded fallback path"
        );
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
