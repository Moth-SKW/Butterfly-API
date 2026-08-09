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

    public static Vec3d lookDirection(Entity entity) {
        return Angles.lookDirection(entity);
    }

    public static Vec3d velocityFromLook(Entity entity, double speed) {
        return lookDirection(entity).multiply(speed);
    }

    public static Vec3d setVelocityFromLook(Entity entity, double speed) {
        return setVelocity(entity, velocityFromLook(entity, speed));
    }

    public static Vec3d addVelocityFromLook(Entity entity, double speed) {
        return addVelocity(entity, velocityFromLook(entity, speed));
    }

    public static Vec3d velocityToward(Vec3d from, Vec3d to, double speed) {
        return Vecs.direction(from, to).multiply(speed);
    }

    public static Vec3d velocityToward(Entity from, Vec3d to, double speed) {
        Objects.requireNonNull(from, "from");
        return velocityToward(position(from), to, speed);
    }

    public static Vec3d velocityToward(Entity from, Entity to, double speed) {
        Objects.requireNonNull(to, "to");
        return velocityToward(from, position(to), speed);
    }

    public static Vec3d setVelocityToward(Entity entity, Vec3d target, double speed) {
        Objects.requireNonNull(entity, "entity");
        return setVelocity(entity, velocityToward(position(entity), Objects.requireNonNull(target, "target"), speed));
    }

    public static Vec3d setVelocityToward(Entity entity, Entity target, double speed) {
        Objects.requireNonNull(target, "target");
        return setVelocityToward(entity, position(target), speed);
    }

    public static Vec3d addVelocityToward(Entity entity, Vec3d target, double speed) {
        Objects.requireNonNull(entity, "entity");
        return addVelocity(entity, velocityToward(position(entity), Objects.requireNonNull(target, "target"), speed));
    }

    public static Vec3d addVelocityToward(Entity entity, Entity target, double speed) {
        Objects.requireNonNull(target, "target");
        return addVelocityToward(entity, position(target), speed);
    }

    public static Vec3d position(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        return new Vec3d(entity.getX(), entity.getY(), entity.getZ());
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
        Vec3d entityPos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
        return impulse(entity, entityPos.subtract(Objects.requireNonNull(origin, "origin")), horizontalStrength, verticalStrength);
    }

    public static Vec3d pushFrom(Entity entity, Entity source, double horizontalStrength, double verticalStrength) {
        Entity checkedSource = Objects.requireNonNull(source, "source");
        return pushFrom(entity, new Vec3d(checkedSource.getX(), checkedSource.getY(), checkedSource.getZ()), horizontalStrength, verticalStrength);
    }

    public static Vec3d pullToward(Entity entity, Vec3d target, double horizontalStrength, double verticalStrength) {
        Objects.requireNonNull(entity, "entity");
        Vec3d entityPos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
        return impulse(entity, Objects.requireNonNull(target, "target").subtract(entityPos), horizontalStrength, verticalStrength);
    }

    public static Vec3d pullToward(Entity entity, Entity target, double horizontalStrength, double verticalStrength) {
        Entity checkedTarget = Objects.requireNonNull(target, "target");
        return pullToward(entity, new Vec3d(checkedTarget.getX(), checkedTarget.getY(), checkedTarget.getZ()), horizontalStrength, verticalStrength);
    }
}
