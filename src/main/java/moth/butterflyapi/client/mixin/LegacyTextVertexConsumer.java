package moth.butterflyapi.client.text.internal;

//? if <1.21.11 {
/*
import com.mojang.blaze3d.vertex.VertexConsumer;
import moth.butterflyapi.text.TextRenderBounds;
import moth.butterflyapi.text.TextRenderOffset;
import moth.butterflyapi.text.TextTransform;
import moth.butterflyapi.text.TextTransformScope;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class LegacyTextVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final int color;
    private final boolean overrideColor;
    private final TextTransform transform;
    private final TextRenderBounds glyphBounds;
    private final TextRenderBounds clusterBounds;
    private final TextRenderOffset effectOffset;
    private final boolean reverseWinding;
    private final CapturedVertex[] quad = new CapturedVertex[4];
    private int quadSize;
    private CapturedVertex pending;

    public LegacyTextVertexConsumer(
            VertexConsumer delegate,
            int color,
            TextTransform transform,
            TextRenderBounds glyphBounds,
            TextRenderBounds clusterBounds,
            TextRenderOffset effectOffset
    ) {
        this.delegate = delegate;
        this.color = color;
        overrideColor = true;
        this.transform = transform == null ? TextTransform.NONE : transform;
        this.glyphBounds = glyphBounds;
        this.clusterBounds = clusterBounds;
        this.effectOffset = effectOffset == null ? TextRenderOffset.ZERO : effectOffset;
        reverseWinding = this.transform.flippedX() != this.transform.flippedY();
    }

    public LegacyTextVertexConsumer(
            VertexConsumer delegate,
            TextTransform transform,
            TextRenderBounds glyphBounds,
            TextRenderBounds clusterBounds,
            TextRenderOffset effectOffset
    ) {
        this.delegate = delegate;
        color = 0;
        overrideColor = false;
        this.transform = transform == null ? TextTransform.NONE : transform;
        this.glyphBounds = glyphBounds;
        this.clusterBounds = clusterBounds;
        this.effectOffset = effectOffset == null ? TextRenderOffset.ZERO : effectOffset;
        reverseWinding = this.transform.flippedX() != this.transform.flippedY();
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        flushIncomplete();
        float[] transformed = transform(x, y, pivotBounds().centerX(), pivotBounds().centerY());
        pending = new CapturedVertex(transformed[0], transformed[1], z);
        return this;
    }

    @Override
    public VertexConsumer addVertex(Matrix4f matrix, float x, float y, float z) {
        flushIncomplete();
        Vector3f point = matrix.transformPosition(x, y, z, new Vector3f());
        TextRenderBounds pivotBounds = pivotBounds();
        Vector3f pivot = matrix.transformPosition(pivotBounds.centerX(), pivotBounds.centerY(), z, new Vector3f());
        float[] transformed = transform(point.x(), point.y(), pivot.x(), pivot.y());
        pending = new CapturedVertex(transformed[0], transformed[1], point.z());
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        if (pending != null) {
            pending.red = red;
            pending.green = green;
            pending.blue = blue;
            pending.alpha = alpha;
            pending.hasColor = true;
        }
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        if (pending != null) {
            pending.u = u;
            pending.v = v;
            pending.hasUv = true;
        }
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        if (pending != null) {
            pending.overlayU = u;
            pending.overlayV = v;
            pending.hasOverlay = true;
        }
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        if (pending != null) {
            pending.lightU = u;
            pending.lightV = v;
            pending.hasLight = true;
            flush();
        }
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        if (pending != null) {
            pending.normalX = x;
            pending.normalY = y;
            pending.normalZ = z;
            pending.hasNormal = true;
        }
        return this;
    }

    private TextRenderBounds pivotBounds() {
        return transform.scope() == TextTransformScope.CLUSTER ? clusterBounds : glyphBounds;
    }

    private float[] transform(float x, float y, float pivotX, float pivotY) {
        float localX = x - pivotX;
        float localY = y - pivotY;
        if (transform.flippedX()) {
            localX = -localX;
        }
        if (transform.flippedY()) {
            localY = -localY;
        }
        if (transform.rotationDegrees() != 0.0F) {
            double radians = Math.toRadians(transform.rotationDegrees());
            float rotatedX = (float) (localX * Math.cos(radians) - localY * Math.sin(radians));
            float rotatedY = (float) (localX * Math.sin(radians) + localY * Math.cos(radians));
            localX = rotatedX;
            localY = rotatedY;
        }
        return new float[] {
                pivotX + localX + effectOffset.x() + transform.offsetX(),
                pivotY + localY + effectOffset.y() + transform.offsetY()
        };
    }

    private void flushIncomplete() {
        if (pending != null) {
            flush();
        }
    }

    private void flush() {
        if (pending == null) {
            return;
        }
        if (!reverseWinding) {
            write(pending);
            pending = null;
            return;
        }
        quad[quadSize++] = pending;
        pending = null;
        if (quadSize == 4) {
            write(quad[0]);
            write(quad[3]);
            write(quad[2]);
            write(quad[1]);
            for (int index = 0; index < quad.length; index++) {
                quad[index] = null;
            }
            quadSize = 0;
        }
    }

    private void write(CapturedVertex vertex) {
        delegate.addVertex(vertex.x, vertex.y, vertex.z);
        if (overrideColor) {
            delegate.setColor(color);
        } else if (vertex.hasColor) {
            delegate.setColor(vertex.red, vertex.green, vertex.blue, vertex.alpha);
        }
        if (vertex.hasUv) {
            delegate.setUv(vertex.u, vertex.v);
        }
        if (vertex.hasOverlay) {
            delegate.setUv1(vertex.overlayU, vertex.overlayV);
        }
        if (vertex.hasLight) {
            delegate.setUv2(vertex.lightU, vertex.lightV);
        }
        if (vertex.hasNormal) {
            delegate.setNormal(vertex.normalX, vertex.normalY, vertex.normalZ);
        }
    }

    private static final class CapturedVertex {
        private final float x;
        private final float y;
        private final float z;
        private int red;
        private int green;
        private int blue;
        private int alpha;
        private float u;
        private float v;
        private int overlayU;
        private int overlayV;
        private int lightU;
        private int lightV;
        private float normalX;
        private float normalY;
        private float normalZ;
        private boolean hasUv;
        private boolean hasColor;
        private boolean hasOverlay;
        private boolean hasLight;
        private boolean hasNormal;

        private CapturedVertex(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
*///?}
