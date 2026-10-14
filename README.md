# Butterfly API

Butterfly API is a lightweight developer library for **Fabric mods on Minecraft 1.21.1, 1.21.11, 26.1, 26.2, and 26.3**.

It simplifies common Minecraft modding tasks through reusable utilities, registration helpers, and customizable systems, reducing boilerplate without replacing standard Minecraft or Fabric functionality.

## Features

Butterfly API provides a collection of tools for mod developers:

- **Registration** — Simplified registration of items, blocks, entities, sounds, particles, and other content through `ModContext`.
- **Blocks and Multiblocks** — Configurable block builders, custom block items, multiblock structures, and automatic structure cleanup.
- **Items** — Item builders, combat attribute utilities, separate GUI and held models, and customizable progress bars.
- **Creative Tabs** — Custom item groups with categories, ordering, backgrounds, colors, and individually styled entries.
- **Plush API** — Interactive plush blocks with customizable sounds, squish behavior, and block-model rendering.
- **Outline API** — Customizable entity, block entity, and item outlines with configurable colors, thickness, visibility, and synchronization.
- **Text API** — Text construction, formatting, gradients, fonts, transformations, visual effects, and expandable tooltips.
- **Particle API** — Reloadable, JSON-defined client particle effects and configurable particle emitters.
- **Client Registration** — Simplified registration of renderers, screens, model layers, and particle factories.
- **Math Utilities** — Helpers for vectors, angles, bounding boxes, motion, interpolation, and sampling.
- **Bootstrap Utilities** — One-time initialization helpers to prevent duplicate registration and setup.

Butterfly API is designed to integrate with existing Minecraft and Fabric systems, allowing developers to use individual features without adopting an entirely new framework.

## Supported Versions

Butterfly API supports multiple Minecraft versions through separate, version-specific builds.

| Minecraft | Java | Mapping / Loom Model |
| --- | --- | --- |
| 1.21.1 | 21 | Official Mojang mappings with remapped Fabric Loom |
| 1.21.11 | 21 | Official Mojang mappings with remapped Fabric Loom |
| 26.1 | 25 | Unobfuscated Minecraft with standard Fabric Loom dependencies |
| 26.2 | 25 | Unobfuscated Minecraft with standard Fabric Loom dependencies |
| 26.3 | 25 | Unobfuscated Minecraft with standard Fabric Loom dependencies |

Each Minecraft version requires its corresponding Butterfly API JAR. Builds are not interchangeable between Minecraft versions.

### Requirements

- A supported Minecraft version
- Fabric Loader
- Fabric API
- Java 21 or Java 25, depending on the Minecraft version

## Getting Started

Butterfly API uses `ModContext` as its primary entry point for registration and other common utilities.

Create a context for your mod:

```java
import moth.butterflyapi.ButterflyApi;
import moth.butterflyapi.mod.ModContext;

public static final ModContext MOD =
        ButterflyApi.mod("example_mod", "Example Mod");
```

The context provides access to registration utilities, namespaced identifiers, creative tabs, and other API functionality.

For installation, dependencies, implementation examples, and detailed API references, see the wiki.

## Documentation

The **[Butterfly API Wiki](https://github.com/Moth-SKW/Butterfly-API/wiki)** contains setup instructions, examples, and references for the available API systems.

Documentation includes:

- Installation and quick-start guides
- Registration and `ModContext`
- Items, blocks, and multiblocks
- Creative tabs and styling
- Plush blocks and resources
- Entity, block entity, and item outlines
- Text effects, fonts, and expandable tooltips
- Particle definitions and emitters
- Client registration and utilities
- Resource conventions and troubleshooting

The wiki is the primary reference for developing mods with Butterfly API.

## Building and Testing

Butterfly API uses Gradle and Stonecutter to maintain separate builds for its supported Minecraft versions.

The dependency-free regression suite can be executed across all targets using:

```powershell
.\gradlew.bat :1.21.1:runLogicTests :1.21.11:runLogicTests :26.1:runLogicTests :26.2:runLogicTests :26.3:runLogicTests
```

The `check` task also runs the regression suite.

Version-specific builds allow Butterfly API to accommodate changes in Minecraft's internal systems while maintaining a consistent developer-facing API wherever possible.

## License

Butterfly API is licensed under the **MIT License**.

See [LICENSE](LICENSE) for the full license text.