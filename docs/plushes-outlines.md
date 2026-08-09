# Plushes And Outlines

## Plush API

Butterfly plushes are vanilla block-model plush blocks with:

- a placed block
- a matching block item
- a shared plush block entity type
- a honk sound
- squish state when interacted with
- normal block-model lighting and cutout texture support

They do not use GeckoLib.

Create a plush entrypoint:

```java
package com.example;

import moth.butterflyapi.plush.PlushEntrypoint;
import moth.butterflyapi.plush.PlushRegistrar;
import net.minecraft.block.AbstractBlock;
import net.minecraft.sound.BlockSoundGroup;

public final class ExamplePlushes implements PlushEntrypoint {
    @Override
    public void registerPlushes(PlushRegistrar registrar) {
        registrar.plush(ExampleMod.MOD, "glow_moth_plush");

        registrar.plush(ExampleMod.MOD, "heavy_plush", plush -> plush
                .sound("glow_moth_plush_honk")
                .blockSettings(AbstractBlock.Settings.create()
                        .strength(0.5F)
                        .sounds(BlockSoundGroup.WOOL)
                        .nonOpaque()
                        .dynamicBounds())
                .itemSettings(ExampleMod.MOD.itemSettings("heavy_plush").maxCount(16))
                .onUse(blockEntity -> blockEntity.markDirty()));
    }
}
```

`fabric.mod.json`:

```json
{
  "entrypoints": {
    "butterfly_api:plush": [
      "com.example.ExamplePlushes"
    ]
  }
}
```

Recommended plush resources:

```text
assets/<modid>/blockstates/<plush>.json
assets/<modid>/models/block/<plush>.json
assets/<modid>/models/item/<plush>.json
assets/<modid>/items/<plush>.json
assets/<modid>/textures/block/<plush>.png
assets/<modid>/sounds.json
assets/<modid>/sounds/<plush>_honk.ogg
data/<modid>/loot_table/blocks/<plush>.json
data/<modid>/recipe/<plush>.json
```

Default plush sound id:

```text
<modid>:<plush_path>_honk
```

Plush blocks and items are automatically added to:

```java
ButterflyApiTags.PLUSH_BLOCKS
ButterflyApiTags.PLUSH_ITEMS
```

## Outlines

Server-synced outline for every viewer:

```java
import moth.butterflyapi.outline.OutlinePlacement;
import moth.butterflyapi.outline.OutlineStyle;
import moth.butterflyapi.outline.ServerOutlines;

OutlineStyle questStyle = OutlineStyle.builder("#FFFFFF")
        .thickness(2)
        .placement(OutlinePlacement.CENTERED)
        .blackBorder(false)
        .visibleThroughWalls(true)
        .matchModelLayer(true)
        .consistentThickness(true)
        .build();

ServerOutlines.set(targetEntity, questStyle);
ServerOutlines.clear(targetEntity);
```

Viewer-specific server outline:

```java
ServerOutlines.setFor(player, targetEntity, OutlineStyle.builder("#66CCFF")
        .thickness(3)
        .placement(OutlinePlacement.OUTSIDE)
        .build());

ServerOutlines.clearFor(player, targetEntity);
```

Block entity outlines:

```java
ServerOutlines.set(displayCaseBlockEntity, OutlineStyle.of("#FFFFFF"));
ServerOutlines.clear(displayCaseBlockEntity);
```

Client-only outlines:

```java
import moth.butterflyapi.client.outline.ClientOutlines;

ClientOutlines.set(entity, OutlineStyle.builder("#FFAA33").thickness(2).build());
ClientOutlines.clear(entity);
ClientOutlines.clearAll();
```

Style notes:

- Colors are RGB, such as `"#FFFFFF"` or `0xFFFFFF`.
- Thickness is measured in pixels and must be between `1` and `63`.
- `blackBorder(true)` supports thickness up to `22`.
- `visibleThroughWalls(true)` keeps outlines visible through depth.
- `matchModelLayer(true)` helps cutout or translucent masks follow their source layer.
- `consistentThickness(true)` keeps the requested width stable across distance.
- `placement(...)` can be `CENTERED`, `OUTSIDE`, or `INSIDE`.

Custom client renderers can contribute outline masks with `OutlineRenderers.capture(...)`. Block entities whose renderer only exists for outline-mask contribution can implement `OutlineOnlyBlockEntity`.
