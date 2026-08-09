package moth.butterflyapi.content.paintings;

import moth.butterflyapi.content.ContentBootstrap;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.util.Identifier;

import java.util.Optional;

public final class ModPaintings {
    /** The static shape descriptor; the actual registry entry is loaded from data. */
    public static final PaintingVariant HI = new PaintingVariant(
            1,
            1,
            Identifier.of(ContentBootstrap.MOD_ID, "hi"),
            Optional.empty(),
            Optional.empty()
    );

    private ModPaintings() {
    }

    public static void init() {
        // Painting variants are data-driven dynamic registry entries in 1.21.11.
    }
}
