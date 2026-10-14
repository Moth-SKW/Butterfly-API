package moth.butterflyapi.plush;

import moth.butterflyapi.ButterflyApi;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
            bootstrapped = true;
            return;
        }

        for (RegisteredPlush plush : PLUSHES.values()) {
            PlushDefinition definition = plush.definition();
            Identifier id = definition.id();
            SoundEvent sound = Registry.register(BuiltInRegistries.SOUND_EVENT, definition.soundId(), SoundEvent.createVariableRangeEvent(definition.soundId()));
//? if >=1.21.11 {
            definition.blockSettings().setId(ResourceKey.create(Registries.BLOCK, id));
//?}
            PlushBlock block = Registry.register(BuiltInRegistries.BLOCK, id, new PlushBlock(definition, plush::soundOrNull));
//? if >=1.21.11 {
            definition.itemSettings().setId(ResourceKey.create(Registries.ITEM, id));
//?}
            Item item = Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, definition.itemSettings()));
            plush.bind(sound, block, item);
        }

        blockEntityType = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
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
