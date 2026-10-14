package moth.butterflyapi.math;

import java.util.Objects;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

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
        return Mth.wrapDegrees(degrees);
    }

    public static double wrapDegrees(double degrees) {
        return Mth.wrapDegrees(degrees);
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

    public static Vec3 direction(double yawDegrees, double pitchDegrees) {
        return Vec3.directionFromRotation((float) pitchDegrees, (float) yawDegrees);
    }

    public static Vec3 lookDirection(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        return direction(entity.getYRot(), entity.getXRot());
    }

    public static Vec3 direction(YawPitch rotation) {
        return Objects.requireNonNull(rotation, "rotation").direction();
    }

    public static double yaw(Vec3 direction) {
        Vec3 normalized = Vecs.safeNormalize(Objects.requireNonNull(direction, "direction"), new Vec3(0.0D, 0.0D, 1.0D));
        return Math.toDegrees(Math.atan2(-normalized.x, normalized.z));
    }

    public static double pitch(Vec3 direction) {
        Vec3 normalized = Vecs.safeNormalize(Objects.requireNonNull(direction, "direction"));
        double horizontalLength = Math.sqrt(normalized.x * normalized.x + normalized.z * normalized.z);
        return Math.toDegrees(Math.atan2(-normalized.y, horizontalLength));
    }

    public static YawPitch yawPitch(Vec3 direction) {
        return YawPitch.of(yaw(direction), pitch(direction));
    }

    public static YawPitch lookAt(Vec3 from, Vec3 to) {
        return yawPitch(Objects.requireNonNull(to, "to").subtract(Objects.requireNonNull(from, "from")));
    }

    public static YawPitch lookAt(Entity from, Vec3 to) {
        Objects.requireNonNull(from, "from");
        return lookAt(new Vec3(from.getX(), from.getY(), from.getZ()), to);
    }

    public static YawPitch lookAt(Entity from, Entity to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        return lookAt(
                new Vec3(from.getX(), from.getY(), from.getZ()),
                new Vec3(to.getX(), to.getY(), to.getZ())
        );
    }
}
