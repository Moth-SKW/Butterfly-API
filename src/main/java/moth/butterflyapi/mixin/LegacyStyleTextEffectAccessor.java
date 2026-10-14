package moth.butterflyapi.mixin;

//? if <1.21.11 {
/*

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Style.class)
public interface LegacyStyleTextEffectAccessor {
    @Invoker("<init>")
    static Style butterflyApi$create(
            TextColor color,
            Boolean bold,
            Boolean italic,
            Boolean underlined,
            Boolean strikethrough,
            Boolean obfuscated,
            ClickEvent clickEvent,
            HoverEvent hoverEvent,
            String insertion,
            ResourceLocation font
    ) {
        throw new AssertionError();
    }

    @Accessor("color")
    TextColor butterflyApi$getColor();

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
    ResourceLocation butterflyApi$getFont();
}
*///?}
