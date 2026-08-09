package moth.butterflyapi.item;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.Objects;

public final class ItemUtils {
    public static final double PLAYER_BASE_ATTACK_SPEED = 4.0D;

    private ItemUtils() {
    }

    public static double attackSpeedModifier(double displayedAttackSpeed) {
        return displayedAttackSpeed - PLAYER_BASE_ATTACK_SPEED;
    }

    public static Item.Settings displayedAttackSpeed(Item.Settings settings, Identifier modifierId, double displayedAttackSpeed) {
        Objects.requireNonNull(settings, "settings");
        return settings.attributeModifiers(attackSpeedAttributes(modifierId, displayedAttackSpeed));
    }

    public static Item.Settings combatAttributes(Item.Settings settings, Identifier modifierId, double attackDamage, double displayedAttackSpeed) {
        Objects.requireNonNull(settings, "settings");
        return settings.attributeModifiers(combatAttributes(modifierId, attackDamage, displayedAttackSpeed));
    }

    public static AttributeModifiersComponent attackSpeedAttributes(Identifier modifierId, double displayedAttackSpeed) {
        Identifier checkedId = Objects.requireNonNull(modifierId, "modifierId");
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(
                                checkedId.withSuffixedPath("_attack_speed"),
                                attackSpeedModifier(displayedAttackSpeed),
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }

    public static AttributeModifiersComponent combatAttributes(Identifier modifierId, double attackDamage, double displayedAttackSpeed) {
        Identifier checkedId = Objects.requireNonNull(modifierId, "modifierId");
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                checkedId.withSuffixedPath("_attack_damage"),
                                attackDamage,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(
                                checkedId.withSuffixedPath("_attack_speed"),
                                attackSpeedModifier(displayedAttackSpeed),
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }
}
