plugins {
    id("dev.kikugie.stonecutter")
}

// Keep the 1.21.11 baseline live in the shared source tree. Stonecutter generates
// the 26.1 branch with the relevant //? conditionals applied when it is built.
stonecutter active "1.21.11"

stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod.version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"
    constants["unobfuscated_minecraft"] = current.parsed >= "26.1"

    replacements {
        string(current.parsed < "1.21.11") {
            replace("net.minecraft.resources.Identifier", "net.minecraft.resources.ResourceLocation")
            replace("Identifier.", "ResourceLocation.")
            replace("Identifier", "ResourceLocation")
            replace("net.minecraft.world.entity.decoration.painting.PaintingVariant", "net.minecraft.world.entity.decoration.PaintingVariant")
            replace("net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap", "net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap")
        }

        string(current.parsed >= "26.1") {
            // Fabric API's official-name migration.
            replace("net.fabricmc.fabric.api.itemgroup.v1", "net.fabricmc.fabric.api.creativetab.v1")
            replace("ItemGroupEvents", "CreativeModeTabEvents")
            replace("FabricItemGroup", "FabricCreativeModeTab")
            replace("ParticleFactoryRegistry", "ParticleProviderRegistry")
            replace("PendingParticleFactory", "PendingParticleProvider")
            replace("EntityModelLayerRegistry", "ModelLayerRegistry")
            replace("TexturedModelDataProvider", "TexturedLayerDefinitionProvider")
            replace("ServerEntityWorldChangeEvents", "ServerEntityLevelChangeEvents")
            replace("AFTER_ENTITY_CHANGE_WORLD", "AFTER_ENTITY_CHANGE_LEVEL")
            replace("AFTER_PLAYER_CHANGE_WORLD", "AFTER_PLAYER_CHANGE_LEVEL")
            replace("PayloadTypeRegistry.playS2C()", "PayloadTypeRegistry.clientboundPlay()")
            replace("PlayerLookup.world", "PlayerLookup.level")
            replace("modifyEntriesEvent", "modifyOutputEvent")
            replace(".registerReloader(", ".registerReloadListener(")
            replace("net.fabricmc.fabric.api.client.rendering.v1.world", "net.fabricmc.fabric.api.client.rendering.v1.level")
            replace("WorldRenderContext", "LevelRenderContext")
            replace("WorldRenderEvents", "LevelRenderEvents")
            replace("BEFORE_DEBUG_RENDER", "BEFORE_GIZMOS")
            replace(".matrices()", ".poseStack()")
            replace(".consumers()", ".bufferSource()")

            // Minecraft 26.1's GUI extraction and level-render-state packages.
            replace("GuiGraphics", "GuiGraphicsExtractor")
            replace(".drawString(", ".text(")
            replace(".drawCenteredString(", ".centeredText(")
            replace(".hLine(", ".horizontalLine(")
            replace(".vLine(", ".verticalLine(")
            replace("net.minecraft.client.renderer.state.LevelRenderState", "net.minecraft.client.renderer.state.level.LevelRenderState")
            replace("net.minecraft.client.renderer.state.CameraRenderState", "net.minecraft.client.renderer.state.level.CameraRenderState")
            replace("net.minecraft.world.level.BlockAndTintGetter", "net.minecraft.client.renderer.block.BlockAndTintGetter")
            replace(
                "Lnet/minecraft/client/renderer/LevelRenderer;addMainPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/culling/Frustum;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZLnet/minecraft/client/renderer/state/LevelRenderState;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
                "Lnet/minecraft/client/renderer/LevelRenderer;addMainPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/culling/Frustum;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZLnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/util/profiling/ProfilerFiller;Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;)V"
            )

            // The render pipeline stores depth test and depth write together in 26.1.
            replace("com.mojang.blaze3d.platform.DepthTestFunction", "com.mojang.blaze3d.pipeline.DepthStencilState")
            replace("DepthTestFunction.LEQUAL_DEPTH_TEST", "new DepthStencilState(com.mojang.blaze3d.platform.CompareOp.LESS_THAN_OR_EQUAL, false)")
            replace("DepthTestFunction.NO_DEPTH_TEST", "new DepthStencilState(com.mojang.blaze3d.platform.CompareOp.ALWAYS_PASS, false)")
            replace("DepthTestFunction", "DepthStencilState")
            replace(".withDepthTestFunction(", ".withDepthStencilState(")
            replace(".withDepthWrite(false)", "/* depth writing is disabled by the supplied depth-stencil state */")

            // Text glyphs now accept the immutable JOML view, and item hovers
            // use ItemStackTemplate rather than a mutable ItemStack.
            replace("import org.joml.Matrix4f;", "import org.joml.Matrix4f;\nimport org.joml.Matrix4fc;")
            replace("render(Matrix4f matrix,", "render(Matrix4fc matrix,")
            replace(
                "new HoverEvent.ShowItem(Objects.requireNonNull(stack, \"stack\").copy())",
                "new HoverEvent.ShowItem(net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(Objects.requireNonNull(stack, \"stack\")))"
            )
        }
    }
}

tasks.register("buildAllVersions") {
    group = "build"
    description = "Builds and collects Butterfly API for every configured Minecraft version."
    dependsOn(
        ":1.21.1:buildAndCollect",
        ":1.21.11:buildAndCollect",
        ":26.1:buildAndCollect",
        ":26.2:buildAndCollect",
        ":26.3:buildAndCollect"
    )
}
