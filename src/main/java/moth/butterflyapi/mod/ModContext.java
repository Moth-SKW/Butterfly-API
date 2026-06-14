package moth.butterflyapi.mod;

import moth.butterflyapi.client.ClientRegistrar;
import moth.butterflyapi.itemgroup.TabBuilder;
import moth.butterflyapi.itemgroup.Tabs;
import moth.butterflyapi.plush.PlushDefinition;
import moth.butterflyapi.registry.BlockItemFactory;
import moth.butterflyapi.registry.Registrar;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ModContext {
    private final String modId;
    private final String modName;
    private final Logger logger;
    private final Registrar registrar;
    private final Tabs tabs;
    @Nullable
    private ClientRegistrar clientRegistrar;

    private ModContext(String modId, String modName, Logger logger) {
        this.modId = validateModId(modId);
        this.modName = Objects.requireNonNull(modName, "modName");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.registrar = new Registrar(this);
        this.tabs = new Tabs(this);
    }

    public static ModContext of(String modId) {
        return new ModContext(modId, modId, LoggerFactory.getLogger(modId));
    }

    public static ModContext of(String modId, String modName) {
        return new ModContext(modId, modName, LoggerFactory.getLogger(modName));
    }

    private static String validateModId(String modId) {
        Objects.requireNonNull(modId, "modId");
        new Identifier(modId, "bootstrap_check");
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
        return new Identifier(modId, path);
    }

    public PlushDefinition.Builder plush(String path) {
        return PlushDefinition.builder(this, path);
    }

    public PlushDefinition.Builder geckoPlush(String path) {
        return PlushDefinition.geckoBuilder(this, path);
    }

    public Registrar registrar() {
        return registrar;
    }

    public Tabs tabs() {
        return tabs;
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

    public <T extends Block> T block(String path, T block) {
        return registrar.block(path, block);
    }

    public <T extends Block> T block(String path, T block, Item.Settings itemSettings) {
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

    public <T extends SoundEvent> T sound(String path, T soundEvent) {
        return registrar.sound(path, soundEvent);
    }

    public <T extends Enchantment> T enchantment(String path, T enchantment) {
        return registrar.enchantment(path, enchantment);
    }

    public <T extends EntityType<?>> T entity(String path, T entityType) {
        return registrar.entity(path, entityType);
    }

    public <T extends BlockEntityType<?>> T blockEntityType(String path, T blockEntityType) {
        return registrar.blockEntityType(path, blockEntityType);
    }

    public <T extends ScreenHandlerType<?>> T screenHandler(String path, T screenHandlerType) {
        return registrar.screenHandler(path, screenHandlerType);
    }

    public <T extends StatusEffect> T statusEffect(String path, T statusEffect) {
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

    public <T extends PaintingVariant> T painting(String path, T paintingVariant) {
        return registrar.painting(path, paintingVariant);
    }

    public RegistryKey<ItemGroup> tabKey(String path) {
        return tabs.key(path);
    }

    public String tabTranslationKey(String path) {
        return tabs.translationKey(path);
    }

    public TabBuilder tabBuilder(String path) {
        return tabs.builder(path);
    }

    public ItemGroup tab(String path, ItemConvertible icon, ItemConvertible... entries) {
        return tabs.create(path, icon, entries);
    }

    public ItemGroup tab(String path, ItemConvertible icon, String translationKey, ItemConvertible... entries) {
        return tabs.create(path, icon, translationKey, entries);
    }

    public ItemGroup tab(String path, Supplier<ItemStack> icon, Text displayName, Consumer<ItemGroup.Entries> entries) {
        return tabs.create(path, icon, displayName, entries);
    }

    public void addTo(RegistryKey<ItemGroup> group, ItemConvertible... entries) {
        tabs.addTo(group, entries);
    }

    public void addTo(RegistryKey<ItemGroup> group, Collection<? extends ItemConvertible> entries) {
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

    public <T extends Entity> void entityRenderer(EntityType<? extends T> entityType, EntityRendererFactory<T> factory) {
        client().entity(entityType, factory);
    }

    public <T extends BlockEntity> void blockEntityRenderer(BlockEntityType<? extends T> blockEntityType, BlockEntityRendererFactory<? super T> factory) {
        client().blockEntity(blockEntityType, factory);
    }

    public <T extends ScreenHandler, U extends Screen & ScreenHandlerProvider<T>> void screen(ScreenHandlerType<? extends T> screenHandlerType, HandledScreens.Provider<T, U> provider) {
        client().screen(screenHandlerType, provider);
    }

    public void predicate(ItemConvertible item, String path, ClampedModelPredicateProvider provider) {
        client().predicate(item, path, provider);
    }

    public void modelLayer(EntityModelLayer layer, net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.TexturedModelDataProvider provider) {
        client().modelLayer(layer, provider);
    }

    public <T extends ParticleEffect> void particleFactory(ParticleType<T> particleType, net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.PendingParticleFactory<T> factory) {
        client().particle(particleType, factory);
    }
}
