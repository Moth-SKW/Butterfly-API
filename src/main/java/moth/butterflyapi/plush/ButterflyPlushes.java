package moth.butterflyapi.plush;

import moth.butterflyapi.ButterflyApi;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ButterflyPlushes {
    public static final String ENTRYPOINT_KEY = "butterfly_api:plush";

    private static final Map<Identifier, RegisteredPlush> PLUSHES = new LinkedHashMap<>();
    private static boolean bootstrapped;
    @Nullable
    private static BlockEntityType<PlushBlockEntity> blockEntityType;

    private ButterflyPlushes() {
    }

    public static RegisteredPlush register(PlushDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        if (bootstrapped) {
            throw new IllegalStateException("Cannot register plush " + definition.id() + " after Butterfly plush bootstrap");
        }

        @Nullable RegisteredPlush existing = PLUSHES.get(definition.id());
        if (existing != null) {
            throw new IllegalStateException("Duplicate plush registration for id " + definition.id());
        }

        RegisteredPlush plush = new RegisteredPlush(definition);
        PLUSHES.put(definition.id(), plush);
        return plush;
    }

    public static void bootstrap() {
        if (bootstrapped) {
            return;
        }

        PlushRegistrar registrar = ButterflyPlushes::register;
        for (PlushEntrypoint entrypoint : FabricLoader.getInstance().getEntrypoints(ENTRYPOINT_KEY, PlushEntrypoint.class)) {
            entrypoint.registerPlushes(registrar);
        }

        if (PLUSHES.isEmpty()) {
            throw new IllegalStateException("Butterfly plush bootstrap ran without any registered plushes");
        }

        for (RegisteredPlush plush : PLUSHES.values()) {
            PlushDefinition definition = plush.definition();
            Identifier id = definition.id();
            SoundEvent sound = Registry.register(Registries.SOUND_EVENT, definition.soundId(), SoundEvent.of(definition.soundId()));
            definition.blockSettings().registryKey(RegistryKey.of(RegistryKeys.BLOCK, id));
            PlushBlock block = Registry.register(Registries.BLOCK, id, new PlushBlock(definition, plush::soundOrNull));
            definition.itemSettings().registryKey(RegistryKey.of(RegistryKeys.ITEM, id));
            Item item = Registry.register(Registries.ITEM, id, new BlockItem(block, definition.itemSettings()));
            plush.bind(sound, block, item);
        }

        blockEntityType = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                ButterflyApi.id("plush"),
                FabricBlockEntityTypeBuilder.create(
                        PlushBlockEntity::new,
                        allBlocksArray()
                ).build()
        );

        bootstrapped = true;
    }

    public static RegisteredPlush get(Identifier id) {
        RegisteredPlush plush = PLUSHES.get(id);
        if (plush == null) {
            throw new IllegalStateException("Unknown plush id: " + id);
        }
        return plush;
    }

    public static Collection<RegisteredPlush> all() {
        return Collections.unmodifiableCollection(PLUSHES.values());
    }

    public static BlockEntityType<PlushBlockEntity> blockEntityType() {
        if (blockEntityType == null) {
            throw new IllegalStateException("Butterfly plush block entity type is not registered yet");
        }
        return blockEntityType;
    }

    private static Block[] allBlocksArray() {
        return PLUSHES.values().stream()
                .map(plush -> Objects.requireNonNull(plush.block(), "block"))
                .toArray(Block[]::new);
    }
}
