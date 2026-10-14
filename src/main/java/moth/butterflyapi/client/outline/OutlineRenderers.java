package moth.butterflyapi.client.outline;

//? if >=1.21.11 && <26.2 {
import moth.butterflyapi.outline.OutlineStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public final class OutlineRenderers {
    private OutlineRenderers() {
    }

    public static MultiBufferSource capture(
            MultiBufferSource original,
            Identifier texture,
            OutlineStyle style
    ) {
        Objects.requireNonNull(original, "original");
        Objects.requireNonNull(texture, "texture");
        Objects.requireNonNull(style, "style");

        OutlineBufferSource outlines = Minecraft.getInstance()
                .renderBuffers()
                .outlineBufferSource();
        OutlineRenderBridge.configure(outlines, style);

        return renderLayer -> {
            VertexConsumer normal = original.getBuffer(renderLayer);
            RenderType maskLayer = OutlineRenderBridge.maskLayer(renderLayer, style, texture);
            if (maskLayer == null) {
                return normal;
            }
            return combine(normal, outlines.getBuffer(maskLayer));
        };
    }

    public static MultiBufferSource captureAutomatic(
            MultiBufferSource original,
            OutlineBufferSource outlines,
            OutlineStyle style
    ) {
        return renderLayer -> {
            VertexConsumer normal = original.getBuffer(renderLayer);
            RenderType maskLayer = OutlineRenderBridge.maskLayer(renderLayer, style);
            if (maskLayer == null) {
                return normal;
            }
            return combine(normal, outlines.getBuffer(maskLayer));
        };
    }

    private static VertexConsumer combine(VertexConsumer normal, VertexConsumer outline) {
        return VertexMultiConsumer.create(outline, normal);
    }
}
//?} else {
/*
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class OutlineRenderers {
    private OutlineRenderers() {
    }
}
*///?}
