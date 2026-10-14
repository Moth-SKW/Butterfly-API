package moth.butterflyapi.client.outline.internal;

//? if >=1.21.11 {
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
//? if >=26.3 {
/*
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
*///?} else {
import com.mojang.blaze3d.pipeline.RenderPipeline;
//? if >=26.2 {
/*
import com.mojang.blaze3d.PrimitiveTopology;
*///?} else {
import com.mojang.blaze3d.vertex.VertexFormat;
//?}
//?}
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
//?}

/** GUI alpha-mask pipelines used by {@link GuiItemOutlinePass}. */
public final class GuiItemOutlinePipelines {
//? if >=1.21.11 {
    private static final Identifier SILHOUETTE_LOCATION = Identifier.fromNamespaceAndPath(
            "butterfly_api", "pipeline/gui_item_outline_silhouette"
    );
    private static final Identifier INNER_EDGE_LOCATION = Identifier.fromNamespaceAndPath(
            "butterfly_api", "pipeline/gui_item_outline_inner_"
    );
    private static final Identifier SILHOUETTE_SHADER = Identifier.fromNamespaceAndPath(
            "butterfly_api", "core/gui_item_outline_silhouette"
    );
    private static final Identifier INNER_EDGE_SHADER = Identifier.fromNamespaceAndPath(
            "butterfly_api", "core/gui_item_outline_inner"
    );

    private static RenderPipeline silhouette;
    private static final RenderPipeline[] INNER_EDGES = new RenderPipeline[10];
//?}

    private GuiItemOutlinePipelines() {
    }

    public static synchronized void initialize() {
//? if >=1.21.11 {
        if (silhouette != null) {
            return;
        }
        silhouette = createPipeline(SILHOUETTE_LOCATION, SILHOUETTE_SHADER, 0);
        for (int thickness = 1; thickness < INNER_EDGES.length; thickness++) {
            INNER_EDGES[thickness] = createPipeline(
                    Identifier.fromNamespaceAndPath(
                            INNER_EDGE_LOCATION.getNamespace(),
                            INNER_EDGE_LOCATION.getPath() + thickness
                    ),
                    INNER_EDGE_SHADER,
                    thickness
            );
        }
//?}
    }

//? if >=1.21.11 {
    private static RenderPipeline createPipeline(Identifier location, Identifier shader, int thickness) {
//? if >=26.3 {
/*
        RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                .withLocation(location)
                .withFragmentShader(shader)
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
                .withPrimitiveTopology(PrimitiveTopology.QUADS);
*///?} else if >=26.2 {
/*
        RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                .withLocation(location)
                .withFragmentShader(shader)
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
                .withPrimitiveTopology(PrimitiveTopology.QUADS);
*///?} else {
        RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                .withLocation(location)
                .withFragmentShader(shader)
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS);
//?}
        if (thickness > 0) {
            builder.withShaderDefine("BUTTERFLY_API_GUI_ITEM_OUTLINE_THICKNESS", thickness);
        }
        return builder.build();
    }

    public static RenderPipeline pipeline(GuiItemOutlinePass pass) {
        initialize();
        return pass.kind() == GuiItemOutlinePass.Kind.SILHOUETTE
                ? silhouette
                : INNER_EDGES[pass.thickness()];
    }
//?}
}
