package moth.butterflyapi.plush;

import moth.butterflyapi.mod.ModContext;
import net.minecraft.block.AbstractBlock;
import net.minecraft.item.Item;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class PlushDefinition {
    private final Identifier id;
    private final Identifier soundId;
    @Nullable
    private final GeckoRenderData geckolib;
    private final AbstractBlock.Settings blockSettings;
    private final Item.Settings itemSettings;
    private final BiConsumer<PlushBlockEntity, AnimatableManager.ControllerRegistrar> controllerRegistrar;
    private final Consumer<PlushBlockEntity> useAction;

    private PlushDefinition(
            Identifier id,
            Identifier soundId,
            @Nullable GeckoRenderData geckolib,
            AbstractBlock.Settings blockSettings,
            Item.Settings itemSettings,
            BiConsumer<PlushBlockEntity, AnimatableManager.ControllerRegistrar> controllerRegistrar,
            Consumer<PlushBlockEntity> useAction
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.soundId = Objects.requireNonNull(soundId, "soundId");
        this.geckolib = geckolib;
        this.blockSettings = Objects.requireNonNull(blockSettings, "blockSettings");
        this.itemSettings = Objects.requireNonNull(itemSettings, "itemSettings");
        this.controllerRegistrar = Objects.requireNonNull(controllerRegistrar, "controllerRegistrar");
        this.useAction = Objects.requireNonNull(useAction, "useAction");
    }

    public static Builder builder(Identifier id) {
        return new Builder(id);
    }

    public static Builder builder(ModContext context, String path) {
        Objects.requireNonNull(context, "context");
        return builder(context.id(path));
    }

    public static Builder geckoBuilder(Identifier id) {
        return builder(id).geckolib(GeckoRenderData.block(id));
    }

    public static Builder geckoBuilder(ModContext context, String path) {
        Objects.requireNonNull(context, "context");
        return geckoBuilder(context.id(path));
    }

    public Identifier id() {
        return id;
    }

    public Identifier soundId() {
        return soundId;
    }

    public boolean usesGeckolib() {
        return geckolib != null;
    }

    @Nullable
    public GeckoRenderData geckolib() {
        return geckolib;
    }

    public AbstractBlock.Settings blockSettings() {
        return blockSettings;
    }

    public Item.Settings itemSettings() {
        return itemSettings;
    }

    public void registerControllers(PlushBlockEntity plushBlockEntity, AnimatableManager.ControllerRegistrar controllers) {
        controllerRegistrar.accept(plushBlockEntity, controllers);
    }

    public void onUse(PlushBlockEntity plushBlockEntity) {
        useAction.accept(plushBlockEntity);
    }

    public static final class Builder {
        private final Identifier id;
        private Identifier soundId;
        @Nullable
        private GeckoRenderData geckolib;
        private AbstractBlock.Settings blockSettings = defaultBlockSettings();
        private Item.Settings itemSettings = new Item.Settings();
        private BiConsumer<PlushBlockEntity, AnimatableManager.ControllerRegistrar> controllerRegistrar = (plush, controllers) -> {};
        private Consumer<PlushBlockEntity> useAction = plush -> {};

        private Builder(Identifier id) {
            this.id = Objects.requireNonNull(id, "id");
            this.soundId = new Identifier(id.getNamespace(), id.getPath() + "_honk");
        }

        public Builder sound(String path) {
            return sound(new Identifier(id.getNamespace(), path));
        }

        public Builder sound(Identifier soundId) {
            this.soundId = Objects.requireNonNull(soundId, "soundId");
            return this;
        }

        public Builder geckolib(GeckoRenderData geckolib) {
            this.geckolib = Objects.requireNonNull(geckolib, "geckolib");
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

        public Builder controllers(BiConsumer<PlushBlockEntity, AnimatableManager.ControllerRegistrar> controllerRegistrar) {
            this.controllerRegistrar = Objects.requireNonNull(controllerRegistrar, "controllerRegistrar");
            return this;
        }

        public Builder addControllers(BiConsumer<PlushBlockEntity, AnimatableManager.ControllerRegistrar> controllerRegistrar) {
            Objects.requireNonNull(controllerRegistrar, "controllerRegistrar");
            BiConsumer<PlushBlockEntity, AnimatableManager.ControllerRegistrar> previous = this.controllerRegistrar;
            this.controllerRegistrar = (plush, controllers) -> {
                previous.accept(plush, controllers);
                controllerRegistrar.accept(plush, controllers);
            };
            return this;
        }

        public Builder loopingAnimation(String animationName) {
            return loopingAnimation(animationName, animationName);
        }

        public Builder loopingAnimation(String controllerName, String animationName) {
            Objects.requireNonNull(controllerName, "controllerName");
            Objects.requireNonNull(animationName, "animationName");
            RawAnimation animation = RawAnimation.begin().thenLoop(animationName);
            return addControllers((plush, controllers) ->
                    controllers.add(new AnimationController<>(plush, controllerName, 0, state -> state.setAndContinue(animation))));
        }

        public Builder onUse(Consumer<PlushBlockEntity> useAction) {
            this.useAction = Objects.requireNonNull(useAction, "useAction");
            return this;
        }

        public PlushDefinition build() {
            return new PlushDefinition(id, soundId, geckolib, blockSettings, itemSettings, controllerRegistrar, useAction);
        }
    }

    public record GeckoRenderData(
            Identifier model,
            Identifier texture,
            Identifier animation,
            float scale,
            float offsetX,
            float offsetY,
            float offsetZ
    ) {
        public GeckoRenderData {
            Objects.requireNonNull(model, "model");
            Objects.requireNonNull(texture, "texture");
            Objects.requireNonNull(animation, "animation");
        }

        public static GeckoRenderData block(Identifier id) {
            return new GeckoRenderData(
                    new Identifier(id.getNamespace(), "geo/block/" + id.getPath() + ".geo.json"),
                    new Identifier(id.getNamespace(), "textures/block/" + id.getPath() + ".png"),
                    new Identifier(id.getNamespace(), "animations/block/" + id.getPath() + ".animation.json"),
                    1.0f,
                    0.0f,
                    0.0f,
                    0.0f
            );
        }

        public static GeckoRenderData block(ModContext context, String path) {
            Objects.requireNonNull(context, "context");
            return block(context.id(path));
        }

        public GeckoRenderData withScale(float scale) {
            return new GeckoRenderData(model, texture, animation, scale, offsetX, offsetY, offsetZ);
        }

        public GeckoRenderData withOffset(float offsetX, float offsetY, float offsetZ) {
            return new GeckoRenderData(model, texture, animation, scale, offsetX, offsetY, offsetZ);
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
