package moth.butterflyapi.content.mixin;

import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemGroup.Builder.class)
public interface ItemGroupBuilderAccessor {
    @Accessor("type")
    void butterflyApi$setType(ItemGroup.Type type);
}
