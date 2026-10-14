package moth.butterflyapi.particle;

import java.util.Objects;
import java.util.random.RandomGenerator;

public record ParticleEmitterShape(Type type, ParticleVector extent) {
    public enum Type {
        POINT,
        BOX,
        SPHERE
    }

    public ParticleEmitterShape {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(extent, "extent");
        if (extent.x() < 0.0D || extent.y() < 0.0D || extent.z() < 0.0D) {
            throw new IllegalArgumentException("Particle emission extents cannot be negative");
        }
        if (type == Type.POINT && !extent.equals(ParticleVector.ZERO)) {
            throw new IllegalArgumentException("Point emitters cannot define an extent");
        }
        if (type == Type.SPHERE && (extent.x() != extent.y() || extent.x() != extent.z())) {
            throw new IllegalArgumentException("Sphere emitters require a uniform radius");
        }
    }

    public static ParticleEmitterShape point() {
        return new ParticleEmitterShape(Type.POINT, ParticleVector.ZERO);
    }

    public static ParticleEmitterShape box(ParticleVector halfExtent) {
        return new ParticleEmitterShape(Type.BOX, halfExtent);
    }

    public static ParticleEmitterShape sphere(double radius) {
        if (!Double.isFinite(radius) || radius < 0.0D) {
            throw new IllegalArgumentException("Particle sphere radius must be finite and non-negative");
        }
        return new ParticleEmitterShape(Type.SPHERE, new ParticleVector(radius, radius, radius));
    }

    public ParticleVector sample(RandomGenerator random) {
        Objects.requireNonNull(random, "random");
        return switch (type) {
            case POINT -> ParticleVector.ZERO;
            case BOX -> new ParticleVector(
                    between(random, extent.x()),
                    between(random, extent.y()),
                    between(random, extent.z())
            );
            case SPHERE -> ParticleVector.randomInUnitSphere(random).multiply(extent.x());
        };
    }

    private static double between(RandomGenerator random, double extent) {
        return extent == 0.0D ? 0.0D : random.nextDouble(-extent, extent);
    }
}
