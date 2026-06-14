package moth.butterflyapi.content.mixin;

import moth.butterflyapi.content.util.ConnectionEnchantmentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin {

    @Inject(method = "onTakeOutput", at = @At("HEAD"))
    private void butterflyApi$bindConnectionOwnerOnTake(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        ConnectionEnchantmentUtil.bindConnectionOwnerIfMissingOrSelf(stack, player);
    }
}
