package moth.butterflyapi.plush;

//? if >=1.21.11 {
import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
//? if >=26.2 {
/*
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.texture.OverlayTexture;
*///?}
//? if <26.1 {
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
//?}
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Renders plush block models through a block-entity path so squish transforms are visible. */
public final class PlushBlockEntityRenderer implements BlockEntityRenderer<PlushBlockEntity, PlushBlockEntityRenderer.State> {
//? if >=26.2 {
/*
    private final BlockModelResolver blockModelResolver;
*///?}
//? if <26.1 {
    private static final Identifier BLOCK_ATLAS_TEXTURE = Identifier.withDefaultNamespace("textures/atlas/blocks.png");
    private static final RenderType PLUSH_CUTOUT = moth.butterflyapi.client.mixin.RenderLayerAccessor.butterflyApi$of(
            "butterfly_api_plush_cutout",
            RenderSetup.builder(RenderPipelines.CUTOUT_BLOCK)
                    .useLightmap()
                    .withTexture("Sampler0", BLOCK_ATLAS_TEXTURE, RenderTypes.MOVING_BLOCK_SAMPLER)
                    .createRenderSetup()
    );
    private final RandomSource random = RandomSource.create();
    private final List<BlockModelPart> parts = new ArrayList<>();
//?}

    public PlushBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
//? if >=26.2 {
/*
        blockModelResolver = context.blockModelResolver();
*///?}
    }

    public static boolean isPlushCutout(RenderType layer) {
//? if >=26.1 {
/*
        // Block models submit through the native moving-block path in 26.1.
        return false;
*///?} else {
        return layer == PLUSH_CUTOUT;
//?}
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
            PlushBlockEntity blockEntity,
            State state,
            float tickDelta,
            Vec3 cameraPos,
            @Nullable CrumblingOverlay crumblingOverlay
    ) {
//? if >=26.1 {
/*
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        state.squash = (float) blockEntity.interpolatedSquash(tickDelta);
        state.movingBlock = null;

        if (blockEntity.getLevel() instanceof net.minecraft.client.multiplayer.ClientLevel level) {
            BlockPos pos = blockEntity.getBlockPos();
            net.minecraft.client.renderer.block.MovingBlockRenderState movingBlock = new net.minecraft.client.renderer.block.MovingBlockRenderState();
            movingBlock.randomSeedPos = pos;
            movingBlock.blockPos = pos;
            movingBlock.blockState = blockEntity.getBlockState();
            movingBlock.biome = level.getBiome(pos);
            movingBlock.cardinalLighting = level.cardinalLighting();
            movingBlock.lightEngine = level.getLightEngine();
            state.movingBlock = movingBlock;
        }
*///?}
//? if >=26.2 {
/*
        state.blockModel.clear();
        blockModelResolver.update(state.blockModel, blockEntity.getBlockState(), BlockDisplayContext.create());
*///?}
//? if <26.1 {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        state.squash = (float) blockEntity.interpolatedSquash(tickDelta);
//?}
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState camera) {
//? if >=26.2 {
/*
        if (state.movingBlock == null) {
            return;
        }

        double squash = Math.max(0.0, Math.min(state.squash, 2.0));
        float yScale = 1.0f - (float) Math.min(0.40, 0.40 * squash);
        float xzScale = 1.0f + (float) Math.min(0.25, 0.25 * squash);

        matrices.pushPose();
        scaleAroundCenter(matrices, xzScale, yScale, xzScale);
        state.blockModel.submit(
                matrices,
                queue,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0xFFFFFFFF
        );
        matrices.popPose();
*///?}
//? if >=26.1 && <26.2 {
/*
        if (state.movingBlock == null) {
            return;
        }

        double squash = Math.max(0.0, Math.min(state.squash, 2.0));
        float yScale = 1.0f - (float) Math.min(0.40, 0.40 * squash);
        float xzScale = 1.0f + (float) Math.min(0.25, 0.25 * squash);

        matrices.pushPose();
        scaleAroundCenter(matrices, xzScale, yScale, xzScale);
        queue.submitMovingBlock(matrices, state.movingBlock
        );
        matrices.popPose();
*///?}
//? if <26.1 {
        BlockState blockState = state.blockState;
        if (blockState == null) {
            return;
        }

        double squash = Math.max(0.0, Math.min(state.squash, 2.0));
        float yScale = 1.0f - (float) Math.min(0.40, 0.40 * squash);
        float xzScale = 1.0f + (float) Math.min(0.25, 0.25 * squash);

        matrices.pushPose();
        scaleAroundCenter(matrices, xzScale, yScale, xzScale);
        queue.submitCustomGeometry(
                matrices,
                PLUSH_CUTOUT,
                (entry, vertexConsumer) -> renderLitBlockModel(entry, vertexConsumer, blockState, state.blockPos)
        );
        matrices.popPose();
//?}
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    private static void scaleAroundCenter(PoseStack matrices, float x, float y, float z) {
        matrices.translate(0.5, 0.0, 0.5);
        matrices.scale(x, y, z);
        matrices.translate(-0.5, 0.0, -0.5);
    }

//? if >=26.2 {
/*
    public static void submitOutline(State state, PoseStack matrices, SubmitNodeCollector queue) {
    }
*///?}

//? if <26.1 {
    private void renderLitBlockModel(
            PoseStack.Pose entry,
            VertexConsumer vertexConsumer,
            BlockState blockState,
            BlockPos pos
    ) {
        Minecraft client = Minecraft.getInstance();
        BlockAndTintGetter world = client.level;
        if (world == null) {
            return;
        }

        BlockStateModel model = client.getBlockRenderer().getBlockModel(blockState);
        random.setSeed(blockState.getSeed(pos));
        parts.clear();
        model.collectParts(random, parts);

        PoseStack renderMatrices = new PoseStack();
        renderMatrices.last().set(entry);
        client.getBlockRenderer().renderBatched(blockState, pos, world, renderMatrices, vertexConsumer, false, parts);
    }
//?}

    public static final class State extends BlockEntityRenderState {
        private float squash;
//? if >=26.1 {
/*
        private net.minecraft.client.renderer.block.MovingBlockRenderState movingBlock;
*///?}
//? if >=26.2 {
/*
        private final BlockModelRenderState blockModel = new BlockModelRenderState();
*///?}
    }
}
//?} else {
/*
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.RandomSource;

public final class PlushBlockEntityRenderer implements BlockEntityRenderer<PlushBlockEntity> {
    private final RandomSource random = RandomSource.create();

    public PlushBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static boolean isPlushCutout(RenderType layer) {
        return false;
    }

    @Override
    public void render(
            PlushBlockEntity blockEntity,
            float tickDelta,
            PoseStack matrices,
            MultiBufferSource buffers,
            int light,
            int overlay
    ) {
        double squash = Math.max(0.0, Math.min(blockEntity.interpolatedSquash(tickDelta), 2.0));
        float yScale = 1.0F - (float) Math.min(0.40, 0.40 * squash);
        float xzScale = 1.0F + (float) Math.min(0.25, 0.25 * squash);
        matrices.pushPose();
        matrices.translate(0.5, 0.0, 0.5);
        matrices.scale(xzScale, yScale, xzScale);
        matrices.translate(-0.5, 0.0, -0.5);
        Minecraft client = Minecraft.getInstance();
        if (client.level != null) {
            client.getBlockRenderer().renderBatched(
                    blockEntity.getBlockState(),
                    blockEntity.getBlockPos(),
                    client.level,
                    matrices,
                    buffers.getBuffer(RenderType.cutout()),
                    false,
                    random
            );
        }
        matrices.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(PlushBlockEntity blockEntity) {
        return true;
    }
}
*///?}
