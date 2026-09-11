package moth.butterflyapi.client.text.internal;

import moth.butterflyapi.text.TextRenderBounds;
import moth.butterflyapi.text.TextRenderOffset;
import moth.butterflyapi.text.TextTransform;
import moth.butterflyapi.text.TextTransformScope;
import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public final class ColorOverrideVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final int color;
    private final TextTransform transform;
    private final TextRenderBounds glyphBounds;
    private final TextRenderBounds clusterBounds;
    private final TextRenderOffset effectOffset;
    private final boolean preserveMirroredWinding;
    private final CapturedVertex[] quad = new CapturedVertex[4];
    private int quadSize;
    private CapturedVertex pendingVertex;

    public ColorOverrideVertexConsumer(VertexConsumer delegate, int color) {
        this(delegate, color, TextTransform.NONE, TextRenderBounds.of(0.0F, 0.0F, 0.0F, 0.0F), TextRenderBounds.of(0.0F, 0.0F, 0.0F, 0.0F), TextRenderOffset.ZERO);
    }

    public ColorOverrideVertexConsumer(
            VertexConsumer delegate,
            int color,
            TextTransform transform,
            TextRenderBounds glyphBounds,
            TextRenderBounds clusterBounds,
            TextRenderOffset effectOffset
    ) {
        this.delegate = delegate;
        this.color = color;
        this.transform = transform == null ? TextTransform.NONE : transform;
        this.glyphBounds = glyphBounds;
        this.clusterBounds = clusterBounds;
        this.effectOffset = effectOffset == null ? TextRenderOffset.ZERO : effectOffset;
        preserveMirroredWinding = this.transform.flippedX() != this.transform.flippedY();
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        flushPendingIncompleteVertex();
        float[] transformed = transform(x, y);
        pendingVertex = new CapturedVertex(transformed[0], transformed[1], z);
        return this;
    }

    @Override
    public VertexConsumer vertex(Matrix4fc matrix, float x, float y, float z) {
        flushPendingIncompleteVertex();
        Vector3f position = matrix.transformPosition(x, y, z, new Vector3f());
        TextRenderBounds pivotBounds = transform.scope() == TextTransformScope.CLUSTER ? clusterBounds : glyphBounds;
        Vector3f pivot = matrix.transformPosition(pivotBounds.centerX(), pivotBounds.centerY(), z, new Vector3f());
        float[] transformed = transform(position.x(), position.y(), pivot.x(), pivot.y());
        pendingVertex = new CapturedVertex(transformed[0], transformed[1], position.z());
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return this;
    }

    @Override
    public VertexConsumer color(int argb) {
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        if (pendingVertex != null) {
            pendingVertex.u = u;
            pendingVertex.v = v;
            pendingVertex.hasTexture = true;
        }
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        if (pendingVertex != null) {
            pendingVertex.overlayU = u;
            pendingVertex.overlayV = v;
            pendingVertex.hasOverlay = true;
        }
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        if (pendingVertex != null) {
            pendingVertex.lightU = u;
            pendingVertex.lightV = v;
            pendingVertex.hasLight = true;
            flushPendingVertex();
        }
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        if (pendingVertex != null) {
            pendingVertex.normalX = x;
            pendingVertex.normalY = y;
            pendingVertex.normalZ = z;
            pendingVertex.hasNormal = true;
        }
        return this;
    }

    @Override
    public VertexConsumer lineWidth(float width) {
        flushPendingIncompleteVertex();
        delegate.lineWidth(width);
        return this;
    }

    private float[] transform(float x, float y) {
        TextRenderBounds pivotBounds = transform.scope() == TextTransformScope.CLUSTER ? clusterBounds : glyphBounds;
        return transform(x, y, pivotBounds.centerX(), pivotBounds.centerY());
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

    private void flushPendingIncompleteVertex() {
        if (pendingVertex != null) {
            flushPendingVertex();
        }
    }

    private void flushPendingVertex() {
        if (pendingVertex == null) {
            return;
        }
        if (!preserveMirroredWinding) {
            write(pendingVertex);
            pendingVertex = null;
            return;
        }
        quad[quadSize++] = pendingVertex;
        pendingVertex = null;
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
        delegate.vertex(vertex.x, vertex.y, vertex.z);
        delegate.color(color);
        if (vertex.hasTexture) {
            delegate.texture(vertex.u, vertex.v);
        }
        if (vertex.hasOverlay) {
            delegate.overlay(vertex.overlayU, vertex.overlayV);
        }
        if (vertex.hasLight) {
            delegate.light(vertex.lightU, vertex.lightV);
        }
        if (vertex.hasNormal) {
            delegate.normal(vertex.normalX, vertex.normalY, vertex.normalZ);
        }
    }

    private static final class CapturedVertex {
        private final float x;
        private final float y;
        private final float z;
        private float u;
        private float v;
        private int overlayU;
        private int overlayV;
        private int lightU;
        private int lightV;
        private float normalX;
        private float normalY;
        private float normalZ;
        private boolean hasTexture;
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
