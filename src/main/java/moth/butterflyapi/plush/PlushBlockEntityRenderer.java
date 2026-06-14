package moth.butterflyapi.plush;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class PlushBlockEntityRenderer implements BlockEntityRenderer<PlushBlockEntity> {
    private final PlushGeoRenderer geckoRenderer = new PlushGeoRenderer();

    public PlushBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(PlushBlockEntity be, float tickDelta, MatrixStack matrices, VertexConsumerProvider vcp, int light, int overlay) {
        if (be.getWorld() == null) {
            return;
        }

        double squash = Math.max(0.0, Math.min(be.squash, 2.0));
        float yScale = 1.0f - (float) Math.min(0.40, 0.40 * squash);
        float xzScale = 1.0f + (float) Math.min(0.25, 0.25 * squash);

        matrices.push();
        scaleAroundCenter(matrices, xzScale, yScale, xzScale);

        PlushDefinition.GeckoRenderData gecko = be.plushDefinition().geckolib();
        if (gecko != null) {
            if (gecko.scale() != 1.0f) {
                scaleAroundCenter(matrices, gecko.scale(), gecko.scale(), gecko.scale());
            }
            if (gecko.offsetX() != 0.0f || gecko.offsetY() != 0.0f || gecko.offsetZ() != 0.0f) {
                matrices.translate(gecko.offsetX(), gecko.offsetY(), gecko.offsetZ());
            }
            geckoRenderer.render(be, tickDelta, matrices, vcp, light, overlay);
        } else {
            renderVanillaModel(be, matrices, vcp, overlay);
        }

        matrices.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox(PlushBlockEntity be) {
        return true;
    }

    private static void scaleAroundCenter(MatrixStack matrices, float x, float y, float z) {
        matrices.translate(0.5, 0.0, 0.5);
        matrices.scale(x, y, z);
        matrices.translate(-0.5, 0.0, -0.5);
    }

    private static void renderVanillaModel(PlushBlockEntity be, MatrixStack matrices, VertexConsumerProvider vcp, int overlay) {
        BlockRenderManager brm = MinecraftClient.getInstance().getBlockRenderManager();
        var state = be.getCachedState();
        var layer = RenderLayers.getBlockLayer(state);
        var vb = vcp.getBuffer(layer);
        BlockPos pos = be.getPos();

        brm.getModelRenderer().render(
                be.getWorld(),
                brm.getModel(state),
                state,
                pos,
                matrices,
                vb,
                false,
                Random.create(),
                0L,
                overlay
        );
    }
}
