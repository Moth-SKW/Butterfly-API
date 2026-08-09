package moth.butterflyapi.client.mixin;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CreativeInventoryScreen.CreativeScreenHandler.class)
public interface CreativeScreenHandlerAccessor {
    @Accessor("itemList")
    DefaultedList<ItemStack> butterflyApi$getItemList();

    @Invoker("getRow")
    int butterflyApi$getVisibleRow(float scrollPosition);
}
