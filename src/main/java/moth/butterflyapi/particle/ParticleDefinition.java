package moth.butterflyapi.particle;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public record ParticleDefinition(
        ParticleRange emitterLifetimeSeconds,
        double emissionRate,
        ParticleEmitterShape emissionShape,
        ParticleRange particleLifetimeSeconds,
        ParticleVector initialPosition,
        ParticleVector initialVelocity,
        ParticleVector acceleration,
        ParticleColor startColor,
        ParticleColor endColor,
        ParticleRange startSize,
        ParticleRange endSize,
        Optional<Identifier> texture
) {
    public static final double MAX_EMITTER_LIFETIME_SECONDS = 600.0D;
    public static final double MAX_EMISSION_RATE = 1024.0D;
    public static final double MAX_PARTICLE_LIFETIME_SECONDS = 60.0D;
    public static final double MAX_PARTICLE_SIZE = 16.0D;

    public ParticleDefinition {
        Objects.requireNonNull(emitterLifetimeSeconds, "emitterLifetimeSeconds");
        Objects.requireNonNull(emissionShape, "emissionShape");
        Objects.requireNonNull(particleLifetimeSeconds, "particleLifetimeSeconds");
        Objects.requireNonNull(initialPosition, "initialPosition");
        Objects.requireNonNull(initialVelocity, "initialVelocity");
        Objects.requireNonNull(acceleration, "acceleration");
        Objects.requireNonNull(startColor, "startColor");
        Objects.requireNonNull(endColor, "endColor");
        Objects.requireNonNull(startSize, "startSize");
        Objects.requireNonNull(endSize, "endSize");
        texture = Objects.requireNonNull(texture, "texture");
        validateRange(emitterLifetimeSeconds, "emitter lifetime", 0.0D, MAX_EMITTER_LIFETIME_SECONDS);
        if (!Double.isFinite(emissionRate) || emissionRate < 0.0D || emissionRate > MAX_EMISSION_RATE) {
            throw new IllegalArgumentException("Particle emission rate must be between 0 and " + MAX_EMISSION_RATE);
        }
        validateRange(particleLifetimeSeconds, "particle lifetime", 0.05D, MAX_PARTICLE_LIFETIME_SECONDS);
        validateRange(startSize, "particle start size", 0.01D, MAX_PARTICLE_SIZE);
        validateRange(endSize, "particle end size", 0.01D, MAX_PARTICLE_SIZE);
    }

    private static void validateRange(ParticleRange range, String name, double minimum, double maximum) {
        if (range.minimum() < minimum || range.maximum() > maximum) {
            throw new IllegalArgumentException(name + " must be between " + minimum + " and " + maximum);
        }
    }
}
