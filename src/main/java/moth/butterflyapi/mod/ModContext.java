package moth.butterflyapi.mod;

import moth.butterflyapi.client.ClientRegistrar;
import moth.butterflyapi.block.BlockBuilder;
import moth.butterflyapi.block.Blocks;
import moth.butterflyapi.item.ItemBuilder;
import moth.butterflyapi.item.Items;
import moth.butterflyapi.itemgroup.TabBuilder;
import moth.butterflyapi.itemgroup.Tabs;
import moth.butterflyapi.plush.PlushDefinition;
import moth.butterflyapi.registry.BlockItemFactory;
import moth.butterflyapi.registry.Registrar;
import moth.butterflyapi.text.TextFonts;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
//? if >=1.21.11 {
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
//?}
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModContext {
    private final String modId;
    private final String modName;
    private final Logger logger;
    private final Registrar registrar;
    private final Tabs tabs;
    private final Items items;
    private final Blocks blocks;
    @Nullable
    private ClientRegistrar clientRegistrar;

    private ModContext(String modId, String modName, Logger logger) {
        this.modId = validateModId(modId);
        this.modName = Objects.requireNonNull(modName, "modName");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.registrar = new Registrar(this);
        this.tabs = new Tabs(this);
        this.items = new Items(this);
        this.blocks = new Blocks(this);
    }

    public static ModContext of(String modId) {
        return new ModContext(modId, modId, LoggerFactory.getLogger(modId));
    }

    public static ModContext of(String modId, String modName) {
        return new ModContext(modId, modName, LoggerFactory.getLogger(modName));
    }

    private static String validateModId(String modId) {
        Objects.requireNonNull(modId, "modId");
        Identifier.fromNamespaceAndPath(modId, "bootstrap_check");
        return modId;
    }

    public String modId() {
        return modId;
    }

    public String modName() {
        return modName;
    }

    public Logger logger() {
        return logger;
    }

    public Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(modId, path);
    }

    public ResourceKey<Block> blockKey(String path) {
        return ResourceKey.create(Registries.BLOCK, id(path));
    }

    public BlockBehaviour.Properties blockSettings(String path) {
//? if <1.21.11 {
/*
        return BlockBehaviour.Properties.of();
*///?} else {
        return BlockBehaviour.Properties.of().setId(blockKey(path));
//?}
    }

    public ResourceKey<Item> itemKey(String path) {
        return ResourceKey.create(Registries.ITEM, id(path));
    }

    public Item.Properties itemSettings(String path) {
//? if <1.21.11 {
/*
        return new Item.Properties();
*///?} else {
        return new Item.Properties().setId(itemKey(path));
//?}
    }

    public PlushDefinition.Builder plush(String path) {
        return PlushDefinition.builder(this, path);
    }

    public Identifier font(String path) {
        return TextFonts.register(id(path));
    }

    public Registrar registrar() {
        return registrar;
    }

    public Tabs tabs() {
        return tabs;
    }

    public Items items() {
        return items;
    }

    public Blocks blocks() {
        return blocks;
    }

    public <T extends Item> ItemBuilder<T> itemBuilder(String path, Function<Item.Properties, T> factory) {
        return items.builder(path, factory);
    }

    public ClientRegistrar client() {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            throw new IllegalStateException("Client registration helpers are only available on the physical client");
        }
        if (clientRegistrar == null) {
            clientRegistrar = new ClientRegistrar(this);
        }
        return clientRegistrar;
    }

    public <T> T register(Registry<? super T> registry, String path, T value) {
        return registrar.register(registry, path, value);
    }

    public <T extends Item> T item(String path, T item) {
        return registrar.item(path, item);
    }

    public <T extends Item> T item(String path, Function<Item.Properties, T> factory) {
        Objects.requireNonNull(factory, "factory");
        return item(path, factory.apply(itemSettings(path)));
    }

    public <T extends Block> T block(String path, T block) {
        return registrar.block(path, block);
    }

    public <T extends Block> T block(String path, Function<BlockBehaviour.Properties, T> factory) {
        Objects.requireNonNull(factory, "factory");
        return block(path, factory.apply(blockSettings(path)));
    }

    public <T extends Block> BlockBuilder<T> blockBuilder(String path, Function<BlockBehaviour.Properties, T> factory) {
        return new BlockBuilder<>(this, path, factory);
    }

    public <T extends Block> T block(String path, T block, Item.Properties itemSettings) {
        return registrar.block(path, block, itemSettings);
    }

    public <T extends Block> T block(String path, T block, BlockItemFactory<T> itemFactory) {
        return registrar.block(path, block, itemFactory);
    }

    public <T extends Block> T blockOnly(String path, T block) {
        return registrar.blockOnly(path, block);
    }

    public <T extends Block> T door(String path, T block) {
        return registrar.door(path, block);
    }

    public SoundEvent sound(String path) {
        return registrar.sound(path);
    }

    public SoundEvent sound(String path, SoundEvent soundEvent) {
        return registrar.sound(path, soundEvent);
    }

    public Enchantment enchantment(String path, Enchantment enchantment) {
        return registrar.enchantment(path, enchantment);
    }

    public <T extends EntityType<?>> T entity(String path, T entityType) {
        return registrar.entity(path, entityType);
    }

    public <T extends BlockEntityType<?>> T blockEntityType(String path, T blockEntityType) {
        return registrar.blockEntityType(path, blockEntityType);
    }

    public <T extends MenuType<?>> T screenHandler(String path, T screenHandlerType) {
        return registrar.screenHandler(path, screenHandlerType);
    }

    public <T extends MobEffect> T statusEffect(String path, T statusEffect) {
        return registrar.statusEffect(path, statusEffect);
    }

    public <T extends Potion> T potion(String path, T potion) {
        return registrar.potion(path, potion);
    }

    public <T extends RecipeSerializer<?>> T recipeSerializer(String path, T recipeSerializer) {
        return registrar.recipeSerializer(path, recipeSerializer);
    }

    public <T extends RecipeType<?>> T recipeType(String path, T recipeType) {
        return registrar.recipeType(path, recipeType);
    }

    public <T extends ParticleType<?>> T particle(String path, T particleType) {
        return registrar.particle(path, particleType);
    }

    public PaintingVariant painting(String path, PaintingVariant paintingVariant) {
        return registrar.painting(path, paintingVariant);
    }

    public ResourceKey<CreativeModeTab> tabKey(String path) {
        return tabs.key(path);
    }

    public String tabTranslationKey(String path) {
        return tabs.translationKey(path);
    }

    public TabBuilder tabBuilder(String path) {
        return tabs.builder(path);
    }

    public CreativeModeTab tab(String path, ItemLike icon, ItemLike... entries) {
        return tabs.create(path, icon, entries);
    }

    public CreativeModeTab tab(String path, ItemLike icon, String translationKey, ItemLike... entries) {
        return tabs.create(path, icon, translationKey, entries);
    }

    public CreativeModeTab tab(String path, Supplier<ItemStack> icon, Component displayName, Consumer<CreativeModeTab.Output> entries) {
        return tabs.create(path, icon, displayName, entries);
    }

    public void addTo(ResourceKey<CreativeModeTab> group, ItemLike... entries) {
        tabs.addTo(group, entries);
    }

    public void addTo(ResourceKey<CreativeModeTab> group, Collection<? extends ItemLike> entries) {
        tabs.addTo(group, entries);
    }

    public void cutout(Block... blocks) {
        client().cutout(blocks);
    }

    public void cutoutMipped(Block... blocks) {
        client().cutoutMipped(blocks);
    }

    public void translucent(Block... blocks) {
        client().translucent(blocks);
    }

    public <T extends Entity> void entityRenderer(EntityType<? extends T> entityType, EntityRendererProvider<T> factory) {
        client().entity(entityType, factory);
    }

//? if >=1.21.11 {
    public <T extends BlockEntity, S extends BlockEntityRenderState> void blockEntityRenderer(
            BlockEntityType<? extends T> blockEntityType,
            BlockEntityRendererProvider<T, S> factory
    ) {
        client().blockEntity(blockEntityType, factory);
    }
//?} else {
/*
    public <T extends BlockEntity> void blockEntityRenderer(
            BlockEntityType<? extends T> blockEntityType,
            BlockEntityRendererProvider<T> factory
    ) {
        client().blockEntity(blockEntityType, factory);
    }
*///?}

    public <T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> void screen(MenuType<? extends T> screenHandlerType, MenuScreens.ScreenConstructor<T, U> provider) {
        client().screen(screenHandlerType, provider);
    }

    public void modelLayer(ModelLayerLocation layer, net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.TexturedModelDataProvider provider) {
        client().modelLayer(layer, provider);
    }

    public <T extends ParticleOptions> void particleFactory(ParticleType<T> particleType, net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.PendingParticleFactory<T> factory) {
        client().particle(particleType, factory);
    }
}
