package moth.butterflyapi.client.mixin;

import moth.butterflyapi.client.outline.internal.ClientOutlineManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftClientOutlineMixin {
    @Inject(
            method = "shouldEntityAppearGlowing",
            at = @At("RETURN"),
            cancellable = true,
            require = 1
    )
    private void butterflyApi$includeCustomOutlines(
            Entity entity,
            CallbackInfoReturnable<Boolean> cir
    ) {
//? if <1.21.11 {
/*
        Minecraft client = Minecraft.getInstance();
        if (entity == client.getCameraEntity() && client.options.getCameraType().isFirstPerson()) {
            return;
        }
*///?}
        if (!cir.getReturnValueZ() && ClientOutlineManager.resolve(entity) != null) {
            cir.setReturnValue(true);
        }
    }
}
