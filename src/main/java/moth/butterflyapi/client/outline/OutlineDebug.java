package moth.butterflyapi.client.outline;

import moth.butterflyapi.outline.OutlinePlacement;
import moth.butterflyapi.outline.OutlineStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.SheepEntity;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class OutlineDebug {
    private static final OutlineStyle SHEEP_STYLE = OutlineStyle.builder(0xB026FF)
            .thickness(20)
            .placement(OutlinePlacement.OUTSIDE)
            .blackBorder(true)
            .visibleThroughWalls(true)
            .matchModelLayer(false)
            .build();
    private static final OutlineStyle COW_STYLE = OutlineStyle.builder(0xFFFF00)
            .thickness(1)
            .placement(OutlinePlacement.INSIDE)
            .blackBorder(false)
            .visibleThroughWalls(false)
            .matchModelLayer(true)
            .build();
    private static final OutlineStyle CHICKEN_STYLE = OutlineStyle.builder(0xFFFFFF)
            .thickness(1)
            .placement(OutlinePlacement.CENTERED)
            .blackBorder(false)
            .visibleThroughWalls(false)
            .matchModelLayer(true)
            .build();
    private static boolean initialized;

    private OutlineDebug() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> apply(entity));
        ClientTickEvents.END_CLIENT_TICK.register(OutlineDebug::applyToLoadedEntities);
    }

    private static void applyToLoadedEntities(MinecraftClient client) {
        if (client.world == null) {
            return;
        }
        for (Entity entity : client.world.getEntities()) {
            apply(entity);
        }
    }

    private static void apply(Entity entity) {
        OutlineStyle style = styleFor(entity);
        if (style == null || ClientOutlines.getLocal(entity).filter(style::equals).isPresent()) {
            return;
        }
        ClientOutlines.set(entity, style);
    }

    @Nullable
    public static OutlineStyle styleFor(Entity entity) {
        if (entity instanceof SheepEntity) {
            return SHEEP_STYLE;
        }
        if (entity instanceof CowEntity) {
            return COW_STYLE;
        }
        if (entity instanceof ChickenEntity) {
            return CHICKEN_STYLE;
        }
        return null;
    }
}
