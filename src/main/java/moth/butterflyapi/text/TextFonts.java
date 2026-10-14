package moth.butterflyapi.text;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.network.chat.Component;
//? if >=1.21.11 {
import net.minecraft.network.chat.FontDescription;
//?}
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class TextFonts {
    private static final Set<Identifier> REGISTERED_FONTS = new LinkedHashSet<>();

    private TextFonts() {
    }

    public static Identifier register(Identifier id) {
        REGISTERED_FONTS.add(Objects.requireNonNull(id, "id"));
        return id;
    }

    public static Identifier register(String namespace, String path) {
        return register(Identifier.fromNamespaceAndPath(namespace, path));
    }

    public static Set<Identifier> registeredFonts() {
        return Set.copyOf(REGISTERED_FONTS);
    }

    public static Style font(Style style, Identifier id) {
        register(id);
//? if <1.21.11 {
/*
        return style.withFont(id);
*///?} else {
        return style.withFont(new FontDescription.Resource(id));
//?}
    }

    public static MutableComponent font(Component text, Identifier id) {
        return text.copy().withStyle(style -> font(style, id));
    }
}
