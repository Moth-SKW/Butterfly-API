# Getting Started

Create one `ModContext` for your mod and reuse it anywhere you need ids, keyed settings, logging, or registration helpers.

```java
package com.example;

import moth.butterflyapi.ButterflyApi;
import moth.butterflyapi.mod.ModContext;

public final class ExampleContent {
    public static final ModContext MOD = ButterflyApi.mod("example_mod", "Example Mod");

    private ExampleContent() {
    }
}
```

`ModContext` gives you:

- `MOD.id("path")` for `example_mod:path`
- `MOD.blockKey("path")` and `MOD.itemKey("path")`
- `MOD.blockSettings("path")` and `MOD.itemSettings("path")`
- `MOD.registrar()`, `MOD.tabs()`, and `MOD.client()`
- shortcuts such as `MOD.item(...)`, `MOD.block(...)`, `MOD.sound(...)`, and `MOD.tabBuilder(...)`

## Common Registration

Items:

```java
import net.minecraft.item.Item;

public static final Item SILK_SCRAP = MOD.item(
        "silk_scrap",
        settings -> new Item(settings.maxCount(64))
);
```

Blocks with matching block items:

```java
import net.minecraft.block.Block;
import net.minecraft.sound.BlockSoundGroup;

public static final Block POLISHED_SILK = MOD.block(
        "polished_silk",
        settings -> new Block(settings
                .strength(1.5F, 3.0F)
                .sounds(BlockSoundGroup.WOOL))
);
```

Blocks without items:

```java
public static final Block INVISIBLE_TRIGGER = MOD.blockOnly(
        "invisible_trigger",
        new Block(MOD.blockSettings("invisible_trigger").noCollision().nonOpaque())
);
```

Sounds:

```java
import net.minecraft.sound.SoundEvent;

public static final SoundEvent SILK_CHIME = MOD.sound("silk_chime");
```

`assets/<modid>/sounds.json`:

```json
{
  "silk_chime": {
    "sounds": [
      "example_mod:silk_chime"
    ]
  }
}
```

## Other Registry Shortcuts

```java
MOD.entity("fluttering_mob", entityType);
MOD.blockEntityType("display_case", blockEntityType);
MOD.screenHandler("display_case", screenHandlerType);
MOD.statusEffect("silk_touchdown", statusEffect);
MOD.potion("silk_touchdown", potion);
MOD.recipeSerializer("looming", recipeSerializer);
MOD.recipeType("looming", recipeType);
MOD.particle("sparkle", particleType);
```

Enchantments and paintings are data-driven in 1.21.11. Put entries under `data/<modid>/enchantment` and `data/<modid>/painting_variant`.

## Client Registration

Use client helpers from a `ClientModInitializer`. Calling `MOD.client()` on a dedicated server throws.

```java
import net.fabricmc.api.ClientModInitializer;

public final class ExampleClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExampleMod.MOD.cutout(ExampleMod.POLISHED_SILK);
        ExampleMod.MOD.entityRenderer(EXAMPLE_ENTITY, ExampleEntityRenderer::new);
        ExampleMod.MOD.blockEntityRenderer(DISPLAY_CASE_BLOCK_ENTITY, ExampleDisplayCaseRenderer::new);
        ExampleMod.MOD.screen(EXAMPLE_SCREEN_HANDLER, ExampleScreen::new);
        ExampleMod.MOD.modelLayer(EXAMPLE_MODEL_LAYER, ExampleModel::getTexturedModelData);
        ExampleMod.MOD.particleFactory(EXAMPLE_PARTICLE, ExampleParticle.Factory::new);
    }
}
```
