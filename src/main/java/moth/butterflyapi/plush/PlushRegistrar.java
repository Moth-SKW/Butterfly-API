package moth.butterflyapi.plush;

import moth.butterflyapi.mod.ModContext;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.function.Consumer;

@FunctionalInterface
public interface PlushRegistrar {
    RegisteredPlush register(PlushDefinition definition);

    default RegisteredPlush plush(Identifier id) {
        return plush(id, builder -> {});
    }

    default RegisteredPlush plush(ModContext context, String path) {
        return plush(context, path, builder -> {});
    }

    default RegisteredPlush plush(Identifier id, Consumer<PlushDefinition.Builder> customizer) {
        return customizeAndRegister(PlushDefinition.builder(id), customizer);
    }

    default RegisteredPlush plush(ModContext context, String path, Consumer<PlushDefinition.Builder> customizer) {
        return customizeAndRegister(PlushDefinition.builder(context, path), customizer);
    }

    private RegisteredPlush customizeAndRegister(PlushDefinition.Builder builder, Consumer<PlushDefinition.Builder> customizer) {
        Objects.requireNonNull(builder, "builder");
        Objects.requireNonNull(customizer, "customizer").accept(builder);
        return register(builder.build());
    }
}
