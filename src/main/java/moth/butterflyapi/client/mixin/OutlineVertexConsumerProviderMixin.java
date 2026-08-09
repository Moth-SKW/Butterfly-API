package moth.butterflyapi.client.mixin;

import moth.butterflyapi.plush.PlushBlockEntityRenderer;
import moth.butterflyapi.client.render.NoopVertexConsumer;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OutlineVertexConsumerProvider.class)
public abstract class OutlineVertexConsumerProviderMixin {
    @Inject(method = "getBuffer", at = @At("HEAD"), cancellable = true, require = 1)
    private void butterflyApi$skipPlushCutout(RenderLayer layer, CallbackInfoReturnable<VertexConsumer> cir) {
        if (PlushBlockEntityRenderer.isPlushCutout(layer)) {
            cir.setReturnValue(NoopVertexConsumer.INSTANCE);
        }
    }
}
