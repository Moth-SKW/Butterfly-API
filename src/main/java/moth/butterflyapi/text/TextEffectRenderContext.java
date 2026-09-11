package moth.butterflyapi.text;

public record TextEffectRenderContext(
        float left,
        float top,
        float right,
        float bottom,
        int glyphKey,
        long frameTime
) {
}
