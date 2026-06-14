package moth.butterflyapi.math;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public final class Motion {
    private Motion() {
    }

    public static Vec3d setVelocity(Entity entity, Vec3d velocity) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(velocity, "velocity");
        entity.setVelocity(velocity);
        return velocity;
    }

    public static Vec3d addVelocity(Entity entity, Vec3d delta) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(delta, "delta");
        entity.addVelocity(delta.x, delta.y, delta.z);
        return entity.getVelocity();
    }

    public static Vec3d impulse(Entity entity, Vec3d direction, double strength) {
        return impulse(entity, direction, strength, 0.0D);
    }

    public static Vec3d impulse(Entity entity, Vec3d direction, double horizontalStrength, double verticalStrength) {
        Objects.requireNonNull(entity, "entity");
        Vec3d flatDirection = Vecs.safeNormalize(Vecs.horizontal(Objects.requireNonNull(direction, "direction")));
        Vec3d delta = new Vec3d(
                flatDirection.x * horizontalStrength,
                verticalStrength,
                flatDirection.z * horizontalStrength
        );
        entity.addVelocity(delta.x, delta.y, delta.z);
        return delta;
    }

    public static Vec3d knockback(LivingEntity entity, Vec3d direction, double horizontalStrength, double verticalStrength) {
        return impulse(entity, direction, horizontalStrength, verticalStrength);
    }

    public static Vec3d pushFrom(Entity entity, Vec3d origin, double horizontalStrength, double verticalStrength) {
        Objects.requireNonNull(entity, "entity");
        return impulse(entity, entity.getPos().subtract(Objects.requireNonNull(origin, "origin")), horizontalStrength, verticalStrength);
    }

    public static Vec3d pushFrom(Entity entity, Entity source, double horizontalStrength, double verticalStrength) {
        return pushFrom(entity, Objects.requireNonNull(source, "source").getPos(), horizontalStrength, verticalStrength);
    }

    public static Vec3d pullToward(Entity entity, Vec3d target, double horizontalStrength, double verticalStrength) {
        Objects.requireNonNull(entity, "entity");
        return impulse(entity, Objects.requireNonNull(target, "target").subtract(entity.getPos()), horizontalStrength, verticalStrength);
    }

    public static Vec3d pullToward(Entity entity, Entity target, double horizontalStrength, double verticalStrength) {
        return pullToward(entity, Objects.requireNonNull(target, "target").getPos(), horizontalStrength, verticalStrength);
    }
}
