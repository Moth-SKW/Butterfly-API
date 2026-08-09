package moth.butterflyapi.client.outline;

import moth.butterflyapi.outline.OutlineStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.util.Identifier;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public final class OutlineRenderers {
    private OutlineRenderers() {
    }

    public static VertexConsumerProvider capture(
            VertexConsumerProvider original,
            Identifier texture,
            OutlineStyle style
    ) {
        Objects.requireNonNull(original, "original");
        Objects.requireNonNull(texture, "texture");
        Objects.requireNonNull(style, "style");

        OutlineVertexConsumerProvider outlines = MinecraftClient.getInstance()
                .getBufferBuilders()
                .getOutlineVertexConsumers();
        OutlineRenderBridge.configure(outlines, style);

        return renderLayer -> {
            VertexConsumer normal = original.getBuffer(renderLayer);
            RenderLayer maskLayer = OutlineRenderBridge.maskLayer(renderLayer, style, texture);
            if (maskLayer == null) {
                return normal;
            }
            return combine(normal, outlines.getBuffer(maskLayer));
        };
    }

    public static VertexConsumerProvider captureAutomatic(
            VertexConsumerProvider original,
            OutlineVertexConsumerProvider outlines,
            OutlineStyle style
    ) {
        return renderLayer -> {
            VertexConsumer normal = original.getBuffer(renderLayer);
            RenderLayer maskLayer = OutlineRenderBridge.maskLayer(renderLayer, style);
            if (maskLayer == null) {
                return normal;
            }
            return combine(normal, outlines.getBuffer(maskLayer));
        };
    }

    private static VertexConsumer combine(VertexConsumer normal, VertexConsumer outline) {
        return VertexConsumers.union(outline, normal);
    }
}
