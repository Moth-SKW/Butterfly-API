package moth.butterflyapi.text.internal;

import moth.butterflyapi.text.TextRenderStyle;
import org.jetbrains.annotations.Nullable;

public interface TextRenderStyleCarrier {
    @Nullable
    TextRenderStyle butterflyApi$textRenderStyle();

    void butterflyApi$setTextRenderStyle(@Nullable TextRenderStyle renderStyle);
}
