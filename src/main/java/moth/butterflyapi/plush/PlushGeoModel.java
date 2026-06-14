package moth.butterflyapi.plush;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class PlushGeoModel extends GeoModel<PlushBlockEntity> {
    @Override
    public Identifier getModelResource(PlushBlockEntity animatable) {
        return geckoData(animatable).model();
    }

    @Override
    public Identifier getTextureResource(PlushBlockEntity animatable) {
        return geckoData(animatable).texture();
    }

    @Override
    public Identifier getAnimationResource(PlushBlockEntity animatable) {
        return geckoData(animatable).animation();
    }

    @Override
    public RenderLayer getRenderType(PlushBlockEntity animatable, Identifier texture) {
        return RenderLayer.getEntityCutoutNoCull(texture);
    }

    private PlushDefinition.GeckoRenderData geckoData(PlushBlockEntity animatable) {
        PlushDefinition.GeckoRenderData gecko = animatable.plushDefinition().geckolib();
        if (gecko == null) {
            throw new IllegalStateException("Plush " + animatable.plushDefinition().id() + " is not configured for GeckoLib rendering");
        }
        return gecko;
    }
}
