package moth.butterflyapi.math;

import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public record Basis3(Vec3d forward, Vec3d right, Vec3d up) {
    public Basis3 {
        Objects.requireNonNull(forward, "forward");
        Objects.requireNonNull(right, "right");
        Objects.requireNonNull(up, "up");
    }

    public static Basis3 fromForward(Vec3d forward) {
        Vec3d safeForward = Vecs.safeNormalize(Objects.requireNonNull(forward, "forward"), new Vec3d(0.0D, 0.0D, 1.0D));
        Vec3d seed = Math.abs(safeForward.y) > 0.999D
                ? new Vec3d(1.0D, 0.0D, 0.0D)
                : new Vec3d(0.0D, 1.0D, 0.0D);
        Vec3d right = Vecs.safeNormalize(seed.crossProduct(safeForward), new Vec3d(1.0D, 0.0D, 0.0D));
        Vec3d up = Vecs.safeNormalize(safeForward.crossProduct(right), new Vec3d(0.0D, 1.0D, 0.0D));
        return new Basis3(safeForward, right, up);
    }

    public Vec3d offset(double forwardDistance, double rightDistance, double upDistance) {
        return forward.multiply(forwardDistance)
                .add(right.multiply(rightDistance))
                .add(up.multiply(upDistance));
    }

    public Vec3d radial(double angleRadians, double radius) {
        return right.multiply(Math.cos(angleRadians) * radius)
                .add(up.multiply(Math.sin(angleRadians) * radius));
    }

    public Vec3d radialDegrees(double angleDegrees, double radius) {
        return radial(Math.toRadians(angleDegrees), radius);
    }
}
