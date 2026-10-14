package moth.butterflyapi.mixin;

//? if >=1.21.11 {
import moth.butterflyapi.text.TextEffect;
import moth.butterflyapi.text.TextRenderStyle;
import moth.butterflyapi.text.internal.TextEffectCarrier;
import moth.butterflyapi.text.internal.TextRenderStyleCarrier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
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
                    "withColor(Lnet/minecraft/network/chat/TextColor;)Lnet/minecraft/network/chat/Style;",
                    "withColor(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/Style;",
                    "withColor(I)Lnet/minecraft/network/chat/Style;",
                    "withShadowColor(I)Lnet/minecraft/network/chat/Style;",
                    "withoutShadow()Lnet/minecraft/network/chat/Style;",
                    "withBold(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;",
                    "withItalic(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;",
                    "withUnderlined(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;",
                    "withStrikethrough(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;",
                    "withObfuscated(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;",
                    "withClickEvent(Lnet/minecraft/network/chat/ClickEvent;)Lnet/minecraft/network/chat/Style;",
                    "withHoverEvent(Lnet/minecraft/network/chat/HoverEvent;)Lnet/minecraft/network/chat/Style;",
                    "withInsertion(Ljava/lang/String;)Lnet/minecraft/network/chat/Style;",
                    "withFont(Lnet/minecraft/network/chat/FontDescription;)Lnet/minecraft/network/chat/Style;",
                    "applyFormat(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/Style;",
                    "applyFormats([Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/Style;"
            },
            at = @At("RETURN")
    )
    private void butterflyApi$copyEffect(CallbackInfoReturnable<Style> cir) {
        butterflyApi$copyTo(cir.getReturnValue(), butterflyApi$textEffect, butterflyApi$textRenderStyle);
    }

    @Inject(method = "applyLegacyFormat", at = @At("RETURN"))
    private void butterflyApi$copyExclusiveEffect(ChatFormatting formatting, CallbackInfoReturnable<Style> cir) {
        TextEffect effect = formatting == ChatFormatting.RESET || butterflyApi$isColor(formatting) ? null : butterflyApi$textEffect;
        TextRenderStyle renderStyle = formatting == ChatFormatting.RESET || butterflyApi$isColor(formatting) ? null : butterflyApi$textRenderStyle;
        butterflyApi$copyTo(cir.getReturnValue(), effect, renderStyle);
    }

    @Inject(method = "applyTo", at = @At("RETURN"))
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

    @Unique
    private static boolean butterflyApi$isColor(ChatFormatting formatting) {
//? if >=26.2 {
/*
        return formatting.ordinal() <= ChatFormatting.WHITE.ordinal();
*///?} else {
        return formatting.isColor();
//?}
    }
}
//?}
