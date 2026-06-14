package moth.butterflyapi.content.mixin;

import moth.butterflyapi.content.compat.ConnectionDeathCompat;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(
        targets = "net.capozi.demise.common.entity.PlayerRemainsEntity",
        remap = false
)
public abstract class DemisePlayerRemainsEntityMixin {
    @Inject(
            method = "create",
            at = @At("HEAD"),
            remap = false
    )
    private static void butterflyApi$extractConnectionItemsBeforeDemiseCopiesInventory(
            EntityType<? extends LivingEntity> entityType,
            World world,
            PlayerEntity player,
            CallbackInfoReturnable<?> cir
    ) {
        ConnectionDeathCompat.extract(player);
    }
}