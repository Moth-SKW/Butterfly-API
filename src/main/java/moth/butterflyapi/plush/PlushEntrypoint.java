package moth.butterflyapi.plush;

@FunctionalInterface
public interface PlushEntrypoint {
    void registerPlushes(PlushRegistrar registrar);
}
