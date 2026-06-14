package moth.butterflyapi.content.mixin;

import moth.butterflyapi.content.compat.ConnectionDeathCompat;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(
            method = "dropInventory",
            at = @At("HEAD")
    )
    private void butterflyApi$extractConnectionItemsBeforeDeathDrops(
            CallbackInfo ci
    ) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ConnectionDeathCompat.extract(player);
    }
}