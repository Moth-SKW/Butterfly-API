package moth.butterflyapi.plush;

import moth.butterflyapi.mod.ModContext;
import net.minecraft.block.AbstractBlock;
import net.minecraft.item.Item;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.function.Consumer;

public final class PlushDefinition {
    private final Identifier id;
    private final Identifier soundId;
    private final AbstractBlock.Settings blockSettings;
    private final Item.Settings itemSettings;
    private final Consumer<PlushBlockEntity> useAction;

    private PlushDefinition(
            Identifier id,
            Identifier soundId,
            AbstractBlock.Settings blockSettings,
            Item.Settings itemSettings,
            Consumer<PlushBlockEntity> useAction
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.soundId = Objects.requireNonNull(soundId, "soundId");
        this.blockSettings = Objects.requireNonNull(blockSettings, "blockSettings");
        this.itemSettings = Objects.requireNonNull(itemSettings, "itemSettings");
        this.useAction = Objects.requireNonNull(useAction, "useAction");
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

    public AbstractBlock.Settings blockSettings() {
        return blockSettings;
    }

    public Item.Settings itemSettings() {
        return itemSettings;
    }

    public void onUse(PlushBlockEntity plushBlockEntity) {
        useAction.accept(plushBlockEntity);
    }

    public static final class Builder {
        private final Identifier id;
        private Identifier soundId;
        private AbstractBlock.Settings blockSettings = defaultBlockSettings();
        private Item.Settings itemSettings = new Item.Settings();
        private Consumer<PlushBlockEntity> useAction = plush -> {};

        private Builder(Identifier id) {
            this.id = Objects.requireNonNull(id, "id");
            this.soundId = Identifier.of(id.getNamespace(), id.getPath() + "_honk");
        }

        public Builder sound(String path) {
            return sound(Identifier.of(id.getNamespace(), path));
        }

        public Builder sound(Identifier soundId) {
            this.soundId = Objects.requireNonNull(soundId, "soundId");
            return this;
        }

        public Builder blockSettings(AbstractBlock.Settings blockSettings) {
            this.blockSettings = Objects.requireNonNull(blockSettings, "blockSettings");
            return this;
        }

        public Builder itemSettings(Item.Settings itemSettings) {
            this.itemSettings = Objects.requireNonNull(itemSettings, "itemSettings");
            return this;
        }

        public Builder onUse(Consumer<PlushBlockEntity> useAction) {
            this.useAction = Objects.requireNonNull(useAction, "useAction");
            return this;
        }

        public PlushDefinition build() {
            return new PlushDefinition(id, soundId, blockSettings, itemSettings, useAction);
        }
    }

    private static AbstractBlock.Settings defaultBlockSettings() {
        return AbstractBlock.Settings.create()
                .strength(0.2F)
                .sounds(BlockSoundGroup.WOOL)
                .nonOpaque()
                .dynamicBounds();
    }
}
