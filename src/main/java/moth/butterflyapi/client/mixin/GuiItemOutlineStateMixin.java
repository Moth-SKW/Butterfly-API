package moth.butterflyapi.client.mixin;

//? if >=1.21.11 {
import moth.butterflyapi.client.outline.internal.GuiItemOutlinePass;
import moth.butterflyapi.client.outline.internal.GuiItemOutlineState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStackRenderState.class)
public abstract class GuiItemOutlineStateMixin implements GuiItemOutlineState {
    @Unique
    @Nullable
    private GuiItemOutlinePass butterflyApi$guiItemOutlinePass;

    @Override
    public void butterflyApi$setGuiItemOutlinePass(@Nullable GuiItemOutlinePass pass) {
        butterflyApi$guiItemOutlinePass = pass;
    }

    @Override
    @Nullable
    public GuiItemOutlinePass butterflyApi$getGuiItemOutlinePass() {
        return butterflyApi$guiItemOutlinePass;
    }
}
//?}
