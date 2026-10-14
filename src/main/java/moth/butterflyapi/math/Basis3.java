package moth.butterflyapi.math;

import java.util.Objects;
import net.minecraft.world.phys.Vec3;

public record Basis3(Vec3 forward, Vec3 right, Vec3 up) {
    public Basis3 {
        Objects.requireNonNull(forward, "forward");
        Objects.requireNonNull(right, "right");
        Objects.requireNonNull(up, "up");
    }

    public static Basis3 fromForward(Vec3 forward) {
        Vec3 safeForward = Vecs.safeNormalize(Objects.requireNonNull(forward, "forward"), new Vec3(0.0D, 0.0D, 1.0D));
        Vec3 seed = Math.abs(safeForward.y) > 0.999D
                ? new Vec3(1.0D, 0.0D, 0.0D)
                : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = Vecs.safeNormalize(seed.cross(safeForward), new Vec3(1.0D, 0.0D, 0.0D));
        Vec3 up = Vecs.safeNormalize(safeForward.cross(right), new Vec3(0.0D, 1.0D, 0.0D));
        return new Basis3(safeForward, right, up);
    }

    public Vec3 offset(double forwardDistance, double rightDistance, double upDistance) {
        return forward.scale(forwardDistance)
                .add(right.scale(rightDistance))
                .add(up.scale(upDistance));
    }

    public Vec3 radial(double angleRadians, double radius) {
        return right.scale(Math.cos(angleRadians) * radius)
                .add(up.scale(Math.sin(angleRadians) * radius));
    }

    public Vec3 radialDegrees(double angleDegrees, double radius) {
        return radial(Math.toRadians(angleDegrees), radius);
    }
}
