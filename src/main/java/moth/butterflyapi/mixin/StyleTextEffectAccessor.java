package moth.butterflyapi.mixin;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.TextColor;
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
            StyleSpriteSource font
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
    StyleSpriteSource butterflyApi$getFont();
}
