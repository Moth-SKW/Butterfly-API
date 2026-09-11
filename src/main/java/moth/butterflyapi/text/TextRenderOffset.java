package moth.butterflyapi.text;

public record TextRenderOffset(float x, float y) {
    public static final TextRenderOffset ZERO = new TextRenderOffset(0.0F, 0.0F);

    public boolean isZero() {
        return x == 0.0F && y == 0.0F;
    }
}
