package moth.butterflyapi.math;

import net.minecraft.world.phys.Vec3;

public record YawPitch(float yaw, float pitch) {
    public static YawPitch of(double yaw, double pitch) {
        return new YawPitch((float) yaw, (float) pitch);
    }

    public static YawPitch fromDirection(Vec3 direction) {
        return Angles.yawPitch(direction);
    }

    public Vec3 direction() {
        return Vec3.directionFromRotation(pitch, yaw);
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
