package moth.butterflyapi.content.mixin;

import moth.butterflyapi.content.util.ConnectionEnchantmentUtil;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Inject(method = "onPlayerCollision(Lnet/minecraft/entity/player/PlayerEntity;)V", at = @At("HEAD"), cancellable = true)
    private void butterflyApi$blockConnectionGroundPickup(PlayerEntity player, CallbackInfo ci) {
        if (player.getWorld().isClient) {
            return;
        }

        ItemEntity self = (ItemEntity) (Object) this;
        ItemStack stack = self.getStack();
        if (!ConnectionEnchantmentUtil.canPlayerPickup(stack, player)) {
            ci.cancel();
        }
    }
}
