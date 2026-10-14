package moth.butterflyapi.client.mixin;

//? if >=1.21.1 {

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface HandledScreenAccessor {
    @Accessor("leftPos")
    int butterflyApi$getX();

    @Accessor("topPos")
    int butterflyApi$getY();

    @Accessor("hoveredSlot")
    @Nullable
    Slot butterflyApi$getFocusedSlot();
}
//?}
