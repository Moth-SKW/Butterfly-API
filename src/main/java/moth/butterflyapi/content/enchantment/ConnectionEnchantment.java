package moth.butterflyapi.content.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public final class ConnectionEnchantment extends Enchantment {
    private static final EquipmentSlot[] EQUIPMENT_SLOTS = new EquipmentSlot[]{
            EquipmentSlot.MAINHAND,
            EquipmentSlot.OFFHAND,
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    public ConnectionEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.BREAKABLE, EQUIPMENT_SLOTS);
    }

    @Override
    public int getMinPower(int level) {
        return 30;
    }

    @Override
    public int getMaxPower(int level) {
        return 80;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.isEnchantable();
    }

    @Override
    public boolean isTreasure() {
        return true;
    }

    @Override
    public boolean isAvailableForRandomSelection() {
        return false;
    }

    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return false;
    }
}
