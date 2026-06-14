package moth.butterflyapi.plush;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public final class PlushItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private final PlushBlockEntity renderEntity;
    private final PlushBlockEntityRenderer renderer = new PlushBlockEntityRenderer();

    public PlushItemRenderer(RegisteredPlush plush) {
        Objects.requireNonNull(plush, "plush");
        this.renderEntity = new PlushBlockEntity(BlockPos.ORIGIN, plush.block().getDefaultState());
    }

    @Override
    public void render(
            ItemStack stack,
            ModelTransformationMode mode,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            int overlay
    ) {
        renderer.render(renderEntity, 0.0F, matrices, vertexConsumers, light, overlay);
    }
}
