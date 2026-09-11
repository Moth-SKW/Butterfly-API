package moth.butterflyapi.text;

public record TextTransform(
        float offsetX,
        float offsetY,
        float rotationDegrees,
        boolean flippedX,
        boolean flippedY,
        TextTransformScope scope
) {
    public static final TextTransform NONE = new TextTransform(0.0F, 0.0F, 0.0F, false, false, TextTransformScope.GLYPH);

    public static TextTransform glyph() {
        return NONE;
    }

    public static TextTransform cluster() {
        return NONE.withScope(TextTransformScope.CLUSTER);
    }

    public TextTransform offset(float x, float y) {
        return new TextTransform(x, y, rotationDegrees, flippedX, flippedY, scope);
    }

    public TextTransform addOffset(float x, float y) {
        return offset(offsetX + x, offsetY + y);
    }

    public TextTransform rotate(float degrees) {
        return new TextTransform(offsetX, offsetY, degrees, flippedX, flippedY, scope);
    }

    public TextTransform mirrorX() {
        return mirrorHorizontal();
    }

    public TextTransform mirrorY() {
        return mirrorVertical();
    }

    public TextTransform mirrorHorizontal() {
        return new TextTransform(offsetX, offsetY, rotationDegrees, true, flippedY, scope);
    }

    public TextTransform mirrorVertical() {
        return new TextTransform(offsetX, offsetY, rotationDegrees, flippedX, true, scope);
    }

    public TextTransform flipAcrossX() {
        return mirrorVertical();
    }

    public TextTransform flipAcrossY() {
        return mirrorHorizontal();
    }

    public TextTransform withScope(TextTransformScope scope) {
        return new TextTransform(offsetX, offsetY, rotationDegrees, flippedX, flippedY, scope);
    }

    public boolean isNone() {
        return offsetX == 0.0F
                && offsetY == 0.0F
                && rotationDegrees == 0.0F
                && !flippedX
                && !flippedY;
    }
}
