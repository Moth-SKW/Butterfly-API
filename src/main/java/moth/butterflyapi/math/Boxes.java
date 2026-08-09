package moth.butterflyapi.math;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public final class Boxes {
    private Boxes() {
    }

    public static Box around(Vec3d center, double radius) {
        return around(center, radius, radius, radius);
    }

    public static Box around(Vec3d center, double xRadius, double yRadius, double zRadius) {
        Objects.requireNonNull(center, "center");
        return new Box(
                center.x - xRadius,
                center.y - yRadius,
                center.z - zRadius,
                center.x + xRadius,
                center.y + yRadius,
                center.z + zRadius
        );
    }

    public static Box sized(Vec3d center, double width, double height, double depth) {
        return around(center, width * 0.5D, height * 0.5D, depth * 0.5D);
    }

    public static Box between(Vec3d start, Vec3d end) {
        return between(start, end, 0.0D);
    }

    public static Box between(Vec3d start, Vec3d end, double padding) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        return new Box(
                Math.min(start.x, end.x),
                Math.min(start.y, end.y),
                Math.min(start.z, end.z),
                Math.max(start.x, end.x),
                Math.max(start.y, end.y),
                Math.max(start.z, end.z)
        ).expand(padding);
    }

    public static Box at(BlockPos pos) {
        return new Box(Objects.requireNonNull(pos, "pos"));
    }

    public static Box at(BlockPos pos, double padding) {
        return at(pos).expand(padding);
    }

    public static Box of(Entity entity) {
        return Objects.requireNonNull(entity, "entity").getBoundingBox();
    }

    public static Box of(Entity entity, double padding) {
        return of(entity).expand(padding);
    }

    public static Vec3d center(Box box) {
        return Objects.requireNonNull(box, "box").getCenter();
    }

    public static Vec3d size(Box box) {
        Objects.requireNonNull(box, "box");
        return new Vec3d(box.maxX - box.minX, box.maxY - box.minY, box.maxZ - box.minZ);
    }
}
