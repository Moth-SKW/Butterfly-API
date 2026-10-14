package moth.butterflyapi.client.outline.internal;

import moth.butterflyapi.client.outline.GuiItemOutlineRenderer;

/** Metadata carried by a synthetic GUI-item render state. */
public record GuiItemOutlinePass(Kind kind, int color, int thickness) {
    public GuiItemOutlinePass {
        if (kind == null) {
            throw new NullPointerException("kind");
        }
        if (thickness < 1 || thickness > GuiItemOutlineRenderer.MAX_SUPPORTED_THICKNESS + 1) {
            throw new IllegalArgumentException("Unsupported GUI item-outline thickness: " + thickness);
        }
    }

    public static GuiItemOutlinePass silhouette(int color) {
        return new GuiItemOutlinePass(Kind.SILHOUETTE, color, 1);
    }

    public static GuiItemOutlinePass innerEdge(int color, int thickness) {
        return new GuiItemOutlinePass(Kind.INNER_EDGE, color, thickness);
    }

    public enum Kind {
        SILHOUETTE,
        INNER_EDGE
    }
}
