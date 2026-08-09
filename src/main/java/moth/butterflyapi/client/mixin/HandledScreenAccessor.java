package moth.butterflyapi.client.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Accessor("x")
    int butterflyApi$getX();

    @Accessor("y")
    int butterflyApi$getY();

    @Accessor("focusedSlot")
    @Nullable
    Slot butterflyApi$getFocusedSlot();
}
