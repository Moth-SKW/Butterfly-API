package moth.butterflyapi.client.mixin;

//? if <1.21.11 {
/*
import moth.butterflyapi.client.outline.GuiItemOutlineRenderer;
import moth.butterflyapi.client.outline.internal.GuiItemOutlinePass;
import moth.butterflyapi.client.outline.internal.LegacyGuiItemOutlineBufferSource;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// 1.21.1 draws GUI items immediately; synthetic copies only replace ItemRenderer's buffer source.
@Mixin(ItemRenderer.class)
public abstract class LegacyGuiItemRendererOutlineMixin {
    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 1)
    private MultiBufferSource butterflyApi$colorGuiOutlineCopy(MultiBufferSource buffers) {
        GuiItemOutlinePass pass = GuiItemOutlineRenderer.activePass();
        return pass == null || pass.kind() == GuiItemOutlinePass.Kind.INNER_EDGE
                ? buffers
                : new LegacyGuiItemOutlineBufferSource(buffers, pass.color());
    }
}
*///?}
