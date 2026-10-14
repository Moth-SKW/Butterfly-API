package moth.butterflyapi.client.text.internal;

//? if >=1.21.11 {
//? if >=26.3 {
/*
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.GpuTextureView;
*///?} else {
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
//?}
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;

public final class OffsetGlyphRect implements TextRenderable.Styled {
    private final TextRenderable.Styled delegate;
    private final float offsetX;
    private final float offsetY;

    public OffsetGlyphRect(TextRenderable.Styled delegate, float offsetX, float offsetY) {
        this.delegate = delegate;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Override
    public Style style() {
        return delegate.style();
    }

    @Override
    public float activeLeft() {
        return delegate.activeLeft() + offsetX;
    }

    @Override
    public float activeTop() {
        return delegate.activeTop() + offsetY;
    }

    @Override
    public float activeRight() {
        return delegate.activeRight() + offsetX;
    }

    @Override
    public float activeBottom() {
        return delegate.activeBottom() + offsetY;
    }

    @Override
    public void render(Matrix4f matrix, VertexConsumer vertexConsumer, int light, boolean shadow) {
        delegate.render(new Matrix4f(matrix).translate(offsetX, offsetY, 0.0F), vertexConsumer, light, shadow);
    }

    @Override
    public RenderType renderType(Font.DisplayMode layerType) {
        return delegate.renderType(layerType);
    }

    @Override
    public GpuTextureView textureView() {
        return delegate.textureView();
    }

    @Override
    public RenderPipeline guiPipeline() {
        return delegate.guiPipeline();
    }

    @Override
    public float left() {
        return delegate.left() + offsetX;
    }

    @Override
    public float top() {
        return delegate.top() + offsetY;
    }

    @Override
    public float right() {
        return delegate.right() + offsetX;
    }

    @Override
    public float bottom() {
        return delegate.bottom() + offsetY;
    }
}
//?}
