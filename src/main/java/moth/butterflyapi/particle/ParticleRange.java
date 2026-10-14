package moth.butterflyapi.particle;

import java.util.Objects;
import java.util.random.RandomGenerator;

public record ParticleRange(double minimum, double maximum) {
    public ParticleRange {
        if (!Double.isFinite(minimum) || !Double.isFinite(maximum)) {
            throw new IllegalArgumentException("Particle ranges must contain finite numbers");
        }
        if (minimum > maximum) {
            throw new IllegalArgumentException("Particle range minimum cannot exceed its maximum");
        }
    }

    public static ParticleRange fixed(double value) {
        return new ParticleRange(value, value);
    }

    public double sample(RandomGenerator random) {
        Objects.requireNonNull(random, "random");
        return minimum == maximum ? minimum : random.nextDouble(minimum, maximum);
    }
}
