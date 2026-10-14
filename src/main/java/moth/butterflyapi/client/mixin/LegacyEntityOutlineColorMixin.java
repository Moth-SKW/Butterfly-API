package moth.butterflyapi.client.mixin;

//? if <1.21.11 {
/*
import moth.butterflyapi.client.outline.OutlineRenderBridge;
import moth.butterflyapi.client.outline.internal.ClientOutlineManager;
import moth.butterflyapi.outline.EntityOutline;
import moth.butterflyapi.outline.EntityOutlinePart;
import moth.butterflyapi.outline.OutlineStyle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public abstract class LegacyEntityOutlineColorMixin {
    @Redirect(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getTeamColor()I"
            )
    )
    private int butterflyApi$useCustomOutlineColor(Entity entity) {
        EntityOutline outline = ClientOutlineManager.resolveOutline(entity);
        if (outline == null) {
            return entity.getTeamColor();
        }
        OutlineStyle style = outline.style(EntityOutlinePart.BODY).orElse(null);
        return style == null
                ? 0xFF000000
                : OutlineRenderBridge.encodedLegacyEntityColor(
                        entity,
                        style,
                        Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()
                );
    }
}
*///?}
