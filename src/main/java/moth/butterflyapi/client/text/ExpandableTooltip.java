package moth.butterflyapi.client.text;

import net.minecraft.text.Text;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class ExpandableTooltip {
    private static final Set<String> ACTIVE_NODES = ConcurrentHashMap.newKeySet();

    private final TooltipExpandKey key;
    private final Text prompt;
    private final List<Entry> entries;

    private ExpandableTooltip(TooltipExpandKey key, Text prompt, List<Entry> entries) {
        this.key = Objects.requireNonNull(key, "key");
        this.prompt = Objects.requireNonNull(prompt, "prompt");
        this.entries = List.copyOf(entries);
    }

    public static Builder builder() {
        return builder(ExpandableTooltips.defaultKey());
    }

    public static Builder builder(TooltipExpandKey key) {
        return new Builder(key);
    }

    public void addTo(List<Text> tooltip) {
        Objects.requireNonNull(tooltip, "tooltip");
        tooltip.addAll(lines());
    }

    public List<Text> lines() {
        List<Text> lines = new ArrayList<>();
        appendTo(lines);
        return List.copyOf(lines);
    }

    public boolean isExpanded() {
        return updateState(true, "root");
    }

    private void appendTo(List<Text> lines) {
        appendTo(lines, true, "root");
    }

    private void appendTo(List<Text> lines, boolean parentVisible, String path) {
        boolean expanded = updateState(parentVisible, path);
        if (!expanded) {
            lines.add(prompt);
            return;
        }
        for (int index = 0; index < entries.size(); index++) {
            entries.get(index).appendTo(lines, true, path + "/" + index);
        }
    }

    private boolean updateState(boolean parentVisible, String path) {
        String id = nodeId(path);
        boolean pressed = key.isPressed();
        boolean latched = ACTIVE_NODES.contains(id);
        boolean selfExpanded = pressed && (parentVisible || latched);
        if (selfExpanded) {
            ACTIVE_NODES.add(id);
        } else if (!pressed) {
            ACTIVE_NODES.remove(id);
        }

        boolean childExpanded = false;
        for (int index = 0; index < entries.size(); index++) {
            Entry entry = entries.get(index);
            if (entry instanceof Nested nested && nested.tooltip().updateState(selfExpanded, path + "/" + index)) {
                childExpanded = true;
            }
        }
        return selfExpanded || childExpanded;
    }

    private String nodeId(String path) {
        return path + "|" + key.name().getString() + "|" + prompt.getString();
    }

    public static final class Builder {
        private final TooltipExpandKey key;
        private Text prompt;
        private final List<Entry> entries = new ArrayList<>();

        private Builder(TooltipExpandKey key) {
            this.key = Objects.requireNonNull(key, "key");
            this.prompt = ExpandableTooltips.defaultPrompt(key);
        }

        public Builder prompt(Text prompt) {
            this.prompt = Objects.requireNonNull(prompt, "prompt");
            return this;
        }

        public Builder line(String line) {
            return line(Text.literal(Objects.requireNonNull(line, "line")));
        }

        public Builder line(Text line) {
            entries.add(new Line(Objects.requireNonNull(line, "line")));
            return this;
        }

        public Builder lines(List<? extends Text> lines) {
            Objects.requireNonNull(lines, "lines");
            for (Text line : lines) {
                line(line);
            }
            return this;
        }

        public Builder action(TooltipExpandKey key, String actionName, Runnable action) {
            return action(key, TooltipActions.defaultPrompt(key, Text.literal(actionName)), actionName, action);
        }

        public Builder action(TooltipExpandKey key, Text actionName, Runnable action) {
            return action(key, TooltipActions.defaultPrompt(key, actionName), actionName.getString(), action);
        }

        public Builder action(TooltipExpandKey key, Text prompt, String actionId, Runnable action) {
            entries.add(new Action(key, prompt, actionId, action));
            return this;
        }

        public Builder link(TooltipExpandKey key, String label, String uri) {
            return link(key, Text.literal(label), URI.create(Objects.requireNonNull(uri, "uri")));
        }

        public Builder link(TooltipExpandKey key, Text label, URI uri) {
            Objects.requireNonNull(uri, "uri");
            return action(key, TooltipActions.defaultPrompt(key, label), "link:" + uri, () -> {
                net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
                if (client != null) {
                    net.minecraft.client.gui.screen.ConfirmLinkScreen.open(client.currentScreen, uri);
                }
            });
        }

        public Builder expandable(TooltipExpandKey key, Consumer<Builder> nested) {
            Builder builder = builder(key);
            Objects.requireNonNull(nested, "nested").accept(builder);
            return expandable(builder.build());
        }

        public Builder expandable(ExpandableTooltip tooltip) {
            entries.add(new Nested(Objects.requireNonNull(tooltip, "tooltip")));
            return this;
        }

        public ExpandableTooltip build() {
            return new ExpandableTooltip(key, prompt, entries);
        }
    }

    private sealed interface Entry permits Line, Action, Nested {
        void appendTo(List<Text> lines, boolean parentVisible, String path);
    }

    private record Line(Text text) implements Entry {
        @Override
        public void appendTo(List<Text> lines, boolean parentVisible, String path) {
            lines.add(text);
        }
    }

    private record Action(TooltipExpandKey key, Text prompt, String id, Runnable action) implements Entry {
        @Override
        public void appendTo(List<Text> lines, boolean parentVisible, String path) {
            lines.add(prompt);
            TooltipActions.runWhenPressed(key, id, action);
        }
    }

    private record Nested(ExpandableTooltip tooltip) implements Entry {
        @Override
        public void appendTo(List<Text> lines, boolean parentVisible, String path) {
            tooltip.appendTo(lines, parentVisible, path);
        }
    }
}
