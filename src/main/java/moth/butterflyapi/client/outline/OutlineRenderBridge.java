package moth.butterflyapi.client.outline;

import moth.butterflyapi.outline.BlockOutlineSettings;
import moth.butterflyapi.outline.OutlineStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.OutputTarget;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Small compatibility surface for outline renderers.
 *
 * <p>1.21.11 moved outline compositing into the frame graph. Vanilla now
 * consumes {@code EntityRenderState.outlineColor} and renders into its native
 * outline target, so this bridge deliberately uses the same target instead of
 * trying to bind framebuffers or construct legacy RenderLayer instances.</p>
 */
@Environment(EnvType.CLIENT)
public final class OutlineRenderBridge {
    private static final Identifier BLOCK_ATLAS_TEXTURE = Identifier.ofVanilla("textures/atlas/blocks.png");
    private static final int PACKED_STYLE_MARKER = 1 << 11;
    private static final int PACKED_VISIBLE_THROUGH_WALLS = 1 << 10;
    private static final int PACKED_PRESERVE_INSIDE = 1 << 9;
    private static final int PACKED_BLACK_BORDER = 1 << 8;
    private static final int PACKED_PLACEMENT_SHIFT = 6;
    private static final int PACKED_THICKNESS_MASK = 0x3F;
    private static final Map<java.util.UUID, Float> ENTITY_THICKNESSES = new HashMap<>();
    private static final Map<Long, Float> BLOCK_ENTITY_THICKNESSES = new HashMap<>();

    @Nullable
    private static OutlineStyle currentStyle;
    @Nullable
    private static BlockOutlineSettings currentBlockSettings;
    private static boolean entityCaptureSession;
    private static boolean maskOnlyCapture;
    private static long outlineFrameId;
    private static long maskPassId;
    @Nullable
    private static RenderLayer blockMaskLayer;
    @Nullable
    private static RenderLayer blockMaskThroughWallsLayer;

    private OutlineRenderBridge() {
    }

    /** Kept as an idempotent entry point for existing client bootstrap code. */
    public static void initialize() {
        if (blockMaskLayer == null) {
            blockMaskLayer = createBlockMaskLayer(
                    "butterfly_api_outline_block_mask",
                    Identifier.of("butterfly_api", "pipeline/outline_block_mask"),
                    DepthTestFunction.LEQUAL_DEPTH_TEST
            );
            blockMaskThroughWallsLayer = createBlockMaskLayer(
                    "butterfly_api_outline_block_mask_through_walls",
                    Identifier.of("butterfly_api", "pipeline/outline_block_mask_through_walls"),
                    DepthTestFunction.NO_DEPTH_TEST
            );
        }
    }

    private static RenderLayer createBlockMaskLayer(String name, Identifier location, DepthTestFunction depthTest) {
        RenderPipeline pipeline = RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                .withLocation(location)
                .withFragmentShader(Identifier.of("butterfly_api", "core/outline_mask"))
                .withDepthTestFunction(depthTest)
                .withDepthWrite(false)
                .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
                .build();
        RenderSetup setup = RenderSetup.builder(pipeline)
                .outputTarget(OutputTarget.OUTLINE_TARGET)
                .outlineMode(RenderSetup.OutlineMode.IS_OUTLINE)
                .expectedBufferSize(256)
                .build();
        return moth.butterflyapi.client.mixin.RenderLayerAccessor.butterflyApi$of(name, setup);
    }

    public static void beginFrame() {
        outlineFrameId++;
        currentBlockSettings = null;
    }

    public static long frameId() {
        return outlineFrameId;
    }

    public static void beginEntityCapture(@Nullable OutlineStyle style) {
        currentStyle = style;
        entityCaptureSession = style != null;
        maskOnlyCapture = false;
    }

    public static void beginMaskOnlyCapture(OutlineStyle style) {
        currentStyle = Objects.requireNonNull(style, "style");
        entityCaptureSession = true;
        maskOnlyCapture = true;
        maskPassId++;
    }

    public static void endEntityModelCapture() {
        currentStyle = null;
    }

    public static void endEntityCapture() {
        currentStyle = null;
        currentBlockSettings = null;
        entityCaptureSession = false;
        maskOnlyCapture = false;
    }

    public static boolean entityCaptureSession() {
        return entityCaptureSession;
    }

    public static boolean maskOnlyCapture() {
        return maskOnlyCapture;
    }

    public static long maskPassId() {
        return maskPassId;
    }

    public static void beginBlockMask(BlockOutlineSettings settings) {
        currentBlockSettings = Objects.requireNonNull(settings, "settings");
    }

    public static void endBlockMask() {
        currentBlockSettings = null;
    }

    @Nullable
    public static OutlineStyle currentStyle() {
        return currentStyle;
    }

    /** Sets the packed ARGB color expected by the current 1.21.11 outline provider. */
    public static void configure(OutlineVertexConsumerProvider provider, OutlineStyle style) {
        provider.setColor(encodeStyle(style, style.thickness(), false));
    }

    /** Encodes the style metadata carried by the native outline color channel. */
    public static int encodedColor(Entity target, OutlineStyle style, Vec3d camera) {
        return encodeStyle(style, effectiveThickness(target, style, camera), false);
    }

    /** Encodes the style metadata carried by the native outline color channel. */
    public static int encodedColor(BlockEntity target, OutlineStyle style, Vec3d camera) {
        return encodeStyle(style, effectiveThickness(target, style, camera), false);
    }

