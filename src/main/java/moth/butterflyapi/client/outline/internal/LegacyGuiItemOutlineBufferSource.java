package moth.butterflyapi.client.outline.internal;

//? if <1.21.11 {
/*
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

// A colour-forcing buffer source used only by synthetic legacy GUI item draws.
public final class LegacyGuiItemOutlineBufferSource implements MultiBufferSource {
    private final MultiBufferSource delegate;
    private final int color;

    public LegacyGuiItemOutlineBufferSource(MultiBufferSource delegate, int color) {
        this.delegate = delegate;
        this.color = color;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return new ColorOverrideConsumer(delegate.getBuffer(renderType), color);
    }

    private static final class ColorOverrideConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final int color;

        private ColorOverrideConsumer(VertexConsumer delegate, int color) {
            this.delegate = delegate;
            this.color = color;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            delegate.setColor(color);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            delegate.setNormal(x, y, z);
            return this;
        }
    }
}
*///?}
