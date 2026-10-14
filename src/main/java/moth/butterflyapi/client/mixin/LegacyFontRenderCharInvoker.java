package moth.butterflyapi.client.mixin;

//? if <1.21.11 {
/*

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Font.class)
public interface LegacyFontRenderCharInvoker {
    @Invoker("renderChar")
    void butterflyApi$renderChar(
            BakedGlyph glyph,
            boolean italic,
            boolean bold,
            float x,
            float y,
            float depth,
            Matrix4f matrix,
            VertexConsumer vertices,
            float red,
            float green,
            float blue,
            float alpha,
            int light
    );
}
*///?}
