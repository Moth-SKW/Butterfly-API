package moth.butterflyapi.math;

import java.util.Objects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class Motion {
    private Motion() {
    }

    public static Vec3 setVelocity(Entity entity, Vec3 velocity) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(velocity, "velocity");
        entity.setDeltaMovement(velocity);
        return velocity;
    }

    public static Vec3 addVelocity(Entity entity, Vec3 delta) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(delta, "delta");
        entity.push(delta.x, delta.y, delta.z);
        return entity.getDeltaMovement();
    }

    public static Vec3 lookDirection(Entity entity) {
        return Angles.lookDirection(entity);
    }

    public static Vec3 velocityFromLook(Entity entity, double speed) {
        return lookDirection(entity).scale(speed);
    }

    public static Vec3 setVelocityFromLook(Entity entity, double speed) {
        return setVelocity(entity, velocityFromLook(entity, speed));
    }

    public static Vec3 addVelocityFromLook(Entity entity, double speed) {
        return addVelocity(entity, velocityFromLook(entity, speed));
    }

    public static Vec3 velocityToward(Vec3 from, Vec3 to, double speed) {
        return Vecs.direction(from, to).scale(speed);
    }

    public static Vec3 velocityToward(Entity from, Vec3 to, double speed) {
        Objects.requireNonNull(from, "from");
        return velocityToward(position(from), to, speed);
    }

    public static Vec3 velocityToward(Entity from, Entity to, double speed) {
        Objects.requireNonNull(to, "to");
        return velocityToward(from, position(to), speed);
    }

    public static Vec3 setVelocityToward(Entity entity, Vec3 target, double speed) {
        Objects.requireNonNull(entity, "entity");
        return setVelocity(entity, velocityToward(position(entity), Objects.requireNonNull(target, "target"), speed));
    }

    public static Vec3 setVelocityToward(Entity entity, Entity target, double speed) {
        Objects.requireNonNull(target, "target");
        return setVelocityToward(entity, position(target), speed);
    }

    public static Vec3 addVelocityToward(Entity entity, Vec3 target, double speed) {
        Objects.requireNonNull(entity, "entity");
        return addVelocity(entity, velocityToward(position(entity), Objects.requireNonNull(target, "target"), speed));
    }

    public static Vec3 addVelocityToward(Entity entity, Entity target, double speed) {
        Objects.requireNonNull(target, "target");
        return addVelocityToward(entity, position(target), speed);
    }

    public static Vec3 position(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        return new Vec3(entity.getX(), entity.getY(), entity.getZ());
    }

    public static Vec3 impulse(Entity entity, Vec3 direction, double strength) {
        return impulse(entity, direction, strength, 0.0D);
    }

    public static Vec3 impulse(Entity entity, Vec3 direction, double horizontalStrength, double verticalStrength) {
        Objects.requireNonNull(entity, "entity");
        Vec3 flatDirection = Vecs.safeNormalize(Vecs.horizontal(Objects.requireNonNull(direction, "direction")));
        Vec3 delta = new Vec3(
                flatDirection.x * horizontalStrength,
                verticalStrength,
                flatDirection.z * horizontalStrength
        );
        entity.push(delta.x, delta.y, delta.z);
        return delta;
    }

    public static Vec3 knockback(LivingEntity entity, Vec3 direction, double horizontalStrength, double verticalStrength) {
        return impulse(entity, direction, horizontalStrength, verticalStrength);
    }

    public static Vec3 pushFrom(Entity entity, Vec3 origin, double horizontalStrength, double verticalStrength) {
        Objects.requireNonNull(entity, "entity");
        Vec3 entityPos = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        return impulse(entity, entityPos.subtract(Objects.requireNonNull(origin, "origin")), horizontalStrength, verticalStrength);
    }

    public static Vec3 pushFrom(Entity entity, Entity source, double horizontalStrength, double verticalStrength) {
        Entity checkedSource = Objects.requireNonNull(source, "source");
        return pushFrom(entity, new Vec3(checkedSource.getX(), checkedSource.getY(), checkedSource.getZ()), horizontalStrength, verticalStrength);
    }

    public static Vec3 pullToward(Entity entity, Vec3 target, double horizontalStrength, double verticalStrength) {
        Objects.requireNonNull(entity, "entity");
        Vec3 entityPos = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        return impulse(entity, Objects.requireNonNull(target, "target").subtract(entityPos), horizontalStrength, verticalStrength);
    }

    public static Vec3 pullToward(Entity entity, Entity target, double horizontalStrength, double verticalStrength) {
        Entity checkedTarget = Objects.requireNonNull(target, "target");
        return pullToward(entity, new Vec3(checkedTarget.getX(), checkedTarget.getY(), checkedTarget.getZ()), horizontalStrength, verticalStrength);
    }
}
