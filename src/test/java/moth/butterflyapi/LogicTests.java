package moth.butterflyapi;

import java.io.StringReader;
import java.util.concurrent.ThreadLocalRandom;
import moth.butterflyapi.block.multiblock.MultiblockShape;
import moth.butterflyapi.client.outline.internal.BlockOutlineGeometryGroupsTestSupport;
import moth.butterflyapi.client.outline.internal.ClientItemOutlineManager;
import moth.butterflyapi.item.ItemProgressBarDefinition;
import moth.butterflyapi.item.bar.ItemProgressBar;
import moth.butterflyapi.outline.ItemOutlineTarget;
import moth.butterflyapi.outline.OutlinePlacement;
import moth.butterflyapi.outline.EntityOutline;
import moth.butterflyapi.outline.EntityOutlinePart;
import moth.butterflyapi.outline.OutlineStyle;
import moth.butterflyapi.particle.ParticleColor;
import moth.butterflyapi.particle.ParticleDefinition;
import moth.butterflyapi.particle.ParticleDefinitionException;
import moth.butterflyapi.particle.ParticleDefinitionLoader;
import moth.butterflyapi.particle.ParticleEmitterShape;
import moth.butterflyapi.particle.ParticleVector;
import moth.butterflyapi.text.Texts;
import moth.butterflyapi.text.TextGradient;
import moth.butterflyapi.text.TextRenderBounds;
import moth.butterflyapi.text.TextRenderOffset;
import moth.butterflyapi.text.TextRenderStyle;
import moth.butterflyapi.text.TextShakingOptions;
import moth.butterflyapi.text.TextShadow;
import moth.butterflyapi.text.TextTransform;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;

public final class LogicTests {
    private LogicTests() {
    }

    public static void main(String[] args) {
        parsesParticleDefinition();
        rejectsInvalidParticleDefinition();
        samplesParticleShapesAtBoundaries();
        interpolatesParticleColors();
        validatesExistingPublicValueObjects();
        rejectsInvalidTextRenderValues();
        composesEntityOutlineParts();
        validatesOutlineRenderingContracts();
        reusesConfigurationDefinitions();
        System.out.println("Butterfly API logic tests passed");
    }

    private static void parsesParticleDefinition() {
        String json = """
                {
                  "format_version": 1,
                  "emitter": {
                    "lifetime": [1.0, 2.0],
                    "emission_rate": 24,
                    "shape": {"type": "sphere", "radius": 0.75}
                  },
                  "particle": {
                    "lifetime": [0.25, 0.75],
                    "position": [0, 0.25, 0],
                    "velocity": [0, 0.1, 0],
                    "acceleration": [0, -0.05, 0],
                    "color": {"start": "#FF000080", "end": "#0000FFFF"},
                    "size": {"start": [0.25, 0.5], "end": 0.1},
                    "texture": "minecraft:generic_0"
                  }
                }
                """;
        ParticleDefinition definition = ParticleDefinitionLoader.parse(
                Identifier.fromNamespaceAndPath("test", "spark"),
                new StringReader(json)
        );
        require(definition.emissionShape().type() == ParticleEmitterShape.Type.SPHERE, "expected a sphere emitter");
        require(definition.emissionShape().extent().x() == 0.75D, "expected sphere radius");
        require(definition.startColor().alpha() == 128.0F / 255.0F, "expected start alpha");
        require(definition.endColor().blue() == 1.0F, "expected end color");
        require(definition.texture().isPresent(), "expected texture reference");
    }

    private static void rejectsInvalidParticleDefinition() {
        String unsupported = """
                {
                  "format_version": 1,
                  "emitter": {"lifetime": 1, "emission_rate": 1, "unknown": true},
                  "particle": {"lifetime": 1, "color": "#FFFFFF", "size": 1}
                }
                """;
        expectParticleFailure(unsupported);

        String fractionalFormat = """
                {
                  "format_version": 1.5,
                  "emitter": {"lifetime": 1, "emission_rate": 1},
                  "particle": {"lifetime": 1, "color": "#FFFFFF", "size": 1}
                }
                """;
        expectParticleFailure(fractionalFormat);
    }

    private static void samplesParticleShapesAtBoundaries() {
        ParticleVector point = ParticleEmitterShape.point().sample(ThreadLocalRandom.current());
        require(point.equals(ParticleVector.ZERO), "point emitters must sample their origin");
        ParticleVector flatBox = ParticleEmitterShape.box(ParticleVector.ZERO).sample(ThreadLocalRandom.current());
        require(flatBox.equals(ParticleVector.ZERO), "zero-sized boxes must sample their origin");
        ParticleVector sphere = ParticleEmitterShape.sphere(2.0D).sample(ThreadLocalRandom.current());
        require(sphere.lengthSquared() <= 4.0D, "sphere samples must remain within the radius");
    }

    private static void interpolatesParticleColors() {
        ParticleColor start = new ParticleColor(0.0F, 0.0F, 0.0F, 0.0F);
        ParticleColor end = new ParticleColor(1.0F, 0.5F, 0.25F, 1.0F);
        ParticleColor midpoint = start.lerp(end, 0.5F);
        require(midpoint.red() == 0.5F, "expected interpolated red channel");
        require(midpoint.alpha() == 0.5F, "expected interpolated alpha channel");
    }

