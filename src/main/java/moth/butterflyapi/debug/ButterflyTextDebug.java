package moth.butterflyapi.debug;

import moth.butterflyapi.client.text.ClientTexts;
import moth.butterflyapi.client.text.ExpandableTooltip;
import moth.butterflyapi.client.text.ExpandableTooltips;
import moth.butterflyapi.client.text.TooltipExpandKey;
import moth.butterflyapi.text.ShakyTextEffect;
import moth.butterflyapi.text.ShakyTextProfile;
import moth.butterflyapi.text.Texts;
import moth.butterflyapi.text.TextShakingOptions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ButterflyTextDebug {
    private static boolean initialized;

    private ButterflyTextDebug() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        registerSwordTooltips();
        registerActionBarStressTest();
    }

    private static void registerSwordTooltips() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            for (SwordTextTest test : TESTS) {
                if (stack.isOf(test.item())) {
                    applyTooltip(test, lines);
                    return;
                }
            }
        });
    }

    private static void applyTooltip(SwordTextTest test, List<Text> lines) {
        Text title = Texts.builder(test.name())
                .color(test.color())
                .bold()
                .shaky(1.0F)
                .build();
        if (lines.isEmpty()) {
            lines.add(title);
        } else {
            lines.set(0, title);
        }
        lines.add(Texts.builder()
                .text("TextBuilder")
                .color("#A9C7FF")
                .text(" / ")
                .color("#707070")
                .text(test.area())
                .color(test.color())
                .build());
        test.addDetails(lines);
    }

    private static final List<SwordTextTest> TESTS = List.of(
            SwordTextTest.expandable(Items.WOODEN_SWORD, "Wooden Sword", "construction", "#C88A4A", ButterflyTextDebug::constructionLines),
            SwordTextTest.expandable(Items.COPPER_SWORD, "Copper Sword", "translations", "#F19A55", ButterflyTextDebug::translationLines),
            SwordTextTest.expandable(Items.STONE_SWORD, "Stone Sword", "formatting", "#A7ADB4", ButterflyTextDebug::formattingLines),
            SwordTextTest.direct(Items.GOLDEN_SWORD, "Golden Sword", "nested actions", "#FFD447", ButterflyTextDebug::expandableTooltip),
            SwordTextTest.expandable(Items.IRON_SWORD, "Iron Sword", "fonts + reset", "#DDE7EF", ButterflyTextDebug::fontLines),
            SwordTextTest.expandable(Items.DIAMOND_SWORD, "Diamond Sword", "shaky profiles", "#56F0FF", ButterflyTextDebug::shakyLines),
            SwordTextTest.expandable(Items.NETHERITE_SWORD, "Netherite Sword", "legacy compatibility", "#FF4FD8", ButterflyTextDebug::legacyLines)
    );

    private static List<Text> constructionLines() {
        List<Text> lines = new ArrayList<>();
        lines.add(Texts.builder()
                .literal("literal")
                .color("#C88A4A")
                .space()
                .append(Texts.builder("append").color("#FFE0B2"))
                .space()
                .text("space()")
                .color("#7CFF6B")
                .build());
        lines.add(Texts.builder("horizontal gradient across this full line")
                .gradientHorizontal("#FF3636", "#FF4FD8", "#7A5CFF")
                .shadowColor("#AA000000")
                .shadowOffset(1.5F, 1.5F)
                .build());
        lines.addAll(Texts.builder()
                .line("line(String)")
                .color("#E3B37A")
                .line(Texts.color("line(Text)", "#FFCE91"))
                .buildLines());
        return List.copyOf(lines);
    }

    private static List<Text> translationLines() {
        List<Text> lines = new ArrayList<>();
        lines.add(Texts.builder()
                .translatable("item.minecraft.copper_sword")
                .color("#F19A55")
                .text(" + ")
                .color("#707070")
                .translatable("item.minecraft.diamond_sword")
                .color("#56F0FF")
                .build());
        lines.add(Texts.builder("advanced gradient rotated 35 degrees")
                .gradientAdvanced(35.0F, "#FF5A5A", "#FF9AF2", "#9D5CFF", "#56F0FF", "#7CFF6B")
                .opacity(0.85F)
                .shadowColor("#88401020")
                .shadowOpacity(0.6F)
                .build());
        lines.addAll(Texts.builder("resetStyle()")
                .color("#FFB070")
                .italic()
                .resetStyle()
                .text(" vanilla style")
                .blankLine()
                .text("blankLine() + buildLines()")
                .color(0xF19A55)
                .buildLines());
        return List.copyOf(lines);
    }

    private static List<Text> formattingLines() {
        return List.of(
                Texts.builder("bold")
                        .color("#BFC7D1")
                        .bold()
                        .text(" italic")
                        .italic()
                        .text(" underline")
                        .underline()
                        .build(),
                Texts.builder("strikethrough")
                        .color("#E8EEF5")
                        .strikethrough()
                        .build(),
                Texts.builder("obf")
                        .color("#5A7CFF")
                        .obfuscated()
                        .build(),
                Texts.builder("FLIP X: R K P 2 7")
                        .color("#FFD447")
                        .flipLetterX()
                        .build(),
                Texts.builder("FLIP Y: R K P 2 7")
                        .color("#56F0FF")
                        .flipLetterY()
                        .build(),
                Texts.builder("CLUSTER FLIP X + ROTATE")
                        .gradientHorizontal("#FF9AF2", "#FFFFFF", "#56F0FF")
                        .flipTextX()
                        .rotate(-28.0F)
                        .textOffset(6.0F, 0.0F)
                        .build(),
                Texts.builder()
                        .formatted(Formatting.BLUE)
                        .text("formatted()")
                        .build()
                ,
                Texts.builder("vararg formatting")
                        .formatted(Formatting.AQUA, Formatting.BOLD, Formatting.UNDERLINE)
                        .build()
        );
    }

    private static ExpandableTooltip expandableTooltip() {
        TooltipExpandKey gKey = TooltipExpandKey.key("G", InputUtil.GLFW_KEY_G);
        return ExpandableTooltip.builder()
                .line(Texts.builder("Shift expanded this tooltip")
                        .gradientHorizontal("#FFD447", "#FF9AF2")
                        .shadowColor("#AA4A2300")
                        .shadowOffset(2.0F, 0.5F)
                        .build())
                .line(Texts.builder("Release Shift while holding Alt")
                        .color("#FFED99")
                        .shadowOpacity(0.35F)
                        .build())
                .expandable(TooltipExpandKey.ALT, alt -> alt
                        .line(Texts.builder("Alt nested tooltip is active")
                                .gradientVertical("#56F0FF", "#9DFF6E")
                                .shadowRotate(10.0F)
                                .build())
                        .line(Texts.builder("Release Alt to return outward").color("#A9C7FF").build())
                        .link(TooltipExpandKey.CONTROL, "link", "https://github.com/Moth-SKW/Butterfly-API-1.21.11")
                        .action(gKey, "action bar", () -> ClientTexts.actionBar(
                                Texts.builder("Tooltip G action fired")
                                        .gradientHorizontal("#7CFF6B", "#56F0FF", "#FF4FD8")
                                        .shaky(1.0F)
                                        .build()
                        )))
                .build();
    }

    private static List<Text> fontLines() {
        Identifier defaultFont = Identifier.ofVanilla("default");
        return List.of(
                Texts.builder("font(minecraft:default)")
                        .color("#DDE7EF")
                        .font(defaultFont)
                        .space()
                        .text("registered")
                        .color("#9DFF6E")
                        .build(),
                Texts.builder("noColor()")
                        .color("#A9C7FF")
                        .noColor()
                        .space()
                        .text("noShadow()")
                        .shadow(0xAA111111)
                        .noShadow()
                        .space()
                        .text("hover item")
                        .hover(new ItemStack(Items.IRON_SWORD))
                        .build()
                ,
                Texts.builder("shadow offset + color")
                        .color("#DDE7EF")
                        .shadowColor("#CC2244FF")
                        .shadowOffset(3.0F, 0.5F)
                        .build(),
                Texts.builder("shadow opacity + mirror")
                        .color("#FFFFFF")
                        .shadowColor("#AAFF4FD8")
                        .shadowOpacity(0.45F)
                        .shadowMirrorX()
                        .build(),
                Texts.builder("text noShadow()")
                        .gradientHorizontal("#DDE7EF", "#56F0FF")
                        .noShadow()
                        .build()
        );
    }

    private static List<Text> shakyLines() {
        TextShakingOptions jitter = TextShakingOptions.DEFAULT
                .withStrength(1.6F)
                .withProfile(ShakyTextProfile.JITTER)
                .withSeed(0xD1A0L);
        TextShakingOptions wave = TextShakingOptions.DEFAULT
                .withStrength(1.2F)
                .withProfile(ShakyTextProfile.WAVE)
                .withSpeed(1.3F)
                .withSeed(0xD1A1L);
        TextShakingOptions drift = TextShakingOptions.DEFAULT
                .withStrength(1.4F)
                .withProfile(ShakyTextProfile.DRIFT)
                .withSpeed(1.6F)
                .withSeed(0xD1A2L);
        return List.of(
                Texts.builder("jitter")
                        .gradientHorizontal("#56F0FF", "#FFFFFF")
                        .shaky(jitter)
                        .build(),
                Texts.builder("wave")
                        .color("#9DFF6E")
                        .shaky(wave)
                        .space()
                        .text("drift")
                        .color("#FF9AF2")
                        .textOpacity(0.8F)
                        .shaky(drift)
                        .build(),
                Texts.builder("effect()")
                        .color("#D6C5FF")
                        .effect(new ShakyTextEffect(jitter))
                        .space()
                        .text("clearEffect()")
                        .clearEffect()
                        .color("#FFFFFF")
                        .build()
        );
    }

    private static List<Text> legacyLines() {
        TextShakingOptions options = TextShakingOptions.DEFAULT
                .withStrength(1.4F)
                .withProfile(ShakyTextProfile.DRIFT)
                .withSeed(0xBEEFL);
        return List.of(
                Texts.builder("builder legacy marker survives")
                        .color("#FF4FD8")
                        .shaky(options)
                        .build(),
                Text.literal(Texts.legacyHex("#7CFF6B") + "\u00a7`legacy hex + \u00a7` shaky")
                        .formatted(Formatting.ITALIC),
                Text.literal("\u00a7`shaky first \u00a7cvanilla red clears shaking"),
                Texts.builder("hover(event)")
                        .color("#FF9AF2")
                        .hover(new HoverEvent.ShowText(Texts.color("Raw HoverEvent path", "#56F0FF")))
                        .build(),
                Texts.builder("legacy + modern gradient")
                        .gradientHorizontal("#7CFF6B", "#FF4FD8", "#56F0FF")
                        .shadowColor("#88000000")
                        .shaky(options)
                        .build()
        );
    }

    private static void registerActionBarStressTest() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null || client.inGameHud.getTicks() % 8 != 0) {
                return;
            }
            TextShakingOptions options = TextShakingOptions.DEFAULT
                    .withStrength(1.4F)
                    .withProfile(ShakyTextProfile.DRIFT)
                    .withSpeed(1.6F);
            ClientTexts.actionBar(
                    Texts.builder("Butterfly API 1.3.0 text stress").color("#72FFE2").shaky(options).build()
            );
        });
    }

    private record SwordTextTest(Item item, String name, String area, String color, DetailAppender details) {
        static SwordTextTest expandable(Item item, String name, String area, String color, Supplier<List<Text>> lines) {
            return new SwordTextTest(item, name, area, color, tooltip -> ExpandableTooltips.add(tooltip, lines));
        }

        static SwordTextTest direct(Item item, String name, String area, String color, Supplier<ExpandableTooltip> tooltip) {
            return new SwordTextTest(item, name, area, color, lines -> tooltip.get().addTo(lines));
        }

        void addDetails(List<Text> lines) {
            details.add(lines);
        }
    }

    @FunctionalInterface
    private interface DetailAppender {
        void add(List<Text> lines);
    }
}
