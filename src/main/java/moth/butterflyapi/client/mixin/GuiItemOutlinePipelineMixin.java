package moth.butterflyapi.client.mixin;

import moth.butterflyapi.client.outline.internal.GuiItemOutlinePass;
import moth.butterflyapi.client.outline.internal.GuiItemOutlinePipelines;
import moth.butterflyapi.client.outline.internal.GuiItemOutlineState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

//? if >=26.1 {
/*
import net.minecraft.client.gui.render.GuiItemAtlas;
*///?}
//? if >=26.3 {
/*
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
*///?} else if >=26.1 {
/*
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
*///?} else if >=1.21.11 {
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
//?}

//? if >=26.3 {
/*
@Mixin(GuiRenderer.class)
public abstract class GuiItemOutlinePipelineMixin {
    @Unique
    private static final ThreadLocal<GuiItemOutlinePass> butterflyApi$currentPass = new ThreadLocal<>();

    @Inject(method = "submitBlitFromItemAtlas", at = @At("HEAD"), require = 1)
    private void butterflyApi$captureGuiItemOutlinePass(
            GuiItemRenderState state,
            GuiItemAtlas.SlotView slot,
            CallbackInfo ci
    ) {
        butterflyApi$setCurrentPass(state);
    }

    @Inject(method = "submitBlitFromItemAtlas", at = @At("RETURN"), require = 1)
    private void butterflyApi$clearGuiItemOutlinePass(
            GuiItemRenderState state,
            GuiItemAtlas.SlotView slot,
            CallbackInfo ci
    ) {
        butterflyApi$currentPass.remove();
    }

    @ModifyArgs(
            method = "submitBlitFromItemAtlas",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/state/gui/BlitRenderState;<init>(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/client/gui/render/TextureSetup;Lorg/joml/Matrix3x2fc;IIIIFFFFILnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)V"
            ),
            require = 1
    )
    private void butterflyApi$applyGuiItemOutline(Args arguments) {
        GuiItemOutlinePass pass = butterflyApi$currentPass.get();
        if (pass == null) {
            return;
        }
        arguments.set(0, GuiItemOutlinePipelines.pipeline(pass));
        arguments.set(11, pass.color());
    }

    @Unique
    private static void butterflyApi$setCurrentPass(GuiItemRenderState state) {
        if (state.itemStackRenderState() instanceof GuiItemOutlineState outlineState) {
            GuiItemOutlinePass pass = outlineState.butterflyApi$getGuiItemOutlinePass();
            if (pass != null) {
                butterflyApi$currentPass.set(pass);
                return;
            }
        }
        butterflyApi$currentPass.remove();
    }
}
*///?} else if >=26.1 {
/*
@Mixin(GuiRenderer.class)
public abstract class GuiItemOutlinePipelineMixin {
    @Unique
    private static final ThreadLocal<GuiItemOutlinePass> butterflyApi$currentPass = new ThreadLocal<>();

    @Inject(method = "submitBlitFromItemAtlas", at = @At("HEAD"), require = 1)
    private void butterflyApi$captureGuiItemOutlinePass(
            GuiItemRenderState state,
            GuiItemAtlas.SlotView slot,
            CallbackInfo ci
    ) {
        butterflyApi$setCurrentPass(state);
    }

    @Inject(method = "submitBlitFromItemAtlas", at = @At("RETURN"), require = 1)
    private void butterflyApi$clearGuiItemOutlinePass(
            GuiItemRenderState state,
            GuiItemAtlas.SlotView slot,
            CallbackInfo ci
    ) {
        butterflyApi$currentPass.remove();
    }

    @ModifyArgs(
            method = "submitBlitFromItemAtlas",
            at = @At(
                    value = "INVOKE",
                    // The constructor's matrix parameter changed from Matrix3x2f to
                    // Matrix3x2fc in 26.2.  Deliberately leave the descriptor open so
                    // Mixin resolves the sole BlitRenderState constructor on each ABI.
                    target = "Lnet/minecraft/client/renderer/state/gui/BlitRenderState;<init>"
            ),
            require = 1
    )
    private void butterflyApi$applyGuiItemOutline(Args arguments) {
        GuiItemOutlinePass pass = butterflyApi$currentPass.get();
        if (pass == null) {
            return;
        }
        arguments.set(0, GuiItemOutlinePipelines.pipeline(pass));
        arguments.set(11, pass.color());
    }

    @Unique
    private static void butterflyApi$setCurrentPass(GuiItemRenderState state) {
        if (state.itemStackRenderState() instanceof GuiItemOutlineState outlineState) {
            GuiItemOutlinePass pass = outlineState.butterflyApi$getGuiItemOutlinePass();
            if (pass != null) {
                butterflyApi$currentPass.set(pass);
                return;
            }
        }
        butterflyApi$currentPass.remove();
    }
}
*///?} else if >=1.21.11 {
@Mixin(GuiRenderer.class)
public abstract class GuiItemOutlinePipelineMixin {
    @Unique
    private static final ThreadLocal<GuiItemOutlinePass> butterflyApi$currentPass = new ThreadLocal<>();

    @Inject(method = "submitBlitFromItemAtlas", at = @At("HEAD"), require = 1)
    private void butterflyApi$captureGuiItemOutlinePass(
            GuiItemRenderState state,
            float u,
            float v,
            int atlasSize,
            int itemSize,
            CallbackInfo ci
    ) {
        butterflyApi$setCurrentPass(state);
    }

    @Inject(method = "submitBlitFromItemAtlas", at = @At("RETURN"), require = 1)
    private void butterflyApi$clearGuiItemOutlinePass(
            GuiItemRenderState state,
            float u,
            float v,
            int atlasSize,
            int itemSize,
            CallbackInfo ci
    ) {
        butterflyApi$currentPass.remove();
    }

    @ModifyArgs(
            method = "submitBlitFromItemAtlas",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/state/BlitRenderState;<init>(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/client/gui/render/TextureSetup;Lorg/joml/Matrix3x2f;IIIIFFFFILnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)V"
            ),
            require = 1
    )
    private void butterflyApi$applyGuiItemOutline(Args arguments) {
        GuiItemOutlinePass pass = butterflyApi$currentPass.get();
        if (pass == null) {
            return;
        }
        arguments.set(0, GuiItemOutlinePipelines.pipeline(pass));
        arguments.set(11, pass.color());
    }

    @Unique
    private static void butterflyApi$setCurrentPass(GuiItemRenderState state) {
        if (state.itemStackRenderState() instanceof GuiItemOutlineState outlineState) {
            GuiItemOutlinePass pass = outlineState.butterflyApi$getGuiItemOutlinePass();
            if (pass != null) {
                butterflyApi$currentPass.set(pass);
                return;
            }
        }
        butterflyApi$currentPass.remove();
    }
}
//?}
