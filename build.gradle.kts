import org.gradle.api.JavaVersion
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec

plugins {
    // Selects fabric-loom-remap for 1.21.11 and fabric-loom for 26.1.
    id("dev.kikugie.loom-back-compat")
    `maven-publish`
}

group = requireNotNull(property("mod.group"))
version = "${property("mod.version")}+${sc.current.version}"

base {
    archivesName = property("mod.id").toString()
}

val requiredJava = if (sc.current.parsed >= "26.1") 25 else 21
val isUnobfuscatedMinecraft = sc.current.parsed >= "26.1"

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")

    if (isUnobfuscatedMinecraft) {
        // Minecraft 26.1+ ships unobfuscated: Loom uses standard configurations
        // and there is deliberately no mappings dependency in this branch.
        implementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
        implementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    } else {
        // Minecraft 1.21.11 is obfuscated and uses official Mojang mappings.
        loomx.applyMojangMappings()
        modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
        modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    }
}

loom {
    // Version projects share the real resource tree rather than maintaining copies.
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.toVersion(requiredJava)
    targetCompatibility = JavaVersion.toVersion(requiredJava)
    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = requiredJava
    options.compilerArgs.add("-Xlint:deprecation")
}

tasks.processResources {
    if (sc.current.parsed < "1.21.11") {
        exclude("assets/minecraft/post_effect/**")
        exclude("assets/minecraft/shaders/core/**")
    } else {
        exclude("assets/minecraft/shaders/program/**")
        exclude("assets/minecraft/shaders/post/**")
    }

    val usesExplicitShaderLocations = sc.current.parsed >= "26.3"

    val resourceProperties = mapOf(
        "version" to project.version,
        "minecraft_version" to project.property("mod.mc_compat"),
        "loader_version" to project.property("deps.fabric_loader"),
        "fabric_version" to project.property("deps.fabric_api"),
        "java_version" to requiredJava,
        "client_mixins" to if (sc.current.parsed < "1.21.11") {
            "    \"CreativeInventoryScreenMixin\",\n" +
                    "    \"CreativeScreenHandlerAccessor\",\n" +
                    "    \"DrawContextItemProgressBarMixin\",\n" +
                    "    \"GuiItemOutlineMixin\",\n" +
                    "    \"HandledScreenAccessor\",\n" +
                    "    \"LegacyFontTextPlanMixin\",\n" +
                    "    \"LegacyFontRenderCharInvoker\",\n" +
                    "    \"LegacyTextRendererMixin\""
        } else {
            "    \"CreativeInventoryScreenMixin\",\n" +
                    "    \"CreativeScreenHandlerAccessor\",\n" +
                    "    \"DrawContextItemProgressBarMixin\",\n" +
                    "    \"GuiItemOutlineMixin\",\n" +
                    "    \"HandledScreenAccessor\",\n" +
                    "    \"HandledScreenMixin\",\n" +
                    "    \"TextRendererDrawerMixin\""
        },
        "text_mixins" to if (sc.current.parsed < "1.21.11") {
            "    \"LegacyStyleTextEffectAccessor\",\n" +
                    "    \"LegacyStyleTextEffectMixin\",\n" +
                    "    \"TextVisitFactoryMixin\""
        } else {
            "    \"StyleTextEffectAccessor\",\n" +
                    "    \"StyleTextEffectMixin\",\n" +
                    "    \"TextVisitFactoryMixin\""
        },
        "outline_mixins" to when {
            sc.current.parsed < "1.21.11" -> "    \"LegacyBlockEntityOutlineMixin\",\n" +
                    "    \"LegacyGuiItemRendererOutlineMixin\",\n" +
                    "    \"LegacyEntityOutlineColorMixin\",\n" +
                    "    \"LegacyHumanoidArmorLayerOutlineMixin\",\n" +
                    "    \"LegacyItemInHandLayerOutlineMixin\",\n" +
                    "    \"LegacyLevelRendererOutlineMixin\",\n" +
                    "    \"LegacyLivingEntityRendererOutlinePartsMixin\",\n" +
                    "    \"MinecraftClientOutlineMixin\""
            sc.current.parsed < "26.2" -> "    \"EntityRendererOutlineMixin\",\n" +
                    "    \"GuiItemOutlineModelResolverMixin\",\n" +
                    "    \"GuiItemOutlinePipelineMixin\",\n" +
                    "    \"GuiItemOutlineStateMixin\",\n" +
                    "    \"HumanoidArmorLayerOutlineMixin\",\n" +
                    "    \"ItemInHandLayerOutlineMixin\",\n" +
                    "    \"LivingEntityRendererOutlinePartsMixin\",\n" +
                    "    \"MinecraftClientOutlineMixin\",\n" +
                    "    \"OutlineVertexConsumerProviderMixin\",\n" +
                    "    \"RenderLayerAccessor\",\n" +
                    "    \"WorldRendererOutlineMixin\""
            else -> "    \"EntityRendererOutlineMixin\",\n" +
                    "    \"GuiItemOutlineModelResolverMixin\",\n" +
                    "    \"GuiItemOutlinePipelineMixin\",\n" +
                    "    \"GuiItemOutlineStateMixin\",\n" +
                    "    \"HumanoidArmorLayerOutlineMixin\",\n" +
                    "    \"ItemInHandLayerOutlineMixin\",\n" +
                    "    \"LivingEntityRendererOutlinePartsMixin\",\n" +
                    "    \"MinecraftClientOutlineMixin\""
        },
        "resource_pack_metadata" to when (sc.current.version) {
            "1.21.1" -> "\"pack_format\": 34"
            "1.21.11" -> "\"min_format\": 75,\n    \"max_format\": 75"
            "26.1" -> "\"min_format\": 84,\n    \"max_format\": 84"
            "26.2" -> "\"min_format\": 88,\n    \"max_format\": 88"
            "26.3" -> "\"min_format\": 97,\n    \"max_format\": 97"
            else -> error("No resource-pack metadata is defined for ${sc.current.version}")
        },
        "shader_preamble" to if (usesExplicitShaderLocations) {
            "#extension GL_ARB_separate_shader_objects : require"
        } else {
            ""
        },
        "shader_dynamic_transforms" to if (usesExplicitShaderLocations) {
            "#include <minecraft:dynamictransforms.glsl>"
        } else {
            "#moj_import <minecraft:dynamictransforms.glsl>"
        },
        "shader_vertex_color_input" to if (usesExplicitShaderLocations) {
            "layout(location = 0) in"
        } else {
            "in"
        },
        "shader_texture_input" to if (usesExplicitShaderLocations) {
            "layout(location = 1) in"
        } else {
            "in"
        },
        // position_tex_color writes texture coordinates at location 0 and colour at location 1.
        // GUI item-outline shaders use that vertex format rather than the position_color format
        // used by the world-outline mask.
        "shader_gui_texture_input" to if (usesExplicitShaderLocations) {
            "layout(location = 0) in"
        } else {
            "in"
        },
        "shader_gui_vertex_color_input" to if (usesExplicitShaderLocations) {
            "layout(location = 1) in"
        } else {
            "in"
        },
        "shader_post_input" to if (usesExplicitShaderLocations) {
            "layout(location = 0) in"
        } else {
            "in"
        },
        "shader_output" to if (usesExplicitShaderLocations) {
            "layout(location = 0) out"
        } else {
            "out"
        },
        // Mixin 0.8.7 advertises Java 22 as its highest compatibility level.
        // This controls Mixin language semantics, not the Java 25 bytecode target.
        "mixin_java_version" to 21,
    )

    inputs.properties(resourceProperties)
    filesMatching("fabric.mod.json") {
        expand(resourceProperties)
    }
    filesMatching("*.mixins.json") {
        expand(resourceProperties)
    }
    filesMatching("pack.mcmeta") {
        expand(mapOf("resource_pack_metadata" to resourceProperties.getValue("resource_pack_metadata")))
    }
    filesMatching("assets/**/*.fsh") {
        expand(resourceProperties)
    }
}

tasks.jar {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }
    repositories {
        mavenLocal()
    }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds this Minecraft target and collects its binary and sources JARs."
    from(loomx.modJar.map { it.archiveFile }, loomx.modSourcesJar.map { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs/${project.version}"))
    dependsOn(tasks.named("build"))
}

val sourceSets = extensions.getByType<SourceSetContainer>()

tasks.register<JavaExec>("runLogicTests") {
    group = "verification"
    description = "Runs Butterfly API's dependency-free logic regression suite."
    classpath = sourceSets.named("test").get().runtimeClasspath
    mainClass = "moth.butterflyapi.LogicTests"
    dependsOn(tasks.named("testClasses"))
}

// LogicTests is a plain Java runner so it can execute against Minecraft classes
// without introducing a second test framework. Keep Gradle's standard test task
// available for consumers that add JUnit tests, but do not fail on an empty suite.
tasks.withType<Test>().configureEach {
    failOnNoDiscoveredTests = false
}

tasks.named("check") {
    dependsOn(tasks.named("runLogicTests"))
}
