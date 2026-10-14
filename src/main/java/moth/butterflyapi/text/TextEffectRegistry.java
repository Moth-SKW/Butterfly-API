package moth.butterflyapi.text;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;

public final class TextEffectRegistry {
    private static final Map<Identifier, TextEffect> EFFECTS = new ConcurrentHashMap<>();

    private TextEffectRegistry() {
    }

    public static <T extends TextEffect> T register(T effect) {
        Objects.requireNonNull(effect, "effect");
        TextEffect previous = EFFECTS.putIfAbsent(effect.id(), effect);
        if (previous != null) {
            throw new IllegalArgumentException("Text effect already registered: " + effect.id());
        }
        return effect;
    }

    public static Optional<TextEffect> get(Identifier id) {
        return Optional.ofNullable(EFFECTS.get(Objects.requireNonNull(id, "id")));
    }

    public static boolean isRegistered(Identifier id) {
        return EFFECTS.containsKey(Objects.requireNonNull(id, "id"));
    }

    public static void unregister(Identifier id) {
        EFFECTS.remove(Objects.requireNonNull(id, "id"));
    }

    public static Collection<TextEffect> all() {
        return List.copyOf(EFFECTS.values());
    }
}
