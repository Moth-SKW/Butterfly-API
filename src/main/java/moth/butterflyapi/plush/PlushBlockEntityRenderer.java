package moth.butterflyapi.plush;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Renders plush block models through a block-entity path so squish transforms are visible. */
public final class PlushBlockEntityRenderer implements BlockEntityRenderer<PlushBlockEntity, PlushBlockEntityRenderer.State> {
    private static final Identifier BLOCK_ATLAS_TEXTURE = Identifier.ofVanilla("textures/atlas/blocks.png");
    private static final RenderLayer PLUSH_CUTOUT = moth.butterflyapi.client.mixin.RenderLayerAccessor.butterflyApi$of(
            "butterfly_api_plush_cutout",
            RenderSetup.builder(RenderPipelines.CUTOUT_BLOCK)
                    .useLightmap()
                    .texture("Sampler0", BLOCK_ATLAS_TEXTURE, RenderLayers.BLOCK_SAMPLER)
                    .build()
    );
    private final Random random = Random.create();
    private final List<BlockModelPart> parts = new ArrayList<>();

    public PlushBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    public static boolean isPlushCutout(RenderLayer layer) {
        return layer == PLUSH_CUTOUT;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(
            PlushBlockEntity blockEntity,
            State state,
            float tickDelta,
            Vec3d cameraPos,
            @Nullable CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderState.updateBlockEntityRenderState(blockEntity, state, crumblingOverlay);
        state.squash = (float) blockEntity.squash;
    }

    @Override
    public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState camera) {
        BlockState blockState = state.blockState;
        if (blockState == null) {
            return;
        }

        double squash = Math.max(0.0, Math.min(state.squash, 2.0));
        float yScale = 1.0f - (float) Math.min(0.40, 0.40 * squash);
        float xzScale = 1.0f + (float) Math.min(0.25, 0.25 * squash);

        matrices.push();
        scaleAroundCenter(matrices, xzScale, yScale, xzScale);
        queue.submitCustom(
                matrices,
                PLUSH_CUTOUT,
                (entry, vertexConsumer) -> renderLitBlockModel(entry, vertexConsumer, blockState, state.pos)
        );
        matrices.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    private static void scaleAroundCenter(MatrixStack matrices, float x, float y, float z) {
        matrices.translate(0.5, 0.0, 0.5);
        matrices.scale(x, y, z);
        matrices.translate(-0.5, 0.0, -0.5);
    }

    private void renderLitBlockModel(
            MatrixStack.Entry entry,
            VertexConsumer vertexConsumer,
            BlockState blockState,
            BlockPos pos
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        BlockRenderView world = client.world;
        if (world == null) {
            return;
        }

        BlockStateModel model = client.getBlockRenderManager().getModel(blockState);
        random.setSeed(blockState.getRenderingSeed(pos));
        parts.clear();
        model.addParts(random, parts);

        MatrixStack renderMatrices = new MatrixStack();
        renderMatrices.peek().copy(entry);
        client.getBlockRenderManager().renderBlock(blockState, pos, world, renderMatrices, vertexConsumer, false, parts);
    }

    public static final class State extends BlockEntityRenderState {
        private float squash;
    }
}
