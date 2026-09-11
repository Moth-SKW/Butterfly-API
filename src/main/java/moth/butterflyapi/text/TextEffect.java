package moth.butterflyapi.text;

import net.minecraft.util.Identifier;

public interface TextEffect {
    Identifier id();

    TextRenderOffset offset(TextEffectRenderContext context);
}
