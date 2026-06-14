package moth.butterflyapi.plush;

import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class PlushGeoRenderer extends GeoBlockRenderer<PlushBlockEntity> {
    public PlushGeoRenderer() {
        super(new PlushGeoModel());
    }
}
