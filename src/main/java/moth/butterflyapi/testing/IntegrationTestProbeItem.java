package moth.butterflyapi.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import moth.butterflyapi.client.text.ExpandableTooltips;
import moth.butterflyapi.client.text.TooltipActions;
import moth.butterflyapi.client.text.TooltipExpandKey;
import moth.butterflyapi.client.text.ClientTexts;
import moth.butterflyapi.text.Texts;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
//? if >=1.21.11 {
import net.minecraft.world.item.component.TooltipDisplay;
//?}
import net.minecraft.world.item.TooltipFlag;

@SuppressWarnings("deprecation")
final class IntegrationTestProbeItem extends Item {
    IntegrationTestProbeItem(Properties properties) {
        super(properties);
    }

//? if >=1.21.11 {
    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> tooltip,
            TooltipFlag flag
    ) {
        List<Component> lines = new ArrayList<>();
        lines.add(Texts.builder("Butterfly API ")
                .gradientHorizontal("#F72585", "#4CC9F0")
                .bold()
                .text("integration probe")
                .build());
        lines.add(Texts.builder("Transforms and shaky text are active")
                .color("#FFD166")
                .textOffset(0.5F, 0.0F)
                .shaky(0.5F)
                .build());
        ExpandableTooltips.add(
                lines,
                TooltipExpandKey.SHIFT,
                List.of(
                        Texts.color("Shift expansion confirmed", "#7CFF6B"),
                        Texts.color("Progress bars should animate below this item", "#8A8A8A")
                )
        );
        TooltipActions.add(
                lines,
                TooltipExpandKey.CONTROL,
                "a client text confirmation",
                () -> ClientTexts.actionBar(Texts.color("Tooltip action received", "#7CFF6B"))
        );
        lines.forEach(tooltip);
    }
//?} else {
/*
    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        List<Component> lines = integrationLines();
        tooltip.addAll(lines);
    }
*///?}

    private static List<Component> integrationLines() {
        List<Component> lines = new ArrayList<>();
        lines.add(Texts.builder("Butterfly API ")
                .gradientHorizontal("#F72585", "#4CC9F0")
                .bold()
                .text("integration probe")
                .build());
        lines.add(Texts.builder("Transforms and shaky text are active")
                .color("#FFD166")
                .textOffset(0.5F, 0.0F)
                .shaky(0.5F)
                .build());
        ExpandableTooltips.add(
                lines,
                TooltipExpandKey.SHIFT,
                List.of(
                        Texts.color("Shift expansion confirmed", "#7CFF6B"),
                        Texts.color("Progress bars should animate below this item", "#8A8A8A")
                )
        );
        TooltipActions.add(
                lines,
                TooltipExpandKey.CONTROL,
                "a client text confirmation",
                () -> ClientTexts.actionBar(Texts.color("Tooltip action received", "#7CFF6B"))
        );
        return lines;
    }
}
