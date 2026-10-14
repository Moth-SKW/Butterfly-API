package moth.butterflyapi.particle;

public final class ParticleDefinitionException extends IllegalArgumentException {
    public ParticleDefinitionException(String message) {
        super(message);
    }

    public ParticleDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
