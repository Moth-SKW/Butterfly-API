package moth.butterflyapi.client.outline;

import moth.butterflyapi.client.outline.internal.ClientItemOutlineManager;
import moth.butterflyapi.client.outline.internal.GuiItemOutlinePass;
import moth.butterflyapi.client.outline.internal.GuiItemOutlinePipelines;
import moth.butterflyapi.outline.ItemOutlineTarget;
import moth.butterflyapi.outline.OutlineStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Renders GUI-item outlines from the item's rendered alpha mask.
 *
 * <p>The normal item remains the final draw. Outline copies are submitted around it with a
 * solid-colour alpha-mask pipeline, so transparent pixels stay transparent and the border follows
 * the actual rendered item rather than the 16 by 16 item cell.</p>
 */
@Environment(EnvType.CLIENT)
public final class GuiItemOutlineRenderer {
    /**
     * Eight pixels is deliberately the public GUI limit: a thick outline requires several masked
     * item submissions and larger values make densely populated menus needlessly expensive.
     */
    public static final int MAX_SUPPORTED_THICKNESS = 8;

    private static final ThreadLocal<GuiItemOutlinePass> ACTIVE_PASS = new ThreadLocal<>();

    private GuiItemOutlineRenderer() {
    }

    public static void initialize() {
//? if >=1.21.11 {
        GuiItemOutlinePipelines.initialize();
//?}
    }

    /** Submits the exterior portion before the vanilla item draw. */
    public static void renderBefore(GuiGraphics context, ItemStack stack, int x, int y) {
        if (isRenderingPass()) {
            return;
        }
        OutlineStyle style = ClientItemOutlineManager.resolve(stack, ItemOutlineTarget.GUI);
        if (style == null) {
            return;
        }

        int exteriorThickness = exteriorThickness(style);
        if (exteriorThickness == 0) {
            return;
        }
        if (style.blackBorder()) {
            renderExterior(context, stack, x, y, exteriorThickness + 1, 0xFF000000);
        }
        renderExterior(context, stack, x, y, exteriorThickness, opaque(style.rgb()));
    }

    /** Submits the interior portion after the vanilla item draw. */
    public static void renderAfter(GuiGraphics context, ItemStack stack, int x, int y) {
        if (isRenderingPass()) {
            return;
        }
        OutlineStyle style = ClientItemOutlineManager.resolve(stack, ItemOutlineTarget.GUI);
        if (style == null) {
            return;
        }

        int interiorThickness = interiorThickness(style);
        if (interiorThickness == 0) {
            return;
        }
        if (style.blackBorder()) {
            renderInterior(context, stack, x, y, interiorThickness + 1, 0xFF000000);
        }
        renderInterior(context, stack, x, y, interiorThickness, opaque(style.rgb()));
    }

    /**
     * Returns the active synthetic GUI-item pass, if the current item invocation is one of the
     * copies submitted by this renderer. Mixins use this to tag the resulting render state.
     */
    @Nullable
    public static GuiItemOutlinePass activePass() {
        return ACTIVE_PASS.get();
    }

    public static boolean isRenderingPass() {
        return activePass() != null;
    }

    private static void renderExterior(
            GuiGraphics context,
            ItemStack stack,
            int x,
            int y,
            int thickness,
            int color
    ) {
        int radiusSquared = thickness * thickness;
        for (int offsetX = -thickness; offsetX <= thickness; offsetX++) {
            for (int offsetY = -thickness; offsetY <= thickness; offsetY++) {
                if ((offsetX == 0 && offsetY == 0)
                        || offsetX * offsetX + offsetY * offsetY > radiusSquared) {
                    continue;
                }
                renderPass(context, stack, x + offsetX, y + offsetY, GuiItemOutlinePass.silhouette(color));
            }
        }
    }

    private static void renderInterior(
            GuiGraphics context,
            ItemStack stack,
            int x,
            int y,
            int thickness,
            int color
    ) {
        renderPass(context, stack, x, y, GuiItemOutlinePass.innerEdge(color, thickness));
    }

    private static void renderPass(
            GuiGraphics context,
            ItemStack stack,
            int x,
            int y,
            GuiItemOutlinePass pass
    ) {
        GuiItemOutlinePass previous = ACTIVE_PASS.get();
        ACTIVE_PASS.set(pass);
        try {
//? if >=26.1 {
/*
            context.item(stack, x, y);
*///?} else {
            context.renderItem(stack, x, y);
//?}
        } finally {
            if (previous == null) {
                ACTIVE_PASS.remove();
            } else {
                ACTIVE_PASS.set(previous);
            }
        }
    }

    private static int exteriorThickness(OutlineStyle style) {
//? if <1.21.11 {
/*
        // The legacy immediate item renderer has no retained GUI mask to draw after the item.
        // Preserve a visible, model-shaped outline for every placement rather than silently
        // accepting an unsupported placement and drawing nothing.
        return style.thickness();
*///?} else {
        return switch (style.placement()) {
            case OUTSIDE -> style.thickness();
            case INSIDE -> 0;
            case CENTERED -> (style.thickness() + 1) / 2;
        };
//?}
    }

    private static int interiorThickness(OutlineStyle style) {
//? if <1.21.11 {
/*
        return 0;
*///?} else {
        return switch (style.placement()) {
            case OUTSIDE -> 0;
            case INSIDE -> style.thickness();
            case CENTERED -> style.thickness() / 2;
        };
//?}
    }

    private static int opaque(int rgb) {
        return 0xFF000000 | rgb;
    }
}
