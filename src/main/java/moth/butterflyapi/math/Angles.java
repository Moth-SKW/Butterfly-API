package moth.butterflyapi.math;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public final class Angles {
    public static final double TAU = Math.PI * 2.0D;

    private Angles() {
    }

    public static double radians(double degrees) {
        return Math.toRadians(degrees);
    }

    public static double degrees(double radians) {
        return Math.toDegrees(radians);
    }

    public static float wrapDegrees(float degrees) {
        return MathHelper.wrapDegrees(degrees);
    }

    public static double wrapDegrees(double degrees) {
        return MathHelper.wrapDegrees(degrees);
    }

    public static double wrapRadians(double radians) {
        double wrapped = radians % TAU;
        if (wrapped >= Math.PI) {
            wrapped -= TAU;
        }
        if (wrapped < -Math.PI) {
            wrapped += TAU;
        }
        return wrapped;
    }

    public static double deltaDegrees(double from, double to) {
        return wrapDegrees(to - from);
    }

    public static double deltaRadians(double from, double to) {
        return wrapRadians(to - from);
    }

    public static double lerpDegrees(double delta, double start, double end) {
        return start + deltaDegrees(start, end) * delta;
    }

    public static float lerpDegrees(float delta, float start, float end) {
        return start + (float) deltaDegrees(start, end) * delta;
    }

    public static Vec3d direction(double yawDegrees, double pitchDegrees) {
        return Vec3d.fromPolar((float) pitchDegrees, (float) yawDegrees);
    }

    public static Vec3d direction(YawPitch rotation) {
        return Objects.requireNonNull(rotation, "rotation").direction();
    }

    public static double yaw(Vec3d direction) {
        Vec3d normalized = Vecs.safeNormalize(Objects.requireNonNull(direction, "direction"), new Vec3d(0.0D, 0.0D, 1.0D));
        return Math.toDegrees(Math.atan2(-normalized.x, normalized.z));
    }

    public static double pitch(Vec3d direction) {
        Vec3d normalized = Vecs.safeNormalize(Objects.requireNonNull(direction, "direction"));
        double horizontalLength = Math.sqrt(normalized.x * normalized.x + normalized.z * normalized.z);
        return Math.toDegrees(Math.atan2(-normalized.y, horizontalLength));
    }

    public static YawPitch yawPitch(Vec3d direction) {
        return YawPitch.of(yaw(direction), pitch(direction));
    }

    public static YawPitch lookAt(Vec3d from, Vec3d to) {
        return yawPitch(Objects.requireNonNull(to, "to").subtract(Objects.requireNonNull(from, "from")));
    }
}
