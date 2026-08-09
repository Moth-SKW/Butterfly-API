package moth.butterflyapi.content.mixin;

import moth.butterflyapi.content.util.ConnectionEnchantmentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {

    @Inject(method = "canTakeItems(Lnet/minecraft/entity/player/PlayerEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void butterflyApi$blockConnectionInventoryPickup(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        Slot self = (Slot) (Object) this;
        ItemStack stack = self.getStack();
        if (!ConnectionEnchantmentUtil.canPlayerPickup(stack, player)) {
            cir.setReturnValue(false);
        }
    }
}
