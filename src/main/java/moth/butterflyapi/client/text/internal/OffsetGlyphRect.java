package moth.butterflyapi.client.text.internal;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.font.TextDrawable;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.text.Style;
import org.joml.Matrix4f;

public final class OffsetGlyphRect implements TextDrawable.DrawnGlyphRect {
    private final TextDrawable.DrawnGlyphRect delegate;
    private final float offsetX;
    private final float offsetY;

    public OffsetGlyphRect(TextDrawable.DrawnGlyphRect delegate, float offsetX, float offsetY) {
        this.delegate = delegate;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Override
    public Style style() {
        return delegate.style();
    }

    @Override
    public float getLeft() {
        return delegate.getLeft() + offsetX;
    }

    @Override
    public float getTop() {
        return delegate.getTop() + offsetY;
    }

    @Override
    public float getRight() {
        return delegate.getRight() + offsetX;
    }

    @Override
    public float getBottom() {
        return delegate.getBottom() + offsetY;
    }

    @Override
    public void render(Matrix4f matrix, VertexConsumer vertexConsumer, int light, boolean shadow) {
        delegate.render(new Matrix4f(matrix).translate(offsetX, offsetY, 0.0F), vertexConsumer, light, shadow);
    }

    @Override
    public RenderLayer getRenderLayer(TextRenderer.TextLayerType layerType) {
        return delegate.getRenderLayer(layerType);
    }

    @Override
    public GpuTextureView textureView() {
        return delegate.textureView();
    }

    @Override
    public RenderPipeline getPipeline() {
        return delegate.getPipeline();
    }

    @Override
    public float getEffectiveMinX() {
        return delegate.getEffectiveMinX() + offsetX;
    }

    @Override
    public float getEffectiveMinY() {
        return delegate.getEffectiveMinY() + offsetY;
    }

    @Override
    public float getEffectiveMaxX() {
        return delegate.getEffectiveMaxX() + offsetX;
    }

    @Override
    public float getEffectiveMaxY() {
        return delegate.getEffectiveMaxY() + offsetY;
    }
}
