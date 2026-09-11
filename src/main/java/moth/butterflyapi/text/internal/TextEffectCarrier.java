package moth.butterflyapi.text.internal;

import moth.butterflyapi.text.TextEffect;
import org.jetbrains.annotations.Nullable;

public interface TextEffectCarrier {
    @Nullable
    TextEffect butterflyApi$textEffect();

    void butterflyApi$setTextEffect(@Nullable TextEffect effect);
}
