package moth.butterflyapi.client.mixin;

import moth.butterflyapi.client.outline.OutlineDebug;
import moth.butterflyapi.client.outline.internal.ClientOutlineManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientOutlineMixin {
    @Inject(
            method = "hasOutline",
            at = @At("RETURN"),
            cancellable = true,
            require = 1
    )
    private void butterflyApi$includeCustomOutlines(
            Entity entity,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValueZ()
                && (OutlineDebug.styleFor(entity) != null || ClientOutlineManager.resolve(entity) != null)) {
            cir.setReturnValue(true);
        }
    }
}
