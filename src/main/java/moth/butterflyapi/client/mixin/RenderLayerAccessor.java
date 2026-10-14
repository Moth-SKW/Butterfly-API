package moth.butterflyapi.client.mixin;

//? if >=1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderType.class)
public interface RenderLayerAccessor {
    @Invoker("create")
    static RenderType butterflyApi$of(String name, RenderSetup setup) {
        throw new AssertionError();
    }
}
//?}
