package moth.butterflyapi.client.text;

import moth.butterflyapi.text.Texts;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class ExpandableTooltips {
    private static TooltipExpandKey defaultKey = TooltipExpandKey.SHIFT;

    private ExpandableTooltips() {
    }

    public static TooltipExpandKey defaultKey() {
        return defaultKey;
    }

    public static void setDefaultKey(TooltipExpandKey key) {
        defaultKey = Objects.requireNonNull(key, "key");
    }

    public static void add(List<Component> tooltip, Component expandedLine) {
        add(tooltip, defaultKey, List.of(expandedLine));
    }

    public static void add(List<Component> tooltip, List<? extends Component> expandedLines) {
        add(tooltip, defaultKey, expandedLines);
    }

    public static void add(List<Component> tooltip, Supplier<? extends List<? extends Component>> expandedLines) {
        add(tooltip, defaultKey, expandedLines);
    }

    public static void add(List<Component> tooltip, ExpandableTooltip expandableTooltip) {
        Objects.requireNonNull(expandableTooltip, "expandableTooltip").addTo(tooltip);
    }

    public static void add(List<Component> tooltip, TooltipExpandKey key, Component expandedLine) {
        add(tooltip, key, List.of(expandedLine));
    }

    public static void add(List<Component> tooltip, TooltipExpandKey key, List<? extends Component> expandedLines) {
        add(tooltip, key, defaultPrompt(key), expandedLines);
    }

    public static void add(List<Component> tooltip, TooltipExpandKey key, Supplier<? extends List<? extends Component>> expandedLines) {
        Objects.requireNonNull(expandedLines, "expandedLines");
        add(tooltip, key, defaultPrompt(key), expandedLines.get());
    }

    public static void add(List<Component> tooltip, TooltipExpandKey key, Component prompt, List<? extends Component> expandedLines) {
        Objects.requireNonNull(tooltip, "tooltip");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(prompt, "prompt");
        Objects.requireNonNull(expandedLines, "expandedLines");
        ExpandableTooltip.builder(key).prompt(prompt).lines(expandedLines).build().addTo(tooltip);
    }

    public static ExpandableTooltip.Builder builder() {
        return ExpandableTooltip.builder(defaultKey);
    }

    public static ExpandableTooltip.Builder builder(TooltipExpandKey key) {
        return ExpandableTooltip.builder(key);
    }

    public static Component defaultPrompt(TooltipExpandKey key) {
        Objects.requireNonNull(key, "key");
        return Texts.builder("Hold ")
                .color("#8A8A8A")
                .append(key.name())
                .color("#FFD447")
                .text(" to show more information")
                .color("#8A8A8A")
                .build();
    }
}
