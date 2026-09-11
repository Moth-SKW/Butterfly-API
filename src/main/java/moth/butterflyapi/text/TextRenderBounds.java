package moth.butterflyapi.text;

public record TextRenderBounds(float minX, float minY, float maxX, float maxY) {
    public static TextRenderBounds of(float minX, float minY, float maxX, float maxY) {
        return new TextRenderBounds(minX, minY, maxX, maxY);
    }

    public TextRenderBounds union(TextRenderBounds other) {
        return new TextRenderBounds(
                Math.min(minX, other.minX),
                Math.min(minY, other.minY),
                Math.max(maxX, other.maxX),
                Math.max(maxY, other.maxY)
        );
    }

    public float width() {
        return Math.max(0.0F, maxX - minX);
    }

    public float height() {
        return Math.max(0.0F, maxY - minY);
    }

    public float centerX() {
        return (minX + maxX) * 0.5F;
    }

    public float centerY() {
        return (minY + maxY) * 0.5F;
    }
}
