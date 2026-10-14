package moth.butterflyapi.particle;

import java.util.Objects;
import java.util.random.RandomGenerator;

public record ParticleVector(double x, double y, double z) {
    public static final ParticleVector ZERO = new ParticleVector(0.0D, 0.0D, 0.0D);

    public ParticleVector {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            throw new IllegalArgumentException("Particle vectors must contain finite coordinates");
        }
    }

    public ParticleVector add(ParticleVector other) {
        Objects.requireNonNull(other, "other");
        return new ParticleVector(x + other.x, y + other.y, z + other.z);
    }

    public ParticleVector multiply(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Particle vector multiplier must be finite");
        }
        return new ParticleVector(x * value, y * value, z * value);
    }

    public static ParticleVector randomInUnitSphere(RandomGenerator random) {
        Objects.requireNonNull(random, "random");
        for (int attempt = 0; attempt < 32; attempt++) {
            ParticleVector value = new ParticleVector(
                    random.nextDouble(-1.0D, 1.0D),
                    random.nextDouble(-1.0D, 1.0D),
                    random.nextDouble(-1.0D, 1.0D)
            );
            if (value.lengthSquared() <= 1.0D) {
                return value;
            }
        }
        return ZERO;
    }

    public double lengthSquared() {
        return x * x + y * y + z * z;
    }
}
