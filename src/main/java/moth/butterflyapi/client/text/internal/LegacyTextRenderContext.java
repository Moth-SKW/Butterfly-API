package moth.butterflyapi.client.text.internal;

//? if <1.21.11 {
/*
import moth.butterflyapi.text.TextRenderBounds;
import moth.butterflyapi.text.TextEffect;
import moth.butterflyapi.text.TextEffectRenderContext;
import moth.butterflyapi.text.TextEffects;
import moth.butterflyapi.text.TextRenderOffset;
import moth.butterflyapi.text.TextRenderStyle;
import moth.butterflyapi.text.TextRenderStyles;
import moth.butterflyapi.text.TextShadow;
import moth.butterflyapi.text.TextTransform;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public final class LegacyTextRenderContext {
    private static final ThreadLocal<Deque<Plan>> PLANS = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<ShadowPass> PENDING_SHADOW_PASS = new ThreadLocal<>();

    private LegacyTextRenderContext() {
    }

    public static void begin(
            FormattedCharSequence text,
            float x,
            float y,
            boolean shadow,
            AdvanceResolver advances
    ) {
        Map<TextRenderStyle, TextRenderBounds> clusters = new HashMap<>();
        Map<Integer, TextRenderBounds> glyphs = new HashMap<>();
        Appearance[] uniformAppearance = new Appearance[1];
        TextRenderBounds[] uniformBounds = new TextRenderBounds[1];
        boolean[] uniform = {true};
        float[] cursor = {x};
        text.accept((index, style, codePoint) -> {
            float advance = Math.max(0.0F, advances.advance(style, codePoint));
            TextRenderBounds glyph = TextRenderBounds.of(cursor[0], y, cursor[0] + Math.max(advance, 1.0F), y + 9.0F);
            TextRenderStyle renderStyle = TextRenderStyles.get(style);
            Appearance appearance = new Appearance(renderStyle, TextEffects.effect(style));
            if (renderStyle != null) {
                clusters.merge(renderStyle, glyph, TextRenderBounds::union);
                glyphs.put(index, glyph);
            }
            if (uniformAppearance[0] == null) {
                uniformAppearance[0] = appearance;
                uniformBounds[0] = glyph;
            } else {
                uniform[0] &= uniformAppearance[0].equals(appearance);
                uniformBounds[0] = uniformBounds[0].union(glyph);
            }
            cursor[0] += advance;
            return true;
        });
        PLANS.get().push(new Plan(
                clusters,
                glyphs,
                uniformAppearance[0],
                uniformBounds[0],
                uniform[0],
                frameTime(text, x, y, shadow)
        ));
    }

    public static void end() {
        Deque<Plan> plans = PLANS.get();
        if (!plans.isEmpty()) {
            plans.pop();
        }
        if (plans.isEmpty()) {
            PLANS.remove();
        }
    }

    public static Plan current() {
        return PLANS.get().peek();
    }

    @FunctionalInterface
    public interface AdvanceResolver {
        float advance(Style style, int codePoint);
    }

    private static long frameTime(FormattedCharSequence text, float x, float y, boolean shadow) {
        long frameTime = System.nanoTime() / 16_666_667L;
        if (shadow) {
            PENDING_SHADOW_PASS.set(new ShadowPass(text, x, y, frameTime));
            return frameTime;
        }
        ShadowPass pending = PENDING_SHADOW_PASS.get();
        if (pending != null && pending.matches(text, x, y)) {
            PENDING_SHADOW_PASS.remove();
            return pending.frameTime();
        }
        PENDING_SHADOW_PASS.remove();
        return frameTime;
    }

    public record Plan(
            Map<TextRenderStyle, TextRenderBounds> clusters,
            Map<Integer, TextRenderBounds> glyphs,
            Appearance uniformAppearance,
            TextRenderBounds uniformBounds,
            boolean uniform,
            long frameTime
    ) {
        public TextRenderBounds clusterBounds(TextRenderStyle style, TextRenderBounds fallback) {
            return clusters.getOrDefault(style, fallback);
        }

        public TextRenderBounds glyphBounds(int index, TextRenderBounds fallback) {
            return glyphs.getOrDefault(index, fallback);
        }

        public BackgroundTransform backgroundTransform(boolean shadow) {
            if (!uniform || uniformAppearance == null || uniformBounds == null) {
                return null;
            }
            TextRenderStyle renderStyle = uniformAppearance.renderStyle();
            TextTransform transform = renderStyle == null
                    ? TextTransform.NONE
                    : shadow ? renderStyle.shadow().transform() : renderStyle.transform();
            TextRenderOffset offset = TextRenderOffset.ZERO;
            TextEffect effect = uniformAppearance.effect();
            if (effect != null) {
                int glyphKey = Math.round(uniformBounds.minX() * 31.0F + uniformBounds.minY() * 17.0F);
                offset = effect.offset(new TextEffectRenderContext(
                        uniformBounds.minX(),
                        uniformBounds.minY(),
                        uniformBounds.maxX(),
                        uniformBounds.maxY(),
                        glyphKey,
                        frameTime
                ));
            }
            if (shadow && renderStyle != null) {
                TextShadow textShadow = renderStyle.shadow();
                offset = new TextRenderOffset(
                        offset.x() + textShadow.offsetX(),
                        offset.y() + textShadow.offsetY()
                );
            }
            return new BackgroundTransform(transform, uniformBounds, offset);
        }
    }

    public record Appearance(TextRenderStyle renderStyle, TextEffect effect) {
    }

    public record BackgroundTransform(TextTransform transform, TextRenderBounds bounds, TextRenderOffset offset) {
    }

    private record ShadowPass(FormattedCharSequence text, float x, float y, long frameTime) {
        private boolean matches(FormattedCharSequence other, float otherX, float otherY) {
            return text == other && x == otherX && y == otherY;
        }
    }
}
*///?}
