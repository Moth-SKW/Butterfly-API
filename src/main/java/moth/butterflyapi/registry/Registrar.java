package moth.butterflyapi.registry;

import moth.butterflyapi.block.BlockBuilder;
import moth.butterflyapi.mod.ModContext;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.resources.Identifier;
import java.util.Objects;
import java.util.function.Function;

public final class Registrar {
    private final ModContext context;

    public Registrar(ModContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public <T> T register(Registry<? super T> registry, String path, T value) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(value, "value");
        Identifier id = context.id(path);
        if (registry.containsKey(id)) {
            throw new IllegalStateException("Duplicate registration for " + id + " in " + registry.key().
//? if <1.21.11 {
/*
                    location()
*///?} else {
                    identifier()
//?}
            );
        }
        return Registry.register(registry, id, value);
    }

    public <T extends Item> T item(String path, T item) {
        return register(BuiltInRegistries.ITEM, path, item);
    }

    public <T extends Block> T block(String path, T block) {
        return block(path, block, registeredBlock -> new BlockItem(registeredBlock, context.itemSettings(path)));
    }

    public <T extends Block> BlockBuilder<T> blockBuilder(String path, Function<BlockBehaviour.Properties, T> factory) {
        return new BlockBuilder<>(context, path, factory);
    }

    public <T extends Block> T block(String path, T block, Item.Properties itemSettings) {
        Objects.requireNonNull(itemSettings, "itemSettings");
//? if >=1.21.11 {
        itemSettings.setId(context.itemKey(path));
//?}
        return block(path, block, registeredBlock -> new BlockItem(registeredBlock, itemSettings));
    }

    public <T extends Block> T block(String path, T block, BlockItemFactory<T> itemFactory) {
        Objects.requireNonNull(itemFactory, "itemFactory");
        T registered = blockOnly(path, block);
        item(path, itemFactory.create(registered));
        return registered;
    }

    public <T extends Block> T blockOnly(String path, T block) {
        return register(BuiltInRegistries.BLOCK, path, block);
    }

    public <T extends Block> T door(String path, T block) {
        return block(path, block, registeredBlock -> new DoubleHighBlockItem(registeredBlock, context.itemSettings(path)));
    }

    public SoundEvent sound(String path) {
        return sound(path, SoundEvent.createVariableRangeEvent(context.id(path)));
    }

    public SoundEvent sound(String path, SoundEvent soundEvent) {
        return register(BuiltInRegistries.SOUND_EVENT, path, soundEvent);
    }

    public Enchantment enchantment(String path, Enchantment enchantment) {
        Objects.requireNonNull(enchantment, "enchantment");
        throw new IllegalStateException(
                "Enchantment " + context.id(path) + " must be declared through its dynamic-registry data file; "
                        + "runtime enchantment registration is not supported by Minecraft"
        );
    }

    public <T extends EntityType<?>> T entity(String path, T entityType) {
        return register(BuiltInRegistries.ENTITY_TYPE, path, entityType);
    }

    public <T extends BlockEntityType<?>> T blockEntityType(String path, T blockEntityType) {
        return register(BuiltInRegistries.BLOCK_ENTITY_TYPE, path, blockEntityType);
    }

    public <T extends MenuType<?>> T screenHandler(String path, T screenHandlerType) {
        return register(BuiltInRegistries.MENU, path, screenHandlerType);
    }

    public <T extends MobEffect> T statusEffect(String path, T statusEffect) {
        return register(BuiltInRegistries.MOB_EFFECT, path, statusEffect);
    }

    public <T extends Potion> T potion(String path, T potion) {
        return register(BuiltInRegistries.POTION, path, potion);
    }

    public <T extends RecipeSerializer<?>> T recipeSerializer(String path, T serializer) {
        return register(BuiltInRegistries.RECIPE_SERIALIZER, path, serializer);
    }

    public <T extends RecipeType<?>> T recipeType(String path, T type) {
        return register(BuiltInRegistries.RECIPE_TYPE, path, type);
    }

    public <T extends ParticleType<?>> T particle(String path, T particleType) {
        return register(BuiltInRegistries.PARTICLE_TYPE, path, particleType);
    }

    public PaintingVariant painting(String path, PaintingVariant paintingVariant) {
        Objects.requireNonNull(paintingVariant, "paintingVariant");
        throw new IllegalStateException(
                "Painting variant " + context.id(path) + " must be declared through its dynamic-registry data file; "
                        + "runtime painting registration is not supported by Minecraft"
        );
    }

    public <T extends CreativeModeTab> T itemGroup(String path, T itemGroup) {
        return register(BuiltInRegistries.CREATIVE_MODE_TAB, path, itemGroup);
    }
}
