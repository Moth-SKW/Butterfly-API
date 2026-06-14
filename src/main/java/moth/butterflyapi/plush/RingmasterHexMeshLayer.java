package moth.butterflyapi.plush;

import moth.butterflyapi.content.ContentBootstrap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class RingmasterHexMeshLayer extends GeoRenderLayer<PlushBlockEntity> {
    private static final Identifier PLUSH_ID = ContentBootstrap.id("ringmaster_hex_plush");
    private static final String HAT_BONE = "hat";
    private static final float TEXTURE_SIZE = 64.0F;
    private static final float MODEL_SCALE = 1.0F / 16.0F;
    private static final float MESH_ORIGIN_X = 4.0F;
    private static final float MESH_ORIGIN_Y = 14.9F;
    private static final float MESH_ORIGIN_Z = 3.0F;
    private static final Vector3f MESH_CENTER = new Vector3f(
            MESH_ORIGIN_X * MODEL_SCALE,
            (MESH_ORIGIN_Y + 3.0F) * MODEL_SCALE,
            MESH_ORIGIN_Z * MODEL_SCALE
    );

    private static final Vertex EX_QT = vertex(2.5F, 6.0F, 2.5F);
    private static final Vertex FOUR_AGV = vertex(2.5F, 6.0F, -2.5F);
    private static final Vertex BPAG = vertex(1.5F, 0.0F, 1.5F);
    private static final Vertex DXIN = vertex(1.5F, 0.0F, -1.5F);
    private static final Vertex X7XF = vertex(-2.5F, 6.0F, 2.5F);
    private static final Vertex SEVEN_FDM = vertex(-2.5F, 6.0F, -2.5F);
    private static final Vertex EIGHT_BUM = vertex(-1.5F, 0.0F, 1.5F);
    private static final Vertex Y17Y = vertex(-1.5F, 0.0F, -1.5F);

    private static final Face[] FACES = {
            face(
                    uv(FOUR_AGV, 23, 36),
                    uv(EX_QT, 18, 36),
                    uv(BPAG, 19, 42),
                    uv(DXIN, 22, 42)
            ),
            face(
                    uv(EIGHT_BUM, 28, 42),
                    uv(X7XF, 29, 36),
                    uv(SEVEN_FDM, 24, 36),
                    uv(Y17Y, 25, 42)
            ),
            face(
                    uv(X7XF, 42, 5),
                    uv(EX_QT, 47, 5),
                    uv(FOUR_AGV, 47, 0),
                    uv(SEVEN_FDM, 42, 0)
            ),
            face(
                    uv(DXIN, 33, 39),
                    uv(BPAG, 33, 36),
                    uv(EIGHT_BUM, 30, 36),
                    uv(Y17Y, 30, 39)
            ),
            face(
                    uv(BPAG, 22, 35),
                    uv(EX_QT, 23, 29),
                    uv(X7XF, 18, 29),
                    uv(EIGHT_BUM, 19, 35)
            ),
            face(
                    uv(SEVEN_FDM, 40, 34),
                    uv(FOUR_AGV, 35, 34),
                    uv(DXIN, 36, 40),
                    uv(Y17Y, 39, 40)
            )
    };

    public RingmasterHexMeshLayer(GeoRenderer<PlushBlockEntity> renderer) {
        super(renderer);
    }

    @Override
    public void renderForBone(
            MatrixStack matrices,
            PlushBlockEntity animatable,
            GeoBone bone,
            RenderLayer renderType,
            VertexConsumerProvider bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        if (!PLUSH_ID.equals(animatable.plushDefinition().id()) || !HAT_BONE.equals(bone.getName())) {
            return;
        }

        MatrixStack.Entry entry = matrices.peek();
        Matrix4f positionMatrix = entry.getPositionMatrix();
        Matrix3f normalMatrix = entry.getNormalMatrix();

        for (Face face : FACES) {
            drawFace(buffer, positionMatrix, normalMatrix, face, packedLight, packedOverlay);
        }
    }

    private static void drawFace(
            VertexConsumer buffer,
            Matrix4f positionMatrix,
            Matrix3f normalMatrix,
            Face face,
            int packedLight,
            int packedOverlay
    ) {
        TexturedVertex[] vertices = face.vertices();
        Vector3f first = vertices[0].vertex().position();
        Vector3f edgeA = new Vector3f(vertices[1].vertex().position()).sub(first);
        Vector3f edgeB = new Vector3f(vertices[2].vertex().position()).sub(first);
        Vector3f normal = edgeA.cross(edgeB).normalize();
        Vector3f faceCenter = new Vector3f();
        for (TexturedVertex vertex : vertices) {
            faceCenter.add(vertex.vertex().position());
        }
        faceCenter.div(vertices.length).sub(MESH_CENTER);
        if (normal.dot(faceCenter) < 0.0F) {
            normal.negate();
        }
        normalMatrix.transform(normal).normalize();

        for (TexturedVertex texturedVertex : vertices) {
            Vector4f position = new Vector4f(texturedVertex.vertex().position(), 1.0F);
            positionMatrix.transform(position);
            buffer.vertex(
                    position.x(),
                    position.y(),
                    position.z(),
                    1.0F,
                    1.0F,
                    1.0F,
                    1.0F,
                    texturedVertex.u() / TEXTURE_SIZE,
                    texturedVertex.v() / TEXTURE_SIZE,
                    packedOverlay,
                    packedLight,
                    normal.x(),
                    normal.y(),
                    normal.z()
            );
        }
    }

    private static Vertex vertex(float x, float y, float z) {
        return new Vertex(new Vector3f(
                (MESH_ORIGIN_X + x) * MODEL_SCALE,
                (MESH_ORIGIN_Y + y) * MODEL_SCALE,
                (MESH_ORIGIN_Z + z) * MODEL_SCALE
        ));
    }

    private static TexturedVertex uv(Vertex vertex, float u, float v) {
        return new TexturedVertex(vertex, u, v);
    }

    private static Face face(TexturedVertex... vertices) {
        return new Face(vertices);
    }

    private record Vertex(Vector3f position) {
    }

    private record TexturedVertex(Vertex vertex, float u, float v) {
    }

    private record Face(TexturedVertex[] vertices) {
    }
}
