package moth.butterflyapi.client.outline;

import moth.butterflyapi.outline.BlockOutlineSettings;
import moth.butterflyapi.outline.OutlineStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
//? if >=1.21.11 && <26.2 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
//?}
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public final class OutlineRenderBridge {
//? if <26.2 {
    private static final int PACKED_STYLE_MARKER = 1 << 11;
    private static final int PACKED_VISIBLE_THROUGH_WALLS = 1 << 10;
    private static final int PACKED_PRESERVE_INSIDE = 1 << 9;
    private static final int PACKED_BLACK_BORDER = 1 << 8;
    private static final int PACKED_PLACEMENT_SHIFT = 6;
    private static final int PACKED_THICKNESS_MASK = 0x3F;
//?}
//? if >=1.21.11 && <26.2 {
    private static final Identifier BLOCK_ATLAS_TEXTURE = Identifier.withDefaultNamespace("textures/atlas/blocks.png");
//?}
    private static final Object2FloatMap<java.util.UUID> ENTITY_THICKNESSES = new Object2FloatOpenHashMap<>();
    private static final Long2FloatMap BLOCK_ENTITY_THICKNESSES = new Long2FloatOpenHashMap();

    @Nullable
    private static OutlineStyle currentStyle;
//? if >=1.21.11 && <26.2 {
    @Nullable
    private static BlockOutlineSettings currentBlockSettings;
//?}
    private static boolean entityCaptureSession;
    private static boolean maskOnlyCapture;
    private static long outlineFrameId;
    private static long maskPassId;
//? if >=1.21.11 && <26.2 {
    @Nullable
    private static RenderType blockMaskLayer;
    @Nullable
    private static RenderType blockMaskThroughWallsLayer;
//?}

    private OutlineRenderBridge() {
    }

    public static void initialize() {
//? if >=1.21.11 && <26.2 {
        if (blockMaskLayer == null) {
            blockMaskLayer = createBlockMaskLayer(
                    "butterfly_api_outline_block_mask",
                    Identifier.fromNamespaceAndPath("butterfly_api", "pipeline/outline_block_mask"),
                    DepthTestFunction.LEQUAL_DEPTH_TEST
            );
            blockMaskThroughWallsLayer = createBlockMaskLayer(
                    "butterfly_api_outline_block_mask_through_walls",
                    Identifier.fromNamespaceAndPath("butterfly_api", "pipeline/outline_block_mask_through_walls"),
                    DepthTestFunction.NO_DEPTH_TEST
            );
        }
//?}
    }

//? if >=1.21.11 && <26.2 {
    private static RenderType createBlockMaskLayer(String name, Identifier location, DepthTestFunction depthTest) {
        RenderPipeline pipeline = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                .withLocation(location)
                .withFragmentShader(Identifier.fromNamespaceAndPath("butterfly_api", "core/outline_mask"))
                .withDepthTestFunction(depthTest)
                .withDepthWrite(false)
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
                .build();
        RenderSetup setup = RenderSetup.builder(pipeline)
                .setOutputTarget(OutputTarget.OUTLINE_TARGET)
                .setOutline(RenderSetup.OutlineProperty.IS_OUTLINE)
                .bufferSize(256)
                .createRenderSetup();
        return moth.butterflyapi.client.mixin.RenderLayerAccessor.butterflyApi$of(name, setup);
    }
//?}

    public static void beginFrame() {
        outlineFrameId++;
//? if >=1.21.11 && <26.2 {
        currentBlockSettings = null;
//?}
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
//? if >=1.21.11 && <26.2 {
        currentBlockSettings = null;
//?}
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

//? if >=1.21.11 && <26.2 {
    public static void beginBlockMask(BlockOutlineSettings settings) {
        currentBlockSettings = Objects.requireNonNull(settings, "settings");
    }

    public static void endBlockMask() {
        currentBlockSettings = null;
    }
//?}

    @Nullable
    public static OutlineStyle currentStyle() {
        return currentStyle;
    }

//? if >=1.21.11 && <26.2 {
    public static void configure(OutlineBufferSource provider, OutlineStyle style) {
        provider.setColor(encodeStyle(style, style.thickness(), false));
    }
//?}

    public static int encodedColor(Entity target, OutlineStyle style, Vec3 camera) {
//? if >=1.21.11 && <26.2 {
        return encodeStyle(style, effectiveThickness(target, style, camera), false);
//?} else {
/*
        return 0xFF000000 | (style.rgb() & 0xFFFFFF);
*///?}
    }

    public static int encodedColor(BlockEntity target, OutlineStyle style, Vec3 camera) {
//? if >=1.21.11 && <26.2 {
        return encodeStyle(style, effectiveThickness(target, style, camera), false);
//?} else {
/*
        return 0xFF000000 | (style.rgb() & 0xFFFFFF);
*///?}
    }

//? if <1.21.11 {
/*
    // The legacy post chain uses alpha 254 for entities and 253 for block entities.
    public static int encodedLegacyEntityColor(Entity target, OutlineStyle style, Vec3 camera) {
        return encodeStyle(style, effectiveThickness(target, style, camera), false, 0xFE);
    }

    public static int encodedLegacyBlockColor(BlockEntity target, OutlineStyle style, Vec3 camera) {
        return encodeStyle(style, effectiveThickness(target, style, camera), false, 0xFD);
    }
*///?}

//? if >=1.21.11 && <26.2 {
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
//?}

    public static float effectiveThickness(Entity entity, OutlineStyle style, Vec3 camera) {
        double x = entity.getX() - camera.x;
        double y = entity.getY() - camera.y;
        double z = entity.getZ() - camera.z;
        return effectiveThickness(entity.getUUID(), style, x * x + y * y + z * z);
    }

    public static float effectiveThickness(BlockEntity blockEntity, OutlineStyle style, Vec3 camera) {
        long position = blockEntity.getBlockPos().asLong();
        double x = BlockPos.getX(position) - camera.x;
        double y = BlockPos.getY(position) - camera.y;
        double z = BlockPos.getZ(position) - camera.z;
        return effectiveThickness(position, style, x * x + y * y + z * z);
    }

    public static void clearAdaptiveThickness() {
        ENTITY_THICKNESSES.clear();
        BLOCK_ENTITY_THICKNESSES.clear();
    }

    public static void clearAdaptiveThickness(Entity entity) {
        ENTITY_THICKNESSES.removeFloat(entity.getUUID());
    }

    public static void clearAdaptiveThickness(BlockEntity blockEntity) {
        BLOCK_ENTITY_THICKNESSES.remove(blockEntity.getBlockPos().asLong());
    }

    private static float effectiveThickness(java.util.UUID key, OutlineStyle style, double distanceSquared) {
        if (style.consistentThickness()) {
            ENTITY_THICKNESSES.removeFloat(key);
            return style.thickness();
        }

        float target = adaptiveTarget(style, distanceSquared);
        float previous = ENTITY_THICKNESSES.getFloat(key);
        if (!ENTITY_THICKNESSES.containsKey(key)) {
            previous = target;
        }
        float resolved = previous + (target - previous) * style.thicknessAdaptationRate();
        ENTITY_THICKNESSES.put(key, resolved);
        return resolved;
    }

    private static float effectiveThickness(long key, OutlineStyle style, double distanceSquared) {
        if (style.consistentThickness()) {
            BLOCK_ENTITY_THICKNESSES.remove(key);
            return style.thickness();
        }

        float target = adaptiveTarget(style, distanceSquared);
        float previous = BLOCK_ENTITY_THICKNESSES.get(key);
        if (!BLOCK_ENTITY_THICKNESSES.containsKey(key)) {
            previous = target;
        }
        float resolved = previous + (target - previous) * style.thicknessAdaptationRate();
        BLOCK_ENTITY_THICKNESSES.put(key, resolved);
        return resolved;
    }

    private static float adaptiveTarget(OutlineStyle style, double distanceSquared) {
        double distance = Math.sqrt(Math.max(0.0D, distanceSquared));
        return Math.max(0.25F, (float) (style.thickness()
                * Math.min(1.0D, OutlineStyle.DEFAULT_THICKNESS_REFERENCE_DISTANCE / Math.max(1.0D, distance))));
    }

//? if >=1.21.11 && <26.2 {
    @Nullable
    public static RenderType maskLayer(RenderType source, OutlineStyle style) {
        return maskLayer(source, style, null);
    }

    @Nullable
    public static RenderType maskLayer(
            RenderType source,
            OutlineStyle style,
            @Nullable Identifier fallbackTexture
    ) {
        if (source.isOutline()) {
            return source;
        }
        if (style.matchModelLayer()) {
            if (source.outline().isPresent()) {
                return source.outline().get();
            }
        }
        Identifier texture = fallbackTexture != null
                ? fallbackTexture
                : BLOCK_ATLAS_TEXTURE;
        return RenderTypes.outline(texture);
    }

    public static Identifier blockAtlasTexture() {
        return BLOCK_ATLAS_TEXTURE;
    }

    public static OutlineBufferSource outlineProvider() {
        return Minecraft.getInstance().renderBuffers().outlineBufferSource();
    }

    public static RenderType blockMaskLayer(int encodedColor) {
        initialize();
        if ((packedStyleBits(encodedColor) & PACKED_VISIBLE_THROUGH_WALLS) != 0) {
            return Objects.requireNonNull(blockMaskThroughWallsLayer, "through-walls block mask layer");
        }
        return Objects.requireNonNull(blockMaskLayer, "block mask layer");
    }
//?}

//? if <26.2 {
    private static int encodeStyle(OutlineStyle style, float thickness, boolean preserveInsideInterior) {
        return encodeStyle(style, thickness, preserveInsideInterior, 0xFE);
    }

    private static int encodeStyle(OutlineStyle style, float thickness, boolean preserveInsideInterior, int alpha) {
        int resolvedThickness = Math.round(thickness);
        resolvedThickness = Math.max(OutlineStyle.MIN_THICKNESS, Math.min(OutlineStyle.MAX_THICKNESS, resolvedThickness));
        int packed = PACKED_STYLE_MARKER
                | ((resolvedThickness - 1) & PACKED_THICKNESS_MASK)
                | ((style.placement().id() & 0x3) << PACKED_PLACEMENT_SHIFT)
                | (style.blackBorder() ? PACKED_BLACK_BORDER : 0)
                | (style.visibleThroughWalls() ? PACKED_VISIBLE_THROUGH_WALLS : 0)
                | (preserveInsideInterior ? PACKED_PRESERVE_INSIDE : 0);
        return withPackedStyleBits((alpha << 24) | (style.rgb() & 0x00FFFFFF), packed);
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
//?}
}
