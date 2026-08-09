# Math And Bootstrap

## Angles

```java
double yawDelta = Angles.deltaDegrees(entity.getYaw(), targetYaw);
Vec3d direction = Angles.lookDirection(entity);
YawPitch look = Angles.lookAt(entity.getEyePos(), target.getEyePos());
```

## Vectors

```java
Vec3d from = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
Vec3d to = target.getEyePos();

Vec3d towardTarget = Vecs.direction(from, to, Vec3d.ZERO);
Vec3d capped = Vecs.limitLength(entity.getVelocity(), 0.5D);
Vec3d reflected = Vecs.reflect(incoming, surfaceNormal);
```

## Boxes

```java
Box searchArea = Boxes.around(entity.getEyePos(), 8.0D);
Box beamArea = Boxes.between(start, end, 0.25D);
Vec3d center = Boxes.center(searchArea);
```

## Motion

```java
Motion.pushFrom(target, source, 0.8D, 0.2D);
Motion.pullToward(projectile, target, 0.4D, 0.0D);
Motion.setVelocityFromLook(projectile, 1.5D);
Motion.addVelocityToward(projectile, target, 0.25D);
```

## Sampling

```java
for (Vec3d point : Sampling.circle(center, new Vec3d(0.0D, 1.0D, 0.0D), 2.0D, 16)) {
    world.addParticle(particle, point.x, point.y, point.z, 0.0D, 0.0D, 0.0D);
}
```

## Scalars

```java
double progress = Scalars.clamp01(Scalars.inverseLerp(value, min, max));
double mapped = Scalars.mapClamped(distance, 0.0D, 32.0D, 1.0D, 0.0D);
double eased = Scalars.approach(current, target, step);
```

## Bootstrap

`Bootstrap` and `RunOnce` help keep initialization idempotent.

```java
import moth.butterflyapi.bootstrap.Bootstrap;

public final class ExampleBootstrap {
    private static final Bootstrap BOOTSTRAP = Bootstrap.create();

    public static void init() {
        BOOTSTRAP.run(
                ExampleItems::init,
                ExampleBlocks::init,
                ExampleSounds::init
        );
    }
}
```
