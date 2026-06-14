package moth.butterflyapi.itemgroup;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public final class TabEntryStyle {
    private static final TabEntryStyle DEFAULT = new TabEntryStyle(null);

    @Nullable
    private final TabSurface slotBackground;

    private TabEntryStyle(@Nullable TabSurface slotBackground) {
        this.slotBackground = slotBackground;
    }

    static TabEntryStyle configure(Consumer<Builder> configuration) {
        Objects.requireNonNull(configuration, "configuration");
        Builder builder = new Builder();
        configuration.accept(builder);
        return builder.build();
    }

    static TabEntryStyle defaultStyle() {
        return DEFAULT;
    }

    @Nullable
    TabSurface slotBackground() {
        return slotBackground;
    }

    public static final class Builder {
        @Nullable
        private TabSurface slotBackground;

        public Builder slotTexture(Identifier texture) {
            slotBackground = TabSurface.texture(Objects.requireNonNull(texture, "texture"));
            return this;
        }

        public Builder slotColor(String hexColor) {
            slotBackground = TabSurface.color(hexColor);
            return this;
        }

        public Builder slotBackgroundTexture(Identifier texture) {
            return slotTexture(texture);
        }

        public Builder slotBackgroundColor(String hexColor) {
            return slotColor(hexColor);
        }

        public Builder defaultSlot() {
            slotBackground = null;
            return this;
        }

        private TabEntryStyle build() {
            return slotBackground == null ? DEFAULT : new TabEntryStyle(slotBackground);
        }
    }
}
