package moth.butterflyapi.outline;

import net.minecraft.world.entity.EquipmentSlot;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class EntityOutline {
    private static final EntityOutline EMPTY = new EntityOutline(new EnumMap<>(EntityOutlinePart.class));

    private final Map<EntityOutlinePart, OutlineStyle> styles;

    private EntityOutline(EnumMap<EntityOutlinePart, OutlineStyle> styles) {
        this.styles = Collections.unmodifiableMap(styles);
    }

    public static EntityOutline empty() {
        return EMPTY;
    }

    public static EntityOutline attached(OutlineStyle style) {
        return builder().attached(style).build();
    }

    public static EntityOutline body(OutlineStyle style) {
        return builder().body(style).build();
    }

    public static EntityOutline armor(OutlineStyle style) {
        return builder().armor(style).build();
    }

    public static EntityOutline heldItems(OutlineStyle style) {
        return builder().heldItems(style).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<OutlineStyle> style(EntityOutlinePart part) {
        return Optional.ofNullable(styles.get(Objects.requireNonNull(part, "part")));
    }

    public Optional<OutlineStyle> armor(EquipmentSlot slot) {
        return style(EntityOutlinePart.armor(Objects.requireNonNull(slot, "slot")));
    }

    public Map<EntityOutlinePart, OutlineStyle> styles() {
        return styles;
    }

    public boolean isEmpty() {
        return styles.isEmpty();
    }

    public EntityOutline with(EntityOutlinePart part, OutlineStyle style) {
        EnumMap<EntityOutlinePart, OutlineStyle> copy = new EnumMap<>(styles);
        copy.put(Objects.requireNonNull(part, "part"), Objects.requireNonNull(style, "style"));
        return new EntityOutline(copy);
    }

    public EntityOutline without(EntityOutlinePart part) {
        EnumMap<EntityOutlinePart, OutlineStyle> copy = new EnumMap<>(styles);
        copy.remove(Objects.requireNonNull(part, "part"));
        return copy.isEmpty() ? EMPTY : new EntityOutline(copy);
    }

    public OutlineStyle primaryStyle() {
        OutlineStyle body = styles.get(EntityOutlinePart.BODY);
        if (body != null) {
            return body;
        }
        return styles.values().stream().findFirst().orElse(null);
    }

    public static final class Builder {
        private final EnumMap<EntityOutlinePart, OutlineStyle> styles = new EnumMap<>(EntityOutlinePart.class);

        public Builder part(EntityOutlinePart part, OutlineStyle style) {
            styles.put(Objects.requireNonNull(part, "part"), Objects.requireNonNull(style, "style"));
            return this;
        }

        public Builder body(OutlineStyle style) {
            return part(EntityOutlinePart.BODY, style);
        }

        public Builder armor(OutlineStyle style) {
            return armor(EquipmentSlot.HEAD, style)
                    .armor(EquipmentSlot.CHEST, style)
                    .armor(EquipmentSlot.LEGS, style)
                    .armor(EquipmentSlot.FEET, style);
        }

        public Builder armor(EquipmentSlot slot, OutlineStyle style) {
            return part(EntityOutlinePart.armor(Objects.requireNonNull(slot, "slot")), style);
        }

        public Builder heldItems(OutlineStyle style) {
            return mainHand(style).offHand(style);
        }

        public Builder mainHand(OutlineStyle style) {
            return part(EntityOutlinePart.MAIN_HAND, style);
        }

        public Builder offHand(OutlineStyle style) {
            return part(EntityOutlinePart.OFF_HAND, style);
        }

        public Builder features(OutlineStyle style) {
            return part(EntityOutlinePart.FEATURES, style);
        }

        public Builder attached(OutlineStyle style) {
            for (EntityOutlinePart part : EntityOutlinePart.values()) {
                part(part, style);
            }
            return this;
        }

        public EntityOutline build() {
            return styles.isEmpty() ? EMPTY : new EntityOutline(new EnumMap<>(styles));
        }
    }
}
