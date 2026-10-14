package moth.butterflyapi.item;

import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public final class ItemUtils {
    public static final double PLAYER_BASE_ATTACK_SPEED = 4.0D;

    private ItemUtils() {
    }

    public static double attackSpeedModifier(double displayedAttackSpeed) {
        return displayedAttackSpeed - PLAYER_BASE_ATTACK_SPEED;
    }

    public static Item.Properties displayedAttackSpeed(Item.Properties settings, Identifier modifierId, double displayedAttackSpeed) {
        Objects.requireNonNull(settings, "settings");
        return settings.attributes(attackSpeedAttributes(modifierId, displayedAttackSpeed));
    }

    public static Item.Properties combatAttributes(Item.Properties settings, Identifier modifierId, double attackDamage, double displayedAttackSpeed) {
        Objects.requireNonNull(settings, "settings");
        return settings.attributes(combatAttributes(modifierId, attackDamage, displayedAttackSpeed));
    }

    public static ItemAttributeModifiers attackSpeedAttributes(Identifier modifierId, double displayedAttackSpeed) {
        Identifier checkedId = Objects.requireNonNull(modifierId, "modifierId");
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                checkedId.withSuffix("_attack_speed"),
                                attackSpeedModifier(displayedAttackSpeed),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    public static ItemAttributeModifiers combatAttributes(Identifier modifierId, double attackDamage, double displayedAttackSpeed) {
        Identifier checkedId = Objects.requireNonNull(modifierId, "modifierId");
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                checkedId.withSuffix("_attack_damage"),
                                attackDamage,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                checkedId.withSuffix("_attack_speed"),
                                attackSpeedModifier(displayedAttackSpeed),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }
}
