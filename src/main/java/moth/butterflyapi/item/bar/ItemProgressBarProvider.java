package moth.butterflyapi.item.bar;

import java.util.List;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ItemProgressBarProvider {
    List<ItemProgressBar> bars(ItemStack stack);
}
