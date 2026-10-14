package moth.butterflyapi.mixin;

//? if >=1.21.11 {
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Style.class)
public interface StyleTextEffectAccessor {
    @Invoker("<init>")
    static Style butterflyApi$create(
            TextColor color,
            Integer shadowColor,
            Boolean bold,
            Boolean italic,
            Boolean underlined,
            Boolean strikethrough,
            Boolean obfuscated,
            ClickEvent clickEvent,
            HoverEvent hoverEvent,
            String insertion,
            FontDescription font
    ) {
        throw new AssertionError();
    }

    @Accessor("color")
    TextColor butterflyApi$getColor();

    @Accessor("shadowColor")
    Integer butterflyApi$getShadowColor();

    @Accessor("bold")
    Boolean butterflyApi$getBold();

    @Accessor("italic")
    Boolean butterflyApi$getItalic();

    @Accessor("underlined")
    Boolean butterflyApi$getUnderlined();

    @Accessor("strikethrough")
    Boolean butterflyApi$getStrikethrough();

    @Accessor("obfuscated")
    Boolean butterflyApi$getObfuscated();

    @Accessor("clickEvent")
    ClickEvent butterflyApi$getClickEvent();

    @Accessor("hoverEvent")
    HoverEvent butterflyApi$getHoverEvent();

    @Accessor("insertion")
    String butterflyApi$getInsertion();

    @Accessor("font")
    FontDescription butterflyApi$getFont();
}
//?}
