package moth.butterflyapi.client.particle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.concurrent.ThreadLocalRandom;
import moth.butterflyapi.ButterflyApi;
import moth.butterflyapi.particle.ParticleColor;
import moth.butterflyapi.particle.ParticleDefinition;
import moth.butterflyapi.particle.ParticleDefinitions;
import moth.butterflyapi.particle.ParticleVector;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.AbstractTexture;
//? if <1.21.11 {
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
//?}
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
final class ClientParticleManager {
    private static final int MAX_ACTIVE_EMITTERS = 128;
    private static final int MAX_ACTIVE_PARTICLES = 8192;
    private static final int MAX_PARTICLES_PER_TICK = 256;
    private static final Identifier DEFAULT_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "generic_0");
    private static final Identifier PARTICLE_ATLAS = Identifier.withDefaultNamespace("textures/atlas/particles.png");
    private static final List<Emitter> EMITTERS = new ArrayList<>();
    private static final List<TrackedParticle> PARTICLES = new ArrayList<>();
    private static final Set<Identifier> MISSING_TEXTURE_WARNINGS = new HashSet<>();

    private static ClientLevel activeLevel;

    private ClientParticleManager() {
    }

    static boolean spawn(Identifier definitionId, Vec3 position) {
        Objects.requireNonNull(definitionId, "definitionId");
        Objects.requireNonNull(position, "position");
        requireClientThread();
        observeLevel(Minecraft.getInstance().level);
        if (activeLevel == null || EMITTERS.size() >= MAX_ACTIVE_EMITTERS) {
            return false;
        }
        ParticleDefinition definition = ParticleDefinitions.get(definitionId).orElse(null);
        if (definition == null) {
            return false;
        }
        EMITTERS.add(new Emitter(definitionId, definition, position));
        return true;
    }

    static int activeEmitterCount() {
        return EMITTERS.size();
    }

    static int activeParticleCount() {
        return PARTICLES.size();
    }

    static void tick(Minecraft client) {
        requireClientThread();
        observeLevel(client.level);
        if (activeLevel == null) {
            return;
        }
        tickParticles();
        tickEmitters(client);
    }

    static void clear() {
        Minecraft client = Minecraft.getInstance();
        if (!client.isSameThread()) {
            client.execute(ClientParticleManager::clear);
            return;
        }
        for (TrackedParticle tracked : PARTICLES) {
            tracked.particle().remove();
        }
        PARTICLES.clear();
        EMITTERS.clear();
        MISSING_TEXTURE_WARNINGS.clear();
        activeLevel = client.level;
    }

    private static void tickParticles() {
        Iterator<TrackedParticle> iterator = PARTICLES.iterator();
        while (iterator.hasNext()) {
            TrackedParticle tracked = iterator.next();
            if (!tracked.particle().isAlive()) {
                iterator.remove();
                continue;
            }
            tracked.tick();
        }
    }

    private static void tickEmitters(Minecraft client) {
        Iterator<Emitter> iterator = EMITTERS.iterator();
        while (iterator.hasNext()) {
            Emitter emitter = iterator.next();
            if (!emitter.tick(client)) {
                iterator.remove();
            }
        }
    }

    private static DefinitionParticle createParticle(Minecraft client, Emitter emitter, RandomGenerator random) {
        TextureAtlasSprite sprite = sprite(client, emitter.definition().texture().orElse(DEFAULT_TEXTURE));
        if (sprite == null || client.level == null) {
            return null;
        }
        ParticleDefinition definition = emitter.definition();
        ParticleVector offset = definition.emissionShape().sample(random).add(definition.initialPosition());
        ParticleVector velocity = definition.initialVelocity();
        double lifetimeSeconds = definition.particleLifetimeSeconds().sample(random);
        float startSize = (float) definition.startSize().sample(random);
        float endSize = (float) definition.endSize().sample(random);
        DefinitionParticle particle = new DefinitionParticle(
                client.level,
                emitter.origin().x + offset.x(),
                emitter.origin().y + offset.y(),
                emitter.origin().z + offset.z(),
                velocity.x(),
                velocity.y(),
                velocity.z(),
                sprite,
                definition.startColor(),
                startSize
        );
        particle.setLifetime(Math.max(1, (int) Math.ceil(lifetimeSeconds * 20.0D)));
        client.particleEngine.add(particle);
        PARTICLES.add(new TrackedParticle(
                particle,
                definition.acceleration(),
                definition.startColor(),
                definition.endColor(),
                startSize,
                endSize
        ));
        return particle;
    }

    private static TextureAtlasSprite sprite(Minecraft client, Identifier textureId) {
        AbstractTexture texture = client.getTextureManager().getTexture(PARTICLE_ATLAS);
        if (!(texture instanceof TextureAtlas atlas)) {
            ButterflyApi.LOGGER.warn("Particle atlas is unavailable while spawning Butterfly particles");
            return null;
        }
        TextureAtlasSprite sprite = atlas.getSprite(textureId);
        if (
//? if <1.21.11 {
/*
                sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())
*///?} else {
                sprite == atlas.missingSprite()
//?}
        ) {
            if (MISSING_TEXTURE_WARNINGS.add(textureId)) {
                ButterflyApi.LOGGER.warn(
                        "Particle texture {} is not present in the particle atlas; using {} instead",
                        textureId,
                        DEFAULT_TEXTURE
                );
            }
            TextureAtlasSprite fallback = atlas.getSprite(DEFAULT_TEXTURE);
            return
//? if <1.21.11 {
/*
                    fallback.contents().name().equals(MissingTextureAtlasSprite.getLocation())
*///?} else {
                    fallback == atlas.missingSprite()
//?}
                    ? sprite : fallback;
        }
        return sprite;
    }

    private static void observeLevel(ClientLevel level) {
        if (activeLevel == level) {
            return;
        }
        for (TrackedParticle tracked : PARTICLES) {
            tracked.particle().remove();
        }
        PARTICLES.clear();
        EMITTERS.clear();
        MISSING_TEXTURE_WARNINGS.clear();
        activeLevel = level;
    }

    private static void requireClientThread() {
        if (!Minecraft.getInstance().isSameThread()) {
            throw new IllegalStateException("Butterfly particle effects must be changed on the client thread");
        }
    }

    private static final class Emitter {
        private final Identifier id;
        private final ParticleDefinition definition;
        private final Vec3 origin;
        private int remainingTicks;
        private double carry;

        private Emitter(Identifier id, ParticleDefinition definition, Vec3 origin) {
            this.id = id;
            this.definition = definition;
            this.origin = origin;
            this.remainingTicks = (int) Math.ceil(
                    definition.emitterLifetimeSeconds().sample(ThreadLocalRandom.current()) * 20.0D
            );
        }

        private Identifier id() {
            return id;
        }

        private ParticleDefinition definition() {
            return definition;
        }

        private Vec3 origin() {
            return origin;
        }

        private boolean tick(Minecraft client) {
            if (remainingTicks-- <= 0) {
                return false;
            }
            double requested = carry + definition.emissionRate() / 20.0D;
            int toSpawn = Math.min(MAX_PARTICLES_PER_TICK, (int) Math.floor(requested));
            if (PARTICLES.size() < MAX_ACTIVE_PARTICLES) {
                toSpawn = Math.min(toSpawn, MAX_ACTIVE_PARTICLES - PARTICLES.size());
                RandomGenerator random = ThreadLocalRandom.current();
                for (int index = 0; index < toSpawn; index++) {
                    createParticle(client, this, random);
                }
            } else {
                toSpawn = 0;
            }
            carry = requested - Math.floor(requested);
            return true;
        }
    }

    private record TrackedParticle(
            DefinitionParticle particle,
            ParticleVector acceleration,
            ParticleColor startColor,
            ParticleColor endColor,
            float startSize,
            float endSize
    ) {
        private void tick() {
            float progress = Math.min(1.0F, (float) particle.age() / particle.getLifetime());
            particle.setParticleSpeed(
                    particle.xd() + acceleration.x() / 20.0D,
                    particle.yd() + acceleration.y() / 20.0D,
                    particle.zd() + acceleration.z() / 20.0D
            );
            particle.visualize(startColor.lerp(endColor, progress), startSize + (endSize - startSize) * progress);
        }
    }

    private static final class DefinitionParticle extends SingleQuadParticle {
        private float renderedSize;
//? if <1.21.11 {
/*
        private final TextureAtlasSprite sprite;
*///?}

        private DefinitionParticle(
                ClientLevel level,
                double x,
                double y,
                double z,
                double velocityX,
                double velocityY,
                double velocityZ,
                TextureAtlasSprite sprite,
                ParticleColor color,
                float size
        ) {
            super(level, x, y, z, velocityX, velocityY, velocityZ
//? if >=1.21.11 {
                    , sprite
//?}
            );
//? if <1.21.11 {
/*
            this.sprite = sprite;
*///?}
            hasPhysics = false;
            friction = 1.0F;
            renderedSize = 1.0F;
            visualize(color, size);
        }

//? if <1.21.11 {
/*
        @Override
        public net.minecraft.client.particle.ParticleRenderType getRenderType() {
            return net.minecraft.client.particle.ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        @Override
        protected float getU0() {
            return sprite.getU0();
        }

        @Override
        protected float getU1() {
            return sprite.getU1();
        }

        @Override
        protected float getV0() {
            return sprite.getV0();
        }

        @Override
        protected float getV1() {
            return sprite.getV1();
        }
*///?} else {
        @Override
        protected Layer getLayer() {
            return Layer.TRANSLUCENT;
        }
//?}

        private int age() {
            return age;
        }

        private double xd() {
            return xd;
        }

        private double yd() {
            return yd;
        }

        private double zd() {
            return zd;
        }

        private void visualize(ParticleColor color, float size) {
            setColor(color.red(), color.green(), color.blue());
            setAlpha(color.alpha());
            float clampedSize = Math.max(0.01F, size);
            scale(clampedSize / renderedSize);
            renderedSize = clampedSize;
        }
    }
}
