package moth.butterflyapi.registry;

import moth.butterflyapi.mod.ModContext;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.TallBlockItem;
import net.minecraft.particle.ParticleType;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.block.entity.BlockEntityType;

import java.util.Objects;

public final class Registrar {
    private final ModContext context;

    public Registrar(ModContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public <T> T register(Registry<? super T> registry, String path, T value) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(value, "value");
        return Registry.register(registry, context.id(path), value);
    }

    public <T extends Item> T item(String path, T item) {
        return register(Registries.ITEM, path, item);
    }

    public <T extends Block> T block(String path, T block) {
        return block(path, block, registeredBlock -> new BlockItem(registeredBlock, context.itemSettings(path)));
    }

    public <T extends Block> T block(String path, T block, Item.Settings itemSettings) {
        Objects.requireNonNull(itemSettings, "itemSettings");
        itemSettings.registryKey(context.itemKey(path));
        return block(path, block, registeredBlock -> new BlockItem(registeredBlock, itemSettings));
    }

    public <T extends Block> T block(String path, T block, BlockItemFactory<T> itemFactory) {
        Objects.requireNonNull(itemFactory, "itemFactory");
        T registered = blockOnly(path, block);
        item(path, itemFactory.create(registered));
        return registered;
    }

    public <T extends Block> T blockOnly(String path, T block) {
        return register(Registries.BLOCK, path, block);
    }

    public <T extends Block> T door(String path, T block) {
        return block(path, block, registeredBlock -> new TallBlockItem(registeredBlock, context.itemSettings(path)));
    }

    public SoundEvent sound(String path) {
        return sound(path, SoundEvent.of(context.id(path)));
    }

    public SoundEvent sound(String path, SoundEvent soundEvent) {
        return register(Registries.SOUND_EVENT, path, soundEvent);
    }

    public Enchantment enchantment(String path, Enchantment enchantment) {
        // Enchantments are loaded into the dynamic registry from data files in 1.21.11.
        return Objects.requireNonNull(enchantment, "enchantment");
    }

    public <T extends EntityType<?>> T entity(String path, T entityType) {
        return register(Registries.ENTITY_TYPE, path, entityType);
    }

    public <T extends BlockEntityType<?>> T blockEntityType(String path, T blockEntityType) {
        return register(Registries.BLOCK_ENTITY_TYPE, path, blockEntityType);
    }

    public <T extends ScreenHandlerType<?>> T screenHandler(String path, T screenHandlerType) {
        return register(Registries.SCREEN_HANDLER, path, screenHandlerType);
    }

    public <T extends StatusEffect> T statusEffect(String path, T statusEffect) {
        return register(Registries.STATUS_EFFECT, path, statusEffect);
    }

    public <T extends Potion> T potion(String path, T potion) {
        return register(Registries.POTION, path, potion);
    }

    public <T extends RecipeSerializer<?>> T recipeSerializer(String path, T serializer) {
        return register(Registries.RECIPE_SERIALIZER, path, serializer);
    }

    public <T extends RecipeType<?>> T recipeType(String path, T type) {
        return register(Registries.RECIPE_TYPE, path, type);
    }

    public <T extends ParticleType<?>> T particle(String path, T particleType) {
        return register(Registries.PARTICLE_TYPE, path, particleType);
    }

    public PaintingVariant painting(String path, PaintingVariant paintingVariant) {
        // Painting variants are also data-driven dynamic registry entries.
        return Objects.requireNonNull(paintingVariant, "paintingVariant");
    }

    public <T extends ItemGroup> T itemGroup(String path, T itemGroup) {
        return register(Registries.ITEM_GROUP, path, itemGroup);
    }
}
