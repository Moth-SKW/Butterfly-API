package moth.butterflyapi.testing;

import com.mojang.brigadier.Command;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import moth.butterflyapi.ButterflyApi;
import moth.butterflyapi.block.BlockRegistration;
import moth.butterflyapi.item.bar.ItemProgressBars;
import moth.butterflyapi.mod.ModContext;
import moth.butterflyapi.outline.OutlineStyle;
import moth.butterflyapi.outline.ServerOutlines;
import moth.butterflyapi.plush.ButterflyPlushes;
import moth.butterflyapi.plush.RegisteredPlush;
import moth.butterflyapi.testing.network.IntegrationTestRunS2CPacket;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class IntegrationTestService {
    private static final ModContext CONTEXT = ModContext.of(ButterflyApi.MOD_ID, ButterflyApi.MOD_NAME);
    private static final Map<java.util.UUID, BlockPos> PLUSH_POSITIONS = new LinkedHashMap<>();

    private static boolean initialized;
    private static Item probeItem;
    private static BlockRegistration<IntegrationTestMultiblockBlock> multiblock;
    private static RegisteredPlush plush;

    private IntegrationTestService() {
    }

    public static boolean enabled() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static synchronized void initialize() {
        if (!enabled() || initialized) {
            return;
        }

        probeItem = CONTEXT.itemBuilder("integration_probe", IntegrationTestProbeItem::new)
                .stacksTo(1)
                .progress(stack -> (System.currentTimeMillis() % 5_000L) / 5_000.0D, bars -> bars
                        .colors(0xF72585, 0x4CC9F0, 0x7CFF6B)
                        .pixelSnap(false))
                .register();
        multiblock = CONTEXT.blockBuilder("integration_multiblock", IntegrationTestMultiblockBlock::new)
                .settings(settings -> settings.strength(1.0F))
                .register();
        plush = ButterflyPlushes.register(CONTEXT.plush("integration_plush")
                .sound("integration_plush")
                .soundVolume(0.8F)
                .soundPitch(0.85F, 1.15F)
                .soundCooldownTicks(4)
                .onUse(blockEntity -> ButterflyApi.LOGGER.info("Integration plush used at {}", blockEntity.getBlockPos()))
                .build());
        CONTEXT.tabBuilder("integration")
                .icon(probeItem)
                .displayName(Component.literal("Butterfly API Integration"))
                .category("visual", "Visual systems", category -> category.add(probeItem))
                .category("world", "World systems", category -> category.entries(entries -> {
                    entries.accept(multiblock.item());
                    entries.accept(plush.item());
                }))
                .categoryOrder("visual", "world")
                .build();
        CONTEXT.addTo(CreativeModeTabs.TOOLS_AND_UTILITIES, probeItem);

        PayloadTypeRegistry.playS2C().register(IntegrationTestRunS2CPacket.ID, IntegrationTestRunS2CPacket.CODEC);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                Commands.literal("butterflytest")
                        .requires(source -> source.getEntity() instanceof ServerPlayer player && player.isCreative())
                        .then(Commands.literal("run").executes(context -> run(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("clear").executes(context -> clear(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("status").executes(context -> status(context.getSource())))
        ));
        initialized = true;
        ButterflyApi.LOGGER.info("Butterfly API development integration harness is enabled");
    }

    private static int run(ServerPlayer player) {
        verifyRegistrations();
        give(player, new ItemStack(probeItem));
        give(player, new ItemStack(multiblock.item()));
        give(player, new ItemStack(plush.item()));

        ServerLevel level =
//? if <1.21.11 {
/*
                player.serverLevel()
*///?} else {
                player.level()
//?}
                ;
        BlockPos plushPos = player.blockPosition().offset(2, 0, 0);
        clearServerState(player, true);
        if (level.getBlockState(plushPos).canBeReplaced()) {
            level.setBlock(plushPos, plush.block().defaultBlockState(), Block.UPDATE_ALL);
            BlockEntity blockEntity = level.getBlockEntity(plushPos);
            if (blockEntity != null) {
                ServerOutlines.setFor(player, blockEntity, OutlineStyle.builder("#4CC9F0")
                        .thickness(3)
                        .blackBorder()
                        .build());
                PLUSH_POSITIONS.put(player.getUUID(), plushPos);
            }
        }
        ServerOutlines.set(player, OutlineStyle.builder("#F72585").thickness(3).blackBorder().build());
        ServerOutlines.setFor(player, player, OutlineStyle.builder("#7CFF6B").thickness(4).build());
        send(player, new IntegrationTestRunS2CPacket(player.blockPosition(), false));
        player.sendSystemMessage(Component.literal(
                "Butterfly integration test started: inspect particles, component outlines, the test tab, the probe tooltip, plush, and 2x1x2 multiblock item."
        ));
        return Command.SINGLE_SUCCESS;
    }

    private static int clear(ServerPlayer player) {
        clearServerState(player, true);
        player.sendSystemMessage(Component.literal("Butterfly integration effects cleared."));
        return Command.SINGLE_SUCCESS;
    }

    private static int status(net.minecraft.commands.CommandSourceStack source) {
        boolean ready = initialized
                && probeItem != null
                && multiblock != null
                && plush != null
                && ItemProgressBars.isRegistered(probeItem)
                && BuiltInRegistries.ITEM.getKey(probeItem).equals(CONTEXT.id("integration_probe"));
        source.sendSuccess(() -> Component.literal(ready
                ? "Butterfly integration harness is ready. Use /butterflytest run as a player."
                : "Butterfly integration harness is not ready."), false);
        return ready ? Command.SINGLE_SUCCESS : 0;
    }

    private static void clearServerState(ServerPlayer player, boolean removePlush) {
        ServerOutlines.clear(player);
        BlockPos plushPos = PLUSH_POSITIONS.remove(player.getUUID());
        if (plushPos != null) {
            BlockEntity blockEntity = player.level().getBlockEntity(plushPos);
            if (blockEntity != null) {
                ServerOutlines.clear(blockEntity);
            }
            if (removePlush && player.level().getBlockState(plushPos).is(plush.block())) {
                player.level().destroyBlock(plushPos, false);
            }
        }
        send(player, new IntegrationTestRunS2CPacket(player.blockPosition(), true));
    }

    private static void verifyRegistrations() {
        require(ItemProgressBars.isRegistered(probeItem), "Integration probe progress bar is not registered");
        require(BuiltInRegistries.ITEM.getKey(probeItem).equals(CONTEXT.id("integration_probe")), "Integration probe item is missing");
        require(BuiltInRegistries.BLOCK.getKey(multiblock.block()).equals(CONTEXT.id("integration_multiblock")), "Integration multiblock is missing");
        require(plush.item() != null && plush.block() != null && plush.sound() != null, "Integration plush did not finish registration");
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        Objects.requireNonNull(player, "player");
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false
//? if >=26.3 {
/*
                    , net.minecraft.util.Prediction.SERVER_ONLY
*///?}
            );
        }
    }

    private static void send(ServerPlayer player, IntegrationTestRunS2CPacket packet) {
        if (ServerPlayNetworking.canSend(player, IntegrationTestRunS2CPacket.ID)) {
            ServerPlayNetworking.send(player, packet);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