    private static void validatesExistingPublicValueObjects() {
        ItemProgressBar bar = ItemProgressBar.of(Double.NaN, 0xFFAA00);
        require(bar.progress() == 0.0D, "invalid progress must clamp to zero");
        require(bar.color(-1) == 0xFFAA00, "negative layer indices must wrap");
        OutlineStyle style = OutlineStyle.builder().color("#FFFFFF").placement(OutlinePlacement.OUTSIDE).build();
        require(style.rgb() == 0xFFFFFF, "outline color parsing must retain white");
    }

    private static void reusesConfigurationDefinitions() {
        ItemProgressBarDefinition definition = ItemProgressBarDefinition.builder(stack -> 1.5D)
                .visibleWhen(stack -> true)
                .pixelSnap(false)
                .colors(0x112233, 0x445566)
                .build();
        require(!definition.pixelSnap(), "progress definitions should retain rendering options");
        require(definition.colors()[1] == 0x445566, "layered definition colors should be retained");
        require(definition.bar(1.5D).progress() == 1.5D, "progress definitions should create reusable bars");

        MultiblockShape shape = new MultiblockShape(2, 2, 1);
        require(shape.partsList().size() == 4, "multiblock parts should be precomputed once per shape");
        require(shape.partsList() == shape.partsList(), "multiblock part lists should be cached");
        require(Texts.of("Butterfly").getString().equals("Butterfly"), "compact text creation should preserve content");
    }

    private static void rejectsInvalidTextRenderValues() {
        require(TextRenderStyle.clamp01(Float.NaN) == 0.0F, "NaN opacity must resolve safely");
        require(TextRenderStyle.clamp01(Float.POSITIVE_INFINITY) == 1.0F, "infinite opacity must clamp high");
        require(TextRenderStyle.clamp01(Float.NEGATIVE_INFINITY) == 0.0F, "infinite opacity must clamp low");
        expectIllegal(() -> new TextShakingOptions(Float.NaN, null, 1.0F, 0L));
        expectIllegal(() -> new TextShakingOptions(1.0F, null, Float.POSITIVE_INFINITY, 0L));
        expectIllegal(() -> TextGradient.advanced(Float.NaN, 0x000000, 0xFFFFFF));
        expectIllegal(() -> new TextTransform(Float.NaN, 0.0F, 0.0F, false, false, null));
        expectIllegal(() -> TextShadow.offset(Float.POSITIVE_INFINITY, 0.0F));
        expectIllegal(() -> new TextRenderOffset(0.0F, Float.NEGATIVE_INFINITY));

        TextGradient gradient = TextGradient.horizontal(0x000000, 0xFFFFFF);
        int color = gradient.colorAt(
                TextRenderBounds.of(0.0F, 0.0F, 10.0F, 10.0F),
                TextRenderBounds.of(4.0F, 0.0F, 6.0F, 10.0F),
                0x7F000000
        );
        require(color == 0x7F808080, "text gradients must preserve the fallback alpha while interpolating RGB");
    }

    private static void composesEntityOutlineParts() {
        OutlineStyle body = OutlineStyle.of("#F72585");
        OutlineStyle helmet = OutlineStyle.of("#4CC9F0");
        OutlineStyle hand = OutlineStyle.of("#7CFF6B");
        EntityOutline outline = EntityOutline.builder()
                .body(body)
                .armor(EquipmentSlot.HEAD, helmet)
                .mainHand(hand)
                .build();
        require(outline.style(EntityOutlinePart.BODY).orElseThrow() == body, "body style should be retained");
        require(outline.armor(EquipmentSlot.HEAD).orElseThrow() == helmet, "armor slots should map to their part");
        require(outline.style(EntityOutlinePart.MAIN_HAND).orElseThrow() == hand, "hand styles should be retained");
        require(outline.without(EntityOutlinePart.MAIN_HAND).style(EntityOutlinePart.MAIN_HAND).isEmpty(), "parts should be removable");
        require(EntityOutline.attached(body).styles().size() == EntityOutlinePart.values().length, "attached outlines should include every part");
    }

    private static void validatesOutlineRenderingContracts() {
        BlockOutlineGeometryGroupsTestSupport.verify();

        ClientItemOutlineManager.validate(ItemOutlineTarget.GUI, OutlineStyle.DEFAULT);
        expectIllegal(() -> ClientItemOutlineManager.validate(
                ItemOutlineTarget.GUI,
                OutlineStyle.builder().thickness(9).build()
        ));
        expectIllegal(() -> ClientItemOutlineManager.validate(
                ItemOutlineTarget.GUI,
                OutlineStyle.builder().matchModelLayer().build()
        ));
        expectIllegal(() -> ClientItemOutlineManager.validate(
                ItemOutlineTarget.GUI,
                OutlineStyle.builder().occludedByWalls().build()
        ));
        expectIllegal(() -> ClientItemOutlineManager.validate(
                ItemOutlineTarget.GUI,
                OutlineStyle.builder().consistentThickness(false).build()
        ));
    }

    private static void expectParticleFailure(String json) {
        try {
            ParticleDefinitionLoader.parse(Identifier.fromNamespaceAndPath("test", "invalid"), new StringReader(json));
            throw new AssertionError("Expected invalid particle definition to fail");
        } catch (ParticleDefinitionException expected) {
        }
    }

    private static void expectIllegal(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected an illegal argument failure");
        } catch (IllegalArgumentException expected) {
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
