package moth.butterflyapi.client.mixin;

//? if >=1.21.11 && <26.2 {
import moth.butterflyapi.plush.PlushBlockEntityRenderer;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import moth.butterflyapi.client.render.NoopVertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OutlineBufferSource.class)
public abstract class OutlineVertexConsumerProviderMixin {
    @Inject(method = "getBuffer", at = @At("HEAD"), cancellable = true, require = 1)
    private void butterflyApi$skipPlushCutout(RenderType layer, CallbackInfoReturnable<VertexConsumer> cir) {
        if (PlushBlockEntityRenderer.isPlushCutout(layer)) {
            cir.setReturnValue(NoopVertexConsumer.INSTANCE);
        }
    }
}
//?}
