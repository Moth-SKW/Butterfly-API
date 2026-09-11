package moth.butterflyapi.mixin;

import moth.butterflyapi.text.TextEffect;
import moth.butterflyapi.text.TextRenderStyle;
import moth.butterflyapi.text.internal.TextEffectCarrier;
import moth.butterflyapi.text.internal.TextRenderStyleCarrier;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Style.class)
public abstract class StyleTextEffectMixin implements TextEffectCarrier, TextRenderStyleCarrier {
    @Unique
    @Nullable
    private TextEffect butterflyApi$textEffect;
    @Unique
    @Nullable
    private TextRenderStyle butterflyApi$textRenderStyle;

    @Override
    @Nullable
    public TextEffect butterflyApi$textEffect() {
        return butterflyApi$textEffect;
    }

    @Override
    public void butterflyApi$setTextEffect(@Nullable TextEffect effect) {
        butterflyApi$textEffect = effect;
    }

    @Override
    @Nullable
    public TextRenderStyle butterflyApi$textRenderStyle() {
        return butterflyApi$textRenderStyle;
    }

    @Override
    public void butterflyApi$setTextRenderStyle(@Nullable TextRenderStyle renderStyle) {
        butterflyApi$textRenderStyle = renderStyle;
    }

    @Inject(
            method = {
                    "withColor(Lnet/minecraft/text/TextColor;)Lnet/minecraft/text/Style;",
                    "withColor(Lnet/minecraft/util/Formatting;)Lnet/minecraft/text/Style;",
                    "withColor(I)Lnet/minecraft/text/Style;",
                    "withShadowColor(I)Lnet/minecraft/text/Style;",
                    "withoutShadow()Lnet/minecraft/text/Style;",
                    "withBold(Ljava/lang/Boolean;)Lnet/minecraft/text/Style;",
                    "withItalic(Ljava/lang/Boolean;)Lnet/minecraft/text/Style;",
                    "withUnderline(Ljava/lang/Boolean;)Lnet/minecraft/text/Style;",
                    "withStrikethrough(Ljava/lang/Boolean;)Lnet/minecraft/text/Style;",
                    "withObfuscated(Ljava/lang/Boolean;)Lnet/minecraft/text/Style;",
                    "withClickEvent(Lnet/minecraft/text/ClickEvent;)Lnet/minecraft/text/Style;",
                    "withHoverEvent(Lnet/minecraft/text/HoverEvent;)Lnet/minecraft/text/Style;",
                    "withInsertion(Ljava/lang/String;)Lnet/minecraft/text/Style;",
                    "withFont(Lnet/minecraft/text/StyleSpriteSource;)Lnet/minecraft/text/Style;",
                    "withFormatting(Lnet/minecraft/util/Formatting;)Lnet/minecraft/text/Style;",
                    "withFormatting([Lnet/minecraft/util/Formatting;)Lnet/minecraft/text/Style;"
            },
            at = @At("RETURN")
    )
    private void butterflyApi$copyEffect(CallbackInfoReturnable<Style> cir) {
        butterflyApi$copyTo(cir.getReturnValue(), butterflyApi$textEffect, butterflyApi$textRenderStyle);
    }

    @Inject(method = "withExclusiveFormatting", at = @At("RETURN"))
    private void butterflyApi$copyExclusiveEffect(Formatting formatting, CallbackInfoReturnable<Style> cir) {
        TextEffect effect = formatting == Formatting.RESET || formatting.isColor() ? null : butterflyApi$textEffect;
        TextRenderStyle renderStyle = formatting == Formatting.RESET || formatting.isColor() ? null : butterflyApi$textRenderStyle;
        butterflyApi$copyTo(cir.getReturnValue(), effect, renderStyle);
    }

    @Inject(method = "withParent", at = @At("RETURN"))
    private void butterflyApi$inheritParentEffect(Style parent, CallbackInfoReturnable<Style> cir) {
        TextEffect effect = butterflyApi$textEffect;
        Object parentObject = parent;
        if (effect == null && parentObject instanceof TextEffectCarrier carrier) {
            effect = carrier.butterflyApi$textEffect();
        }
        TextRenderStyle renderStyle = butterflyApi$textRenderStyle;
        if (renderStyle == null && parentObject instanceof TextRenderStyleCarrier carrier) {
            renderStyle = carrier.butterflyApi$textRenderStyle();
        }
        butterflyApi$copyTo(cir.getReturnValue(), effect, renderStyle);
    }

    @Unique
    private void butterflyApi$copyTo(Style target, @Nullable TextEffect effect, @Nullable TextRenderStyle renderStyle) {
        Object targetObject = target;
        if (targetObject instanceof TextEffectCarrier carrier) {
            carrier.butterflyApi$setTextEffect(effect);
        }
        if (targetObject instanceof TextRenderStyleCarrier carrier) {
            carrier.butterflyApi$setTextRenderStyle(renderStyle);
        }
    }
}
