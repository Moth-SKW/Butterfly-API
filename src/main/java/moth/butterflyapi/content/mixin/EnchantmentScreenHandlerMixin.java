package moth.butterflyapi.content.mixin;

import moth.butterflyapi.content.util.ConnectionEnchantmentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.EnchantmentScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin {

    @Inject(method = "onButtonClick(Lnet/minecraft/entity/player/PlayerEntity;I)Z", at = @At("RETURN"))
    private void butterflyApi$bindConnectionOwnerOnEnchant(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }

        EnchantmentScreenHandler self = (EnchantmentScreenHandler) (Object) this;
        ItemStack enchantedStack = self.getSlot(0).getStack();
        ConnectionEnchantmentUtil.bindConnectionOwnerIfMissingOrSelf(enchantedStack, player);
    }
}
