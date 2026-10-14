package moth.butterflyapi.plush;

import moth.butterflyapi.mod.ModContext;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.Objects;
import java.util.function.Consumer;

public final class PlushDefinition {
    private final Identifier id;
    private final Identifier soundId;
    private final BlockBehaviour.Properties blockSettings;
    private final Item.Properties itemSettings;
    private final Consumer<PlushBlockEntity> useAction;
    private final float soundVolume;
    private final float minimumSoundPitch;
    private final float maximumSoundPitch;
    private final int soundCooldownTicks;

    private PlushDefinition(
            Identifier id,
            Identifier soundId,
            BlockBehaviour.Properties blockSettings,
            Item.Properties itemSettings,
            Consumer<PlushBlockEntity> useAction,
            float soundVolume,
            float minimumSoundPitch,
            float maximumSoundPitch,
            int soundCooldownTicks
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.soundId = Objects.requireNonNull(soundId, "soundId");
        this.blockSettings = Objects.requireNonNull(blockSettings, "blockSettings");
        this.itemSettings = Objects.requireNonNull(itemSettings, "itemSettings");
        this.useAction = Objects.requireNonNull(useAction, "useAction");
        this.soundVolume = validateVolume(soundVolume);
        this.minimumSoundPitch = validatePitch(minimumSoundPitch, "minimumSoundPitch");
        this.maximumSoundPitch = validatePitch(maximumSoundPitch, "maximumSoundPitch");
        if (this.minimumSoundPitch > this.maximumSoundPitch) {
            throw new IllegalArgumentException("minimumSoundPitch cannot exceed maximumSoundPitch");
        }
        if (soundCooldownTicks < 0) {
            throw new IllegalArgumentException("soundCooldownTicks cannot be negative");
        }
        this.soundCooldownTicks = soundCooldownTicks;
    }

    public static Builder builder(Identifier id) {
        return new Builder(id);
    }

    public static Builder builder(ModContext context, String path) {
        Objects.requireNonNull(context, "context");
        return builder(context.id(path));
    }

    public Identifier id() {
        return id;
    }

    public Identifier soundId() {
        return soundId;
    }

    public BlockBehaviour.Properties blockSettings() {
        return blockSettings;
    }

    public Item.Properties itemSettings() {
        return itemSettings;
    }

    public void onUse(PlushBlockEntity plushBlockEntity) {
        useAction.accept(plushBlockEntity);
    }

    public float soundVolume() {
        return soundVolume;
    }

    public float minimumSoundPitch() {
        return minimumSoundPitch;
    }

    public float maximumSoundPitch() {
        return maximumSoundPitch;
    }

    public int soundCooldownTicks() {
        return soundCooldownTicks;
    }

    public static final class Builder {
        private final Identifier id;
        private Identifier soundId;
        private BlockBehaviour.Properties blockSettings = defaultBlockSettings();
        private Item.Properties itemSettings = new Item.Properties();
        private Consumer<PlushBlockEntity> useAction = plush -> {};
        private float soundVolume = 1.0F;
        private float minimumSoundPitch = 1.0F;
        private float maximumSoundPitch = 1.0F;
        private int soundCooldownTicks;

        private Builder(Identifier id) {
            this.id = Objects.requireNonNull(id, "id");
            this.soundId = Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_honk");
        }

        public Builder sound(String path) {
            return sound(Identifier.fromNamespaceAndPath(id.getNamespace(), path));
        }

        public Builder sound(Identifier soundId) {
            this.soundId = Objects.requireNonNull(soundId, "soundId");
            return this;
        }

        public Builder blockSettings(BlockBehaviour.Properties blockSettings) {
            this.blockSettings = Objects.requireNonNull(blockSettings, "blockSettings");
            return this;
        }

        public Builder itemSettings(Item.Properties itemSettings) {
            this.itemSettings = Objects.requireNonNull(itemSettings, "itemSettings");
            return this;
        }

        public Builder onUse(Consumer<PlushBlockEntity> useAction) {
            this.useAction = Objects.requireNonNull(useAction, "useAction");
            return this;
        }

        public Builder soundVolume(float soundVolume) {
            this.soundVolume = validateVolume(soundVolume);
            return this;
        }

        public Builder soundPitch(float soundPitch) {
            return soundPitch(soundPitch, soundPitch);
        }

        public Builder soundPitch(float minimumSoundPitch, float maximumSoundPitch) {
            float checkedMinimum = validatePitch(minimumSoundPitch, "minimumSoundPitch");
            float checkedMaximum = validatePitch(maximumSoundPitch, "maximumSoundPitch");
            if (checkedMinimum > checkedMaximum) {
                throw new IllegalArgumentException("minimumSoundPitch cannot exceed maximumSoundPitch");
            }
            this.minimumSoundPitch = checkedMinimum;
            this.maximumSoundPitch = checkedMaximum;
            return this;
        }

        public Builder soundCooldownTicks(int soundCooldownTicks) {
            if (soundCooldownTicks < 0) {
                throw new IllegalArgumentException("soundCooldownTicks cannot be negative");
            }
            this.soundCooldownTicks = soundCooldownTicks;
            return this;
        }

        public PlushDefinition build() {
            return new PlushDefinition(
                    id,
                    soundId,
                    blockSettings,
                    itemSettings,
                    useAction,
                    soundVolume,
                    minimumSoundPitch,
                    maximumSoundPitch,
                    soundCooldownTicks
            );
        }
    }

    private static float validateVolume(float volume) {
        if (!Float.isFinite(volume) || volume < 0.0F || volume > 4.0F) {
            throw new IllegalArgumentException("soundVolume must be between 0.0 and 4.0");
        }
        return volume;
    }

    private static float validatePitch(float pitch, String name) {
        if (!Float.isFinite(pitch) || pitch < 0.5F || pitch > 2.0F) {
            throw new IllegalArgumentException(name + " must be between 0.5 and 2.0");
        }
        return pitch;
    }

    private static BlockBehaviour.Properties defaultBlockSettings() {
        return BlockBehaviour.Properties.of()
                .strength(0.2F)
                .sound(SoundType.WOOL)
                .noOcclusion()
                .dynamicShape();
    }
}
