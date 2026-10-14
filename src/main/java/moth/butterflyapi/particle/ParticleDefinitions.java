package moth.butterflyapi.particle;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public final class ParticleDefinitions {
    private static volatile Map<Identifier, ParticleDefinition> definitions = Map.of();

    private ParticleDefinitions() {
    }

    public static Optional<ParticleDefinition> get(Identifier id) {
        return Optional.ofNullable(definitions.get(Objects.requireNonNull(id, "id")));
    }

    public static Collection<Identifier> ids() {
        return definitions.keySet();
    }

    public static boolean contains(Identifier id) {
        return definitions.containsKey(Objects.requireNonNull(id, "id"));
    }

    public static void replace(Map<Identifier, ParticleDefinition> replacements) {
        Objects.requireNonNull(replacements, "replacements");
        Map<Identifier, ParticleDefinition> copy = new LinkedHashMap<>();
        replacements.forEach((id, definition) -> copy.put(
                Objects.requireNonNull(id, "particle definition id"),
                Objects.requireNonNull(definition, "particle definition")
        ));
        definitions = Map.copyOf(copy);
    }

    public static void clear() {
        definitions = Map.of();
    }
}