    /** Adds block-mask-only settings to an already packed outline color. */
    public static int applyBlockMaskSettings(int encodedColor, BlockOutlineSettings settings) {
        int packed = packedStyleBits(encodedColor);
        if ((packed & PACKED_STYLE_MARKER) == 0) {
            return encodedColor;
        }
        packed = settings.preserveInsideInterior()
                ? packed | PACKED_PRESERVE_INSIDE
                : packed & ~PACKED_PRESERVE_INSIDE;
        return withPackedStyleBits(encodedColor, packed);
    }

    public static float effectiveThickness(Entity entity, OutlineStyle style, Vec3d camera) {
        return effectiveThickness(
                entity.getUuid(),
                style,
                new Vec3d(entity.getX(), entity.getY(), entity.getZ()).squaredDistanceTo(camera),
                ENTITY_THICKNESSES
        );
    }

    public static float effectiveThickness(BlockEntity blockEntity, OutlineStyle style, Vec3d camera) {
        return effectiveThickness(
                blockEntity.getPos().asLong(),
                style,
                blockEntity.getPos().getSquaredDistance(camera.x, camera.y, camera.z),
                BLOCK_ENTITY_THICKNESSES
        );
    }

    public static void clearAdaptiveThickness() {
        ENTITY_THICKNESSES.clear();
        BLOCK_ENTITY_THICKNESSES.clear();
    }

    public static void clearAdaptiveThickness(Entity entity) {
        ENTITY_THICKNESSES.remove(entity.getUuid());
    }

    public static void clearAdaptiveThickness(BlockEntity blockEntity) {
        BLOCK_ENTITY_THICKNESSES.remove(blockEntity.getPos().asLong());
    }

    private static <K> float effectiveThickness(
            K key,
            OutlineStyle style,
            double distanceSquared,
            Map<K, Float> previousValues
    ) {
        if (style.consistentThickness()) {
            previousValues.remove(key);
            return style.thickness();
        }

        double distance = Math.sqrt(Math.max(0.0, distanceSquared));
        float target = (float) (style.thickness()
                * Math.min(1.0, OutlineStyle.DEFAULT_THICKNESS_REFERENCE_DISTANCE / Math.max(1.0, distance)));
        target = Math.max(0.25F, target);

        Float previous = previousValues.get(key);
        if (previous == null) {
            previous = target;
        }
        float resolved = previous + (target - previous) * style.thicknessAdaptationRate();
        previousValues.put(key, resolved);
        return resolved;
    }

    /**
     * Resolves a vanilla outline layer for callers that capture a model into
     * an OutlineVertexConsumerProvider. The provider only accepts outline
     * layers or layers explicitly marked as affecting outlines.
     */
    @Nullable
    public static RenderLayer maskLayer(RenderLayer source, OutlineStyle style) {
        return maskLayer(source, style, null);
    }

    @Nullable
    public static RenderLayer maskLayer(
            RenderLayer source,
            OutlineStyle style,
            @Nullable Identifier fallbackTexture
    ) {
        if (source.isOutline()) {
            return source;
        }
        if (style.matchModelLayer()) {
            if (source.getAffectedOutline().isPresent()) {
                return source.getAffectedOutline().get();
            }
        }
        Identifier texture = fallbackTexture != null
                ? fallbackTexture
                : BLOCK_ATLAS_TEXTURE;
        return RenderLayers.outlineNoCull(texture);
    }

    public static Identifier blockAtlasTexture() {
        return BLOCK_ATLAS_TEXTURE;
    }

    /** Returns the native outline provider used by the 1.21.11 frame graph. */
    public static OutlineVertexConsumerProvider outlineProvider() {
        return MinecraftClient.getInstance().getBufferBuilders().getOutlineVertexConsumers();
    }

    /** Render layer for custom exposed-face block masks in the native outline target. */
    public static RenderLayer blockMaskLayer(int encodedColor) {
        initialize();
        if ((packedStyleBits(encodedColor) & PACKED_VISIBLE_THROUGH_WALLS) != 0) {
            return Objects.requireNonNull(blockMaskThroughWallsLayer, "through-walls block mask layer");
        }
        return Objects.requireNonNull(blockMaskLayer, "block mask layer");
    }

    private static int encodeStyle(OutlineStyle style, float thickness, boolean preserveInsideInterior) {
        int resolvedThickness = Math.round(thickness);
        resolvedThickness = Math.max(OutlineStyle.MIN_THICKNESS, Math.min(OutlineStyle.MAX_THICKNESS, resolvedThickness));
        int packed = PACKED_STYLE_MARKER
                | ((resolvedThickness - 1) & PACKED_THICKNESS_MASK)
                | ((style.placement().id() & 0x3) << PACKED_PLACEMENT_SHIFT)
                | (style.blackBorder() ? PACKED_BLACK_BORDER : 0)
                | (style.visibleThroughWalls() ? PACKED_VISIBLE_THROUGH_WALLS : 0)
                | (preserveInsideInterior ? PACKED_PRESERVE_INSIDE : 0);
        return withPackedStyleBits(0xFE000000 | (style.rgb() & 0x00FFFFFF), packed);
    }

    private static int packedStyleBits(int color) {
        int red = (color >> 16) & 0x0F;
        int green = (color >> 8) & 0x0F;
        int blue = color & 0x0F;
        return (red << 8) | (green << 4) | blue;
    }

    private static int withPackedStyleBits(int color, int packed) {
        int red = (packed >> 8) & 0x0F;
        int green = (packed >> 4) & 0x0F;
        int blue = packed & 0x0F;
        return (color & 0xFFF0F0F0) | (red << 16) | (green << 8) | blue;
    }
}
