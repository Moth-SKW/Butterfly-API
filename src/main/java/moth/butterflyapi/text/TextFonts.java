package moth.butterflyapi.text;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class TextFonts {
    private static final Set<Identifier> REGISTERED_FONTS = new LinkedHashSet<>();

    private TextFonts() {
    }

    public static Identifier register(Identifier id) {
        REGISTERED_FONTS.add(Objects.requireNonNull(id, "id"));
        return id;
    }

    public static Identifier register(String namespace, String path) {
        return register(Identifier.of(namespace, path));
    }

    public static Set<Identifier> registeredFonts() {
        return Set.copyOf(REGISTERED_FONTS);
    }

    public static Style font(Style style, Identifier id) {
        register(id);
        return style.withFont(new StyleSpriteSource.Font(id));
    }

    public static MutableText font(Text text, Identifier id) {
        return text.copy().styled(style -> font(style, id));
    }
}
