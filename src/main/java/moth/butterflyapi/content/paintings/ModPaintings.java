package moth.butterflyapi.content.paintings;

import moth.butterflyapi.content.ContentBootstrap;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModPaintings {

    public static final PaintingVariant HI = register("hi", new PaintingVariant(16, 16));

    private ModPaintings() {}

    private static PaintingVariant register(String id, PaintingVariant variant) {
        return Registry.register(Registries.PAINTING_VARIANT, new Identifier(ContentBootstrap.MOD_ID, id), variant);
    }

    public static void init() {}
}
