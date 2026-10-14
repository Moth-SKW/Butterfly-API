package moth.butterflyapi.client.outline;

//? if <1.21.11 {
/*
import moth.butterflyapi.client.outline.internal.ClientOutlineManager;
import moth.butterflyapi.outline.EntityOutline;
import moth.butterflyapi.outline.EntityOutlinePart;
import moth.butterflyapi.outline.OutlineStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.world.entity.Entity;

public final class LegacyOutlineParts {
    private LegacyOutlineParts() {
    }

    public static boolean configure(MultiBufferSource buffers, Entity entity, EntityOutlinePart part) {
        if (!(buffers instanceof OutlineBufferSource provider)) {
            return true;
        }
        EntityOutline outline = ClientOutlineManager.resolveOutline(entity);
        if (outline == null) {
            return true;
        }
        OutlineStyle style = outline.style(part).orElse(null);
        if (style == null) {
            return false;
        }
        set(provider, entity, style);
        return true;
    }

    public static boolean includes(EntityOutline outline, EntityOutlinePart part) {
        return outline == null || outline.style(part).isPresent();
    }

    public static void restoreBody(MultiBufferSource buffers, Entity entity) {
        if (!(buffers instanceof OutlineBufferSource provider)) {
            return;
        }
        EntityOutline outline = ClientOutlineManager.resolveOutline(entity);
        if (outline == null) {
            return;
        }
        OutlineStyle style = outline.style(EntityOutlinePart.BODY).orElse(null);
        if (style == null) {
            provider.setColor(0, 0, 0, 255);
        } else {
            set(provider, entity, style);
        }
    }

    private static void set(OutlineBufferSource provider, Entity entity, OutlineStyle style) {
        int color = OutlineRenderBridge.encodedLegacyEntityColor(
                entity,
                style,
                Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()
        );
        provider.setColor(
                (color >> 16) & 0xFF,
                (color >> 8) & 0xFF,
                color & 0xFF,
                (color >>> 24) & 0xFF
        );
    }
}
*///?}
