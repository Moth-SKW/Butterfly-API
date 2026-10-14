package moth.butterflyapi.client.mixin;

//? if >=1.21.1 {

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CreativeModeInventoryScreen.ItemPickerMenu.class)
public interface CreativeScreenHandlerAccessor {
    @Accessor("items")
    NonNullList<ItemStack> butterflyApi$getItemList();

    @Invoker("getRowIndexForScroll")
    int butterflyApi$getVisibleRow(float scrollPosition);
}
//?}
