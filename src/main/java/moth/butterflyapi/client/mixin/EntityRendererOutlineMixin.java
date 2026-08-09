package moth.butterflyapi.client.mixin;

import moth.butterflyapi.client.outline.OutlineDebug;
import moth.butterflyapi.client.outline.OutlineRenderBridge;
import moth.butterflyapi.client.outline.internal.ClientOutlineManager;
import moth.butterflyapi.outline.OutlineStyle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererOutlineMixin {
    @Inject(method = "updateRenderState", at = @At("TAIL"), require = 1)
    private void butterflyApi$applyCustomOutline(
            Entity entity,
            EntityRenderState state,
            float tickDelta,
            CallbackInfo ci
    ) {
        OutlineStyle style = ClientOutlineManager.resolve(entity);
        if (style == null) {
            style = OutlineDebug.styleFor(entity);
        }
        if (style != null) {
            Vec3d camera = MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos();
            state.outlineColor = OutlineRenderBridge.encodedColor(entity, style, camera);
        }
    }
}
