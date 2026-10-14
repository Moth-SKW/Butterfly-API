package moth.butterflyapi.outline;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;

public enum EntityOutlinePart {
    BODY,
    ARMOR_HEAD,
    ARMOR_CHEST,
    ARMOR_LEGS,
    ARMOR_FEET,
    MAIN_HAND,
    OFF_HAND,
    FEATURES;

    public static EntityOutlinePart armor(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> ARMOR_HEAD;
            case CHEST -> ARMOR_CHEST;
            case LEGS -> ARMOR_LEGS;
            case FEET -> ARMOR_FEET;
            default -> throw new IllegalArgumentException("Only armor equipment slots can be outlined individually: " + slot);
        };
    }

    public static EntityOutlinePart hand(HumanoidArm arm, HumanoidArm mainArm) {
        return arm == mainArm ? MAIN_HAND : OFF_HAND;
    }
}
