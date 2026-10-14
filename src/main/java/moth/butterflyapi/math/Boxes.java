package moth.butterflyapi.math;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class Boxes {
    private Boxes() {
    }

    public static AABB around(Vec3 center, double radius) {
        return around(center, radius, radius, radius);
    }

    public static AABB around(Vec3 center, double xRadius, double yRadius, double zRadius) {
        Objects.requireNonNull(center, "center");
        return new AABB(
                center.x - xRadius,
                center.y - yRadius,
                center.z - zRadius,
                center.x + xRadius,
                center.y + yRadius,
                center.z + zRadius
        );
    }

    public static AABB sized(Vec3 center, double width, double height, double depth) {
        return around(center, width * 0.5D, height * 0.5D, depth * 0.5D);
    }

    public static AABB between(Vec3 start, Vec3 end) {
        return between(start, end, 0.0D);
    }

    public static AABB between(Vec3 start, Vec3 end, double padding) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        return new AABB(
                Math.min(start.x, end.x),
                Math.min(start.y, end.y),
                Math.min(start.z, end.z),
                Math.max(start.x, end.x),
                Math.max(start.y, end.y),
                Math.max(start.z, end.z)
        ).inflate(padding);
    }

    public static AABB at(BlockPos pos) {
        return new AABB(Objects.requireNonNull(pos, "pos"));
    }

    public static AABB at(BlockPos pos, double padding) {
        return at(pos).inflate(padding);
    }

    public static AABB of(Entity entity) {
        return Objects.requireNonNull(entity, "entity").getBoundingBox();
    }

    public static AABB of(Entity entity, double padding) {
        return of(entity).inflate(padding);
    }

    public static Vec3 center(AABB box) {
        return Objects.requireNonNull(box, "box").getCenter();
    }

    public static Vec3 size(AABB box) {
        Objects.requireNonNull(box, "box");
        return new Vec3(box.maxX - box.minX, box.maxY - box.minY, box.maxZ - box.minZ);
    }
}
