package moth.butterflyapi.client.mixin;

//? if >=1.21.11 {
import moth.butterflyapi.client.outline.GuiItemOutlineRenderer;
import moth.butterflyapi.client.outline.internal.GuiItemOutlineState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelResolver.class)
public abstract class GuiItemOutlineModelResolverMixin {
    @Inject(method = "updateForTopItem", at = @At("RETURN"), require = 1)
    private void butterflyApi$tagGuiOutlineState(
            ItemStackRenderState state,
            ItemStack stack,
            ItemDisplayContext displayContext,
            Level level,
            ItemOwner owner,
            int seed,
            CallbackInfo ci
    ) {
        if (displayContext == ItemDisplayContext.GUI && state instanceof GuiItemOutlineState outlineState) {
            outlineState.butterflyApi$setGuiItemOutlinePass(GuiItemOutlineRenderer.activePass());
        }
    }
}
//?}
