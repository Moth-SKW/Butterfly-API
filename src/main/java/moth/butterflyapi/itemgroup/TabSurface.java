package moth.butterflyapi.itemgroup;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import net.minecraft.resources.Identifier;

public final class TabSurface {
    @Nullable
    private final Identifier texture;
    @Nullable
    private final Integer color;

    private TabSurface(@Nullable Identifier texture, @Nullable Integer color) {
        this.texture = texture;
        this.color = color;
    }

    public static TabSurface texture(Identifier texture) {
        return new TabSurface(Objects.requireNonNull(texture, "texture"), null);
    }

    public static TabSurface color(String hexColor) {
        return new TabSurface(null, HexColor.parse(hexColor));
    }

    @Nullable
    public Identifier texture() {
        return texture;
    }

    @Nullable
    public Integer color() {
        return color;
    }
}
