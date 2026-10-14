package moth.butterflyapi.math;

import java.util.Objects;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class Vecs {
    private static final double EPSILON_SQUARED = Scalars.EPSILON * Scalars.EPSILON;

    private Vecs() {
    }

    public static Vec3 safeNormalize(Vec3 vector) {
        return safeNormalize(vector, Vec3.ZERO);
    }

    public static Vec3 safeNormalize(Vec3 vector, Vec3 fallback) {
        Objects.requireNonNull(vector, "vector");
        Objects.requireNonNull(fallback, "fallback");
        double lengthSquared = lengthSquared(vector);
        if (lengthSquared <= EPSILON_SQUARED) {
            return fallback;
        }
        return vector.scale(1.0D / Math.sqrt(lengthSquared));
    }

    public static Vec3 horizontal(Vec3 vector) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3(vector.x, 0.0D, vector.z);
    }

    public static Vec3 withX(Vec3 vector, double x) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3(x, vector.y, vector.z);
    }

    public static Vec3 withY(Vec3 vector, double y) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3(vector.x, y, vector.z);
    }

    public static Vec3 withZ(Vec3 vector, double z) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3(vector.x, vector.y, z);
    }

    public static Vec3 addX(Vec3 vector, double x) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3(vector.x + x, vector.y, vector.z);
    }

    public static Vec3 addY(Vec3 vector, double y) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3(vector.x, vector.y + y, vector.z);
    }

    public static Vec3 addZ(Vec3 vector, double z) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3(vector.x, vector.y, vector.z + z);
    }

    public static Vec3 scaleX(Vec3 vector, double scale) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3(vector.x * scale, vector.y, vector.z);
    }

    public static Vec3 scaleY(Vec3 vector, double scale) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3(vector.x, vector.y * scale, vector.z);
    }

    public static Vec3 scaleZ(Vec3 vector, double scale) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3(vector.x, vector.y, vector.z * scale);
    }

    public static Vec3 midpoint(Vec3 a, Vec3 b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        return new Vec3(
                (a.x + b.x) * 0.5D,
                (a.y + b.y) * 0.5D,
                (a.z + b.z) * 0.5D
        );
    }

    public static Vec3 lerp(Vec3 start, Vec3 end, double delta) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        return new Vec3(
                Scalars.lerp(delta, start.x, end.x),
                Scalars.lerp(delta, start.y, end.y),
                Scalars.lerp(delta, start.z, end.z)
        );
    }

    public static Vec3 toward(Vec3 from, Vec3 to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        return to.subtract(from);
    }

    public static Vec3 direction(Vec3 from, Vec3 to) {
        return direction(from, to, Vec3.ZERO);
    }

    public static Vec3 direction(Vec3 from, Vec3 to, Vec3 fallback) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        return safeNormalize(to.subtract(from), fallback);
    }

    public static Vec3 setLength(Vec3 vector, double length) {
        Objects.requireNonNull(vector, "vector");
        if (Scalars.isZero(length)) {
            return Vec3.ZERO;
        }
        return safeNormalize(vector).scale(length);
    }

    public static Vec3 limitLength(Vec3 vector, double maxLength) {
        Objects.requireNonNull(vector, "vector");
        if (maxLength < 0.0D) {
            throw new IllegalArgumentException("maxLength must be >= 0");
        }
        double lengthSquared = lengthSquared(vector);
        if (lengthSquared <= maxLength * maxLength) {
            return vector;
        }
        return setLength(vector, maxLength);
    }

    public static Vec3 clampLength(Vec3 vector, double minLength, double maxLength) {
        Objects.requireNonNull(vector, "vector");
        if (minLength < 0.0D || maxLength < minLength) {
            throw new IllegalArgumentException("Expected 0 <= minLength <= maxLength");
        }
        double lengthSquared = lengthSquared(vector);
        if (lengthSquared <= EPSILON_SQUARED) {
            return Vec3.ZERO;
        }

        double length = Math.sqrt(lengthSquared);
        if (length < minLength) {
            return vector.scale(minLength / length);
        }
        if (length > maxLength) {
            return vector.scale(maxLength / length);
        }
        return vector;
    }

    public static Vec3 projectOnto(Vec3 vector, Vec3 onto) {
        Objects.requireNonNull(vector, "vector");
        Objects.requireNonNull(onto, "onto");
        double ontoLengthSquared = lengthSquared(onto);
        if (ontoLengthSquared <= EPSILON_SQUARED) {
            return Vec3.ZERO;
        }
        return onto.scale(vector.dot(onto) / ontoLengthSquared);
    }

    public static Vec3 rejectFrom(Vec3 vector, Vec3 onto) {
        Objects.requireNonNull(vector, "vector");
        return vector.subtract(projectOnto(vector, onto));
    }

    public static Vec3 reflect(Vec3 incoming, Vec3 normal) {
        Objects.requireNonNull(incoming, "incoming");
        Vec3 unitNormal = safeNormalize(Objects.requireNonNull(normal, "normal"));
        return incoming.subtract(unitNormal.scale(2.0D * incoming.dot(unitNormal)));
    }

    public static Vec3 perpendicular(Vec3 vector) {
        Vec3 normalized = safeNormalize(Objects.requireNonNull(vector, "vector"));
        if (lengthSquared(normalized) <= EPSILON_SQUARED) {
            return Vec3.ZERO;
        }

        double absX = Math.abs(normalized.x);
        double absY = Math.abs(normalized.y);
        double absZ = Math.abs(normalized.z);
        Vec3 axis = absX <= absY && absX <= absZ
                ? new Vec3(1.0D, 0.0D, 0.0D)
                : absY <= absZ
                ? new Vec3(0.0D, 1.0D, 0.0D)
                : new Vec3(0.0D, 0.0D, 1.0D);
        return safeNormalize(axis.cross(normalized));
    }

    public static Vec3 closestPointOnSegment(Vec3 point, Vec3 start, Vec3 end) {
        Objects.requireNonNull(point, "point");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");

        Vec3 segment = end.subtract(start);
        double segmentLengthSquared = lengthSquared(segment);
        if (segmentLengthSquared <= EPSILON_SQUARED) {
            return start;
        }

        double t = Mth.clamp(point.subtract(start).dot(segment) / segmentLengthSquared, 0.0D, 1.0D);
        return start.add(segment.scale(t));
    }

    public static double distanceSqToSegment(Vec3 point, Vec3 start, Vec3 end) {
        Vec3 closestPoint = closestPointOnSegment(point, start, end);
        return lengthSquared(point.subtract(closestPoint));
    }

    public static double distanceToSegment(Vec3 point, Vec3 start, Vec3 end) {
        return Math.sqrt(distanceSqToSegment(point, start, end));
    }

    public static double lengthSquared(Vec3 vector) {
        Objects.requireNonNull(vector, "vector");
        return vector.x * vector.x + vector.y * vector.y + vector.z * vector.z;
    }
}
