package moth.butterflyapi.item.bar;

import net.minecraft.item.ItemStack;

import java.util.List;

@FunctionalInterface
public interface ItemProgressBarProvider {
    List<ItemProgressBar> bars(ItemStack stack);
}
