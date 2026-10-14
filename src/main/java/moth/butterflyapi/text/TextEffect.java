package moth.butterflyapi.text;

import net.minecraft.resources.Identifier;

public interface TextEffect {
    Identifier id();

    TextRenderOffset offset(TextEffectRenderContext context);
}
