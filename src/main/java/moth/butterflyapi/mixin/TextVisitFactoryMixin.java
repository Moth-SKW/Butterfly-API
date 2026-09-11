package moth.butterflyapi.mixin;

import moth.butterflyapi.text.ShakyTextEffect;
import moth.butterflyapi.text.TextColors;
import moth.butterflyapi.text.TextEffects;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextVisitFactory.class)
public abstract class TextVisitFactoryMixin {
    @Inject(
            method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void butterflyApi$visitFormatted(
            String text,
            int startIndex,
            Style startingStyle,
            Style resetStyle,
            CharacterVisitor visitor,
            CallbackInfoReturnable<Boolean> cir
    ) {
        cir.setReturnValue(butterflyApi$visit(text, startIndex, startingStyle, resetStyle, visitor));
    }

    private static boolean butterflyApi$visit(String text, int startIndex, Style startingStyle, Style resetStyle, CharacterVisitor visitor) {
        int length = text.length();
        Style current = startingStyle;
        for (int index = startIndex; index < length; index++) {
            char character = text.charAt(index);
            if (character == Formatting.FORMATTING_CODE_PREFIX) {
                if (index + 1 >= length) {
                    break;
                }
                int consumed = butterflyApi$formattingLength(text, index);
                current = butterflyApi$styleAfterFormatting(text, index, current, resetStyle);
                index += consumed;
                continue;
            }
            if (Character.isHighSurrogate(character)) {
                if (index + 1 >= length) {
                    return visitor.accept(index, current, 0xFFFD);
                }
                char low = text.charAt(index + 1);
                if (Character.isLowSurrogate(low)) {
                    if (!visitor.accept(index, current, Character.toCodePoint(character, low))) {
                        return false;
                    }
                    index++;
                    continue;
                }
                if (!visitor.accept(index, current, 0xFFFD)) {
                    return false;
                }
                continue;
            }
            if (!visitor.accept(index, current, character)) {
                return false;
            }
        }
        return true;
    }

    private static Style butterflyApi$styleAfterFormatting(String text, int index, Style current, Style resetStyle) {
        char code = text.charAt(index + 1);
        if (code == '`') {
            return TextEffects.withEffect(current, ShakyTextEffect.DEFAULT);
        }
        if (code == '#' && index + 7 < text.length()) {
            String hex = text.substring(index + 2, index + 8);
            if (butterflyApi$isHex(hex)) {
                return TextEffects.clear(current.withColor(TextColors.rgb(hex)));
            }
        }
        Integer rgb = butterflyApi$legacyHexColor(text, index);
        if (rgb != null) {
            return TextEffects.clear(current.withColor(rgb));
        }
        Formatting formatting = Formatting.byCode(code);
        if (formatting == null) {
            return current;
        }
        if (formatting == Formatting.RESET) {
            return TextEffects.clear(resetStyle);
        }
        return current.withExclusiveFormatting(formatting);
    }

    private static int butterflyApi$formattingLength(String text, int index) {
        char code = text.charAt(index + 1);
        if (code == '`') {
            return 1;
        }
        if (code == '#' && index + 7 < text.length() && butterflyApi$isHex(text.substring(index + 2, index + 8))) {
            return 7;
        }
        if (butterflyApi$legacyHexColor(text, index) != null) {
            return 13;
        }
        return 1;
    }

    private static Integer butterflyApi$legacyHexColor(String text, int index) {
        if (index + 13 >= text.length() || Character.toLowerCase(text.charAt(index + 1)) != 'x') {
            return null;
        }
        StringBuilder hex = new StringBuilder(6);
        for (int offset = 2; offset <= 12; offset += 2) {
            if (text.charAt(index + offset) != Formatting.FORMATTING_CODE_PREFIX) {
                return null;
            }
            char digit = text.charAt(index + offset + 1);
            if (Character.digit(digit, 16) < 0) {
                return null;
            }
            hex.append(digit);
        }
        return TextColors.rgb(hex.toString());
    }

    private static boolean butterflyApi$isHex(String value) {
        for (int index = 0; index < value.length(); index++) {
            if (Character.digit(value.charAt(index), 16) < 0) {
                return false;
            }
        }
        return true;
    }
}
