package moth.butterflyapi.client.mixin;

//? if >=1.21.11 {
import moth.butterflyapi.client.outline.OutlineRenderBridge;
import moth.butterflyapi.client.outline.internal.ClientOutlineManager;
import moth.butterflyapi.outline.EntityOutline;
import moth.butterflyapi.outline.EntityOutlinePart;
import moth.butterflyapi.outline.OutlineStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererOutlineMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"), require = 1)
    private void butterflyApi$applyCustomOutline(
            Entity entity,
            EntityRenderState state,
            float tickDelta,
            CallbackInfo ci
    ) {
        EntityOutline outline = ClientOutlineManager.resolveOutline(entity);
        ClientOutlineManager.bindRenderState(state, entity, outline);
        OutlineStyle style = outline == null
                ? null
                : outline.style(EntityOutlinePart.BODY).orElse(null);
        if (style != null) {
            Vec3 camera = Minecraft.getInstance().gameRenderer
//? if >=26.2 {
/*
                    .mainCamera()
*///?} else {
                    .getMainCamera()
//?}
                    .position();
            state.outlineColor = OutlineRenderBridge.encodedColor(entity, style, camera);
        } else if (outline != null) {
            state.outlineColor = 0;
        }
    }
}
//?}
