package moth.butterflyapi.math;

import net.minecraft.util.math.Vec3d;

public record YawPitch(float yaw, float pitch) {
    public static YawPitch of(double yaw, double pitch) {
        return new YawPitch((float) yaw, (float) pitch);
    }

    public static YawPitch fromDirection(Vec3d direction) {
        return Angles.yawPitch(direction);
    }

    public Vec3d direction() {
        return Vec3d.fromPolar(pitch, yaw);
    }

    public YawPitch add(double yawOffset, double pitchOffset) {
        return new YawPitch((float) (yaw + yawOffset), (float) (pitch + pitchOffset));
    }

    public YawPitch withYaw(double newYaw) {
        return new YawPitch((float) newYaw, pitch);
    }

    public YawPitch withPitch(double newPitch) {
        return new YawPitch(yaw, (float) newPitch);
    }
}
