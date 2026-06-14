package moth.butterflyapi.plush;

/**
 * Register this as a {@code butterfly_api:plush} entrypoint in {@code fabric.mod.json}
 * to contribute plushes before Butterfly API finalizes its shared plush registry.
 */
@FunctionalInterface
public interface PlushEntrypoint {
    void registerPlushes(PlushRegistrar registrar);
}
