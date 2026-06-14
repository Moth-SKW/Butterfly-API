package moth.butterflyapi.math;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public final class Vecs {
    private static final double EPSILON_SQUARED = Scalars.EPSILON * Scalars.EPSILON;

    private Vecs() {
    }

    public static Vec3d safeNormalize(Vec3d vector) {
        return safeNormalize(vector, Vec3d.ZERO);
    }

    public static Vec3d safeNormalize(Vec3d vector, Vec3d fallback) {
        Objects.requireNonNull(vector, "vector");
        Objects.requireNonNull(fallback, "fallback");
        double lengthSquared = lengthSquared(vector);
        if (lengthSquared <= EPSILON_SQUARED) {
            return fallback;
        }
        return vector.multiply(1.0D / Math.sqrt(lengthSquared));
    }

    public static Vec3d horizontal(Vec3d vector) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3d(vector.x, 0.0D, vector.z);
    }

    public static Vec3d withX(Vec3d vector, double x) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3d(x, vector.y, vector.z);
    }

    public static Vec3d withY(Vec3d vector, double y) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3d(vector.x, y, vector.z);
    }

    public static Vec3d withZ(Vec3d vector, double z) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3d(vector.x, vector.y, z);
    }

    public static Vec3d addX(Vec3d vector, double x) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3d(vector.x + x, vector.y, vector.z);
    }

    public static Vec3d addY(Vec3d vector, double y) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3d(vector.x, vector.y + y, vector.z);
    }

    public static Vec3d addZ(Vec3d vector, double z) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3d(vector.x, vector.y, vector.z + z);
    }

    public static Vec3d scaleX(Vec3d vector, double scale) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3d(vector.x * scale, vector.y, vector.z);
    }

    public static Vec3d scaleY(Vec3d vector, double scale) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3d(vector.x, vector.y * scale, vector.z);
    }

    public static Vec3d scaleZ(Vec3d vector, double scale) {
        Objects.requireNonNull(vector, "vector");
        return new Vec3d(vector.x, vector.y, vector.z * scale);
    }

    public static Vec3d midpoint(Vec3d a, Vec3d b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        return new Vec3d(
                (a.x + b.x) * 0.5D,
                (a.y + b.y) * 0.5D,
                (a.z + b.z) * 0.5D
        );
    }

    public static Vec3d lerp(Vec3d start, Vec3d end, double delta) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        return new Vec3d(
                Scalars.lerp(delta, start.x, end.x),
                Scalars.lerp(delta, start.y, end.y),
                Scalars.lerp(delta, start.z, end.z)
        );
    }

    public static Vec3d toward(Vec3d from, Vec3d to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        return to.subtract(from);
    }

    public static Vec3d direction(Vec3d from, Vec3d to) {
        return direction(from, to, Vec3d.ZERO);
    }

    public static Vec3d direction(Vec3d from, Vec3d to, Vec3d fallback) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        return safeNormalize(to.subtract(from), fallback);
    }

    public static Vec3d setLength(Vec3d vector, double length) {
        Objects.requireNonNull(vector, "vector");
        if (Scalars.isZero(length)) {
            return Vec3d.ZERO;
        }
        return safeNormalize(vector).multiply(length);
    }

    public static Vec3d limitLength(Vec3d vector, double maxLength) {
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

    public static Vec3d clampLength(Vec3d vector, double minLength, double maxLength) {
        Objects.requireNonNull(vector, "vector");
        if (minLength < 0.0D || maxLength < minLength) {
            throw new IllegalArgumentException("Expected 0 <= minLength <= maxLength");
        }
        double lengthSquared = lengthSquared(vector);
        if (lengthSquared <= EPSILON_SQUARED) {
            return Vec3d.ZERO;
        }

        double length = Math.sqrt(lengthSquared);
        if (length < minLength) {
            return vector.multiply(minLength / length);
        }
        if (length > maxLength) {
            return vector.multiply(maxLength / length);
        }
        return vector;
    }

    public static Vec3d projectOnto(Vec3d vector, Vec3d onto) {
        Objects.requireNonNull(vector, "vector");
        Objects.requireNonNull(onto, "onto");
        double ontoLengthSquared = lengthSquared(onto);
        if (ontoLengthSquared <= EPSILON_SQUARED) {
            return Vec3d.ZERO;
        }
        return onto.multiply(vector.dotProduct(onto) / ontoLengthSquared);
    }

    public static Vec3d rejectFrom(Vec3d vector, Vec3d onto) {
        Objects.requireNonNull(vector, "vector");
        return vector.subtract(projectOnto(vector, onto));
    }

    public static Vec3d reflect(Vec3d incoming, Vec3d normal) {
        Objects.requireNonNull(incoming, "incoming");
        Vec3d unitNormal = safeNormalize(Objects.requireNonNull(normal, "normal"));
        return incoming.subtract(unitNormal.multiply(2.0D * incoming.dotProduct(unitNormal)));
    }

    public static Vec3d perpendicular(Vec3d vector) {
        Vec3d normalized = safeNormalize(Objects.requireNonNull(vector, "vector"));
        if (lengthSquared(normalized) <= EPSILON_SQUARED) {
            return Vec3d.ZERO;
        }

        double absX = Math.abs(normalized.x);
        double absY = Math.abs(normalized.y);
        double absZ = Math.abs(normalized.z);
        Vec3d axis = absX <= absY && absX <= absZ
                ? new Vec3d(1.0D, 0.0D, 0.0D)
                : absY <= absZ
                ? new Vec3d(0.0D, 1.0D, 0.0D)
                : new Vec3d(0.0D, 0.0D, 1.0D);
        return safeNormalize(axis.crossProduct(normalized));
    }

    public static Vec3d closestPointOnSegment(Vec3d point, Vec3d start, Vec3d end) {
        Objects.requireNonNull(point, "point");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");

        Vec3d segment = end.subtract(start);
        double segmentLengthSquared = lengthSquared(segment);
        if (segmentLengthSquared <= EPSILON_SQUARED) {
            return start;
        }

        double t = MathHelper.clamp(point.subtract(start).dotProduct(segment) / segmentLengthSquared, 0.0D, 1.0D);
        return start.add(segment.multiply(t));
    }

    public static double distanceSqToSegment(Vec3d point, Vec3d start, Vec3d end) {
        Vec3d closestPoint = closestPointOnSegment(point, start, end);
        return lengthSquared(point.subtract(closestPoint));
    }

    public static double distanceToSegment(Vec3d point, Vec3d start, Vec3d end) {
        return Math.sqrt(distanceSqToSegment(point, start, end));
    }

    public static double lengthSquared(Vec3d vector) {
        Objects.requireNonNull(vector, "vector");
        return vector.x * vector.x + vector.y * vector.y + vector.z * vector.z;
    }
}
