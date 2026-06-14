package moth.butterflyapi.math;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Sampling {
    private Sampling() {
    }

    public static Vec3d ringPoint(Vec3d center, Vec3d normal, double radius, double angleRadians) {
        Objects.requireNonNull(center, "center");
        Basis3 basis = Basis3.fromForward(Objects.requireNonNull(normal, "normal"));
        return center.add(basis.radial(angleRadians, radius));
    }

    public static Vec3d spreadYawPitch(Vec3d forward, double yawDegrees, double pitchDegrees) {
        return spreadYawPitchRadians(forward, Math.toRadians(yawDegrees), Math.toRadians(pitchDegrees));
    }

    public static Vec3d spreadYawPitchRadians(Vec3d forward, double yawRadians, double pitchRadians) {
        Basis3 basis = Basis3.fromForward(Objects.requireNonNull(forward, "forward"));
        double cosPitch = Math.cos(pitchRadians);
        Vec3d spread = basis.forward().multiply(cosPitch * Math.cos(yawRadians))
                .add(basis.right().multiply(cosPitch * Math.sin(yawRadians)))
                .add(basis.up().multiply(-Math.sin(pitchRadians)));
        return Vecs.safeNormalize(spread, basis.forward());
    }

    public static List<Vec3d> circle(Vec3d center, Vec3d normal, double radius, int points) {
        Objects.requireNonNull(center, "center");
        Basis3 basis = Basis3.fromForward(Objects.requireNonNull(normal, "normal"));
        validatePoints(points);

        List<Vec3d> result = new ArrayList<>(points);
        double step = Angles.TAU / points;
        for (int i = 0; i < points; i++) {
            result.add(center.add(basis.radial(step * i, radius)));
        }
        return result;
    }

    public static List<Vec3d> arc(Vec3d center, Vec3d normal, double radius, int points, double startRadians, double endRadians) {
        Objects.requireNonNull(center, "center");
        Basis3 basis = Basis3.fromForward(Objects.requireNonNull(normal, "normal"));
        validatePoints(points);

        if (points == 1) {
            return List.of(center.add(basis.radial(startRadians, radius)));
        }

        List<Vec3d> result = new ArrayList<>(points);
        double step = (endRadians - startRadians) / (points - 1);
        for (int i = 0; i < points; i++) {
            result.add(center.add(basis.radial(startRadians + step * i, radius)));
        }
        return result;
    }

    private static void validatePoints(int points) {
        if (points <= 0) {
            throw new IllegalArgumentException("points must be > 0");
        }
    }
}
