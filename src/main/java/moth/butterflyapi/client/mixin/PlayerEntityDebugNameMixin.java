package moth.butterflyapi.client.mixin;

import moth.butterflyapi.text.ShakyTextProfile;
import moth.butterflyapi.text.Texts;
import moth.butterflyapi.text.TextShakingOptions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityDebugNameMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void butterflyApi$debugShakyPlayerName(CallbackInfoReturnable<Text> cir) {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment() || cir.getReturnValue() == null) {
            return;
        }
        TextShakingOptions options = TextShakingOptions.DEFAULT
                .withStrength(1.2F)
                .withProfile(ShakyTextProfile.WAVE)
                .withSpeed(1.45F);
        cir.setReturnValue(Texts.shaky(cir.getReturnValue(), options).styled(style -> style.withColor(Texts.rgb("#9DFF6E"))));
    }
}
