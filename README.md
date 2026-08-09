# Butterfly API

Butterfly API is a lightweight Fabric helper mod for Minecraft 1.21.11. It focuses on small, reusable systems for registration, creative tabs, plush blocks, outlines, item utilities, multiblocks, math helpers, and bootstrap code.

The project uses Java 21, Fabric Loader, Fabric API, and no GeckoLib dependency.

## Requirements

- Minecraft 1.21.11
- Java 21 or newer
- Fabric Loader 0.18.1 or newer
- Fabric API 0.141.5+1.21.11 or newer

## Install

Published builds:

```gradle
repositories {
    maven { url = "https://api.modrinth.com/maven" }
}

dependencies {
    modImplementation "maven.modrinth:butterfly-api:1.2.1"
}
```

Local development builds:

```powershell
.\gradlew.bat publishToMavenLocal
```

```gradle
repositories {
    mavenLocal()
}

dependencies {
    modImplementation "moth.butterflyapi:butterfly-api:1.2.1"
}
```

`fabric.mod.json`:

```json
{
  "depends": {
    "butterfly_api": ">=1.2.1"
  }
}
```

## Build

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
```

## Main API Areas

| Area | Main classes | Use |
| --- | --- | --- |
| Mod context | `ButterflyApi`, `ModContext` | Namespaced ids, keyed settings, logging, shortcuts |
| Registration | `Registrar`, `BlockBuilder`, `BlockItemFactory` | Register items, blocks, block items, sounds, entities, screens, particles |
| Items | `ItemUtils`, `ItemModelDefinitions`, `ItemProgressBars` | Displayed attack speed, split GUI/held models, custom item bars |
| Blocks | `BlockBuilder`, `AbstractMultiblockBlock`, `MultiblockShape` | Complex block registration and common multiblock structures |
| Creative tabs | `Tabs`, `TabBuilder`, `TabCategory` | Normal or styled creative tabs with categories |
| Plushes | `PlushEntrypoint`, `PlushRegistrar`, `PlushDefinition` | Squishable vanilla-model plush blocks with sounds |
| Outlines | `ServerOutlines`, `ClientOutlines`, `OutlineStyle` | Colored outlines for entities and block entities |
| Client | `ClientRegistrar` | Render layers, renderers, screens, model layers, particles |
| Math | `Angles`, `Vecs`, `Boxes`, `Motion`, `Sampling`, `Scalars` | Vector, angle, box, motion, sampling, scalar helpers |
| Bootstrap | `Bootstrap`, `RunOnce` | Idempotent initialization |

## Quick Start

```java
package com.example;

import moth.butterflyapi.ButterflyApi;
import moth.butterflyapi.mod.ModContext;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;

public final class ExampleMod implements ModInitializer {
    public static final ModContext MOD = ButterflyApi.mod("example_mod", "Example Mod");

    public static final Item SILK_SCRAP = MOD.item(
            "silk_scrap",
            settings -> new Item(settings.maxCount(64))
    );

    public static final Block POLISHED_SILK = MOD.block(
            "polished_silk",
            settings -> new Block(settings.strength(1.5F).sounds(BlockSoundGroup.WOOL))
    );

    public static final ItemGroup TAB = MOD.tabBuilder("main")
            .icon(SILK_SCRAP)
            .category("blocks", "Blocks", category -> category.add(POLISHED_SILK))
            .category("items", "Items", category -> category.add(SILK_SCRAP))
            .categoryOrder("blocks", "items")
            .build();

    @Override
    public void onInitialize() {
        MOD.logger().info("Initialized {}", MOD.modName());
    }
}
```

## Documentation

- [Getting Started](docs/getting-started.md)
- [Items](docs/items.md)
- [Blocks And Multiblocks](docs/blocks.md)
- [Creative Tabs](docs/creative-tabs.md)
- [Plushes And Outlines](docs/plushes-outlines.md)
- [Math And Bootstrap](docs/math-bootstrap.md)
- [Troubleshooting](docs/troubleshooting.md)

## Notes For 1.21.11

- Use `MOD.blockSettings(path)` and `MOD.itemSettings(path)` when constructing blocks/items. Minecraft expects registry keys on settings before construction.
- Block items need both `assets/<modid>/items/<id>.json` and `assets/<modid>/models/item/<id>.json`.
- Enchantments and paintings are data-driven. Butterfly keeps no-op convenience methods for old setup code, but entries should live in data files.

## Project Layout

```text
src/main/java/moth/butterflyapi
  block/           Block builders and multiblock helpers
  bootstrap/       Idempotent setup helpers
  client/          Client helpers, item bars, plush rendering, outlines
  item/            Item model, attribute, and custom bar helpers
  itemgroup/       Creative-tab builders and styling
  math/            Angle, vector, box, scalar, sampling, and motion helpers
  mod/             ModContext
  outline/         Public outline style and server API
  plush/           Plush registration, blocks, items, and block entities
  registry/        Common registration helpers

src/main/resources
  assets/          API shaders and Minecraft shader overrides
```
