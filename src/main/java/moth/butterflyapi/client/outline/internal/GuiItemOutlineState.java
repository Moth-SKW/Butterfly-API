package moth.butterflyapi.client.outline.internal;

import org.jetbrains.annotations.Nullable;

/** Implemented by item render states that were created for a GUI outline pass. */
public interface GuiItemOutlineState {
    void butterflyApi$setGuiItemOutlinePass(@Nullable GuiItemOutlinePass pass);

    @Nullable
    GuiItemOutlinePass butterflyApi$getGuiItemOutlinePass();
}
