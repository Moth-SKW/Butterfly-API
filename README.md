# Butterfly API

Butterfly API is a Fabric API helper mod for Minecraft 1.21.11. It provides small, focused helpers for registration, creative tabs, plush blocks, custom outlines, math, and bootstrapping, while also shipping a little bundled content for players.

The current project is intentionally lightweight: Java 21, Fabric Loader, Fabric API, and no GeckoLib dependency.

## Requirements

- Minecraft 1.21.11
- Java 21 or newer
- Fabric Loader 0.18.1 or newer
- Fabric API 0.141.5+1.21.11 or newer

## Using The Mod

If you are consuming a published build from Modrinth Maven:

```gradle
repositories {
    maven { url = "https://api.modrinth.com/maven" }
}

dependencies {
    modImplementation "maven.modrinth:butterfly-api:1.2.0"
}
```

If you are using a local development build, publish Butterfly API to your local Maven repository first:

```powershell
.\gradlew.bat publishToMavenLocal
```

Then depend on the local coordinate:

```gradle
repositories {
    mavenLocal()
}

dependencies {
    modImplementation "moth.butterflyapi:butterfly-api:1.2.0"
}
```

Add the dependency to your `fabric.mod.json`:

```json
{
  "depends": {
    "butterfly_api": ">=1.2.0"
  }
}
```

## Building Butterfly API

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
```

The build uses the Java 21 toolchain configured in `gradle.properties`.

## What It Provides

| Area | Main classes | Purpose |
| --- | --- | --- |
| Mod context | `ButterflyApi`, `ModContext` | Namespaced ids, keyed settings, logger, registration helpers |
| Registry helpers | `Registrar`, `BlockItemFactory` | Register common content with less repeated boilerplate |
| Creative tabs | `Tabs`, `TabBuilder`, `TabCategory`, `TabEntryStyle` | Build normal or styled creative tabs with categories |
| Plushes | `PlushEntrypoint`, `PlushRegistrar`, `PlushDefinition` | Register squishable block-model plushes with sounds |
| Outlines | `ServerOutlines`, `ClientOutlines`, `OutlineStyle` | Sync and render colored outlines for entities and block entities |
| Client helpers | `ClientRegistrar` | Render layers, renderers, screens, model layers, particles |
| Math helpers | `Angles`, `Vecs`, `Boxes`, `Motion`, `Sampling`, `Scalars` | Common vector, angle, box, motion, and scalar utilities |
| Bootstrap helpers | `Bootstrap`, `RunOnce` | Idempotent initialization helpers |
| Tags | `ButterflyApiTags` | Built-in plush item and block tags |

## ModContext

Create one `ModContext` for your mod and reuse it anywhere you need ids or registration helpers.

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
- shortcut methods such as `MOD.item(...)`, `MOD.block(...)`, `MOD.sound(...)`, and `MOD.tabBuilder(...)`

Minecraft 1.21.11 expects many block and item settings to already contain their registry key before construction. Prefer `MOD.blockSettings(path)` and `MOD.itemSettings(path)` when constructing your own blocks and items.

## Registration Examples

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

Blocks with custom block-item settings:

```java
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

public static final Block DISPLAY_CASE = new Block(MOD.blockSettings("display_case").strength(1.0F));

static {
    MOD.block(
            "display_case",
            DISPLAY_CASE,
            block -> new BlockItem(block, MOD.itemSettings("display_case").maxCount(16))
    );
}
```

Blocks without items:

```java
public static final Block INVISIBLE_TRIGGER = MOD.blockOnly(
        "invisible_trigger",
        new Block(MOD.blockSettings("invisible_trigger").noCollision().nonOpaque())
);
```

Tall doors:

```java
import net.minecraft.block.DoorBlock;
import net.minecraft.block.BlockSetType;

public static final DoorBlock SILK_DOOR = MOD.door(
        "silk_door",
        new DoorBlock(BlockSetType.OAK, MOD.blockSettings("silk_door").strength(2.0F))
);
```

Sounds:

```java
import net.minecraft.sound.SoundEvent;

public static final SoundEvent SILK_CHIME = MOD.sound("silk_chime");
```

Add a matching entry to `assets/<modid>/sounds.json`:

```json
{
  "silk_chime": {
    "sounds": [
      "example_mod:silk_chime"
    ]
  }
}
```

Other supported registry shortcuts include:

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

Enchantments and paintings are data-driven in 1.21.11. Butterfly API keeps `MOD.enchantment(...)` and `MOD.painting(...)` as convenience no-ops so old setup code can keep a named object, but the actual entries should live under `data/<modid>/enchantment` and `data/<modid>/painting_variant`.

## Creative Tabs

A simple tab:

```java
import net.minecraft.item.ItemGroup;

public static final ItemGroup EXAMPLE_TAB = MOD.tab(
        "main",
        SILK_SCRAP,
        SILK_SCRAP,
        POLISHED_SILK
);
```

The default translation key is:

```text
itemGroup.<modid>.<path>
```

For the example above, add this to `assets/example_mod/lang/en_us.json`:

```json
{
  "itemGroup.example_mod.main": "Example Mod"
}
```

A categorized custom tab:

```java
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;

public static final ItemGroup EXAMPLE_TAB = MOD.tabBuilder("main")
        .icon(EXAMPLEST_ITEM_EVER)
        .translationKey("itemGroup.example_mod.main")
        .backgroundTexture(MOD.id("textures/gui/example_tab.png"))
        .slotBackgroundColor("#222222")
        .betweenSlotsColor("#111111")
        .tabNameColor("#FFE8F5")
        .category("materials", Text.translatable("itemGroup.example_mod.category.materials"), category -> category
                .alignLeft()
                .backgroundColor("#35303A")
                .borderColor("#665578")
                .textColor("#FFE8F5")
                .add(EXAMPLEST_ITEM_EVER)
                .entry(EXAMPLEST_ITEM_EVER_BUT_BETTER, entry -> entry.slotColor("#463B51")))
        .category("tools", Text.translatable("itemGroup.example_mod.category.tools"), category -> category
                .backgroundColor("#2F3740")
                .add(EXAMPLE_TOOL))
        .categoryOrder("materials", "tools")
        .searchOnly(EXAMPLEST_ITEM_EVER_BUT_HIDDEN)
        .build();
```

Category names can be literals or translatable text. If a category name appears as a raw translation key in game, add that key to your language file:

```json
{
  "itemGroup.example_mod.category.materials": "Materials",
  "itemGroup.example_mod.category.tools": "Tools"
}
```

You can also add entries to vanilla or existing tabs:

```java
import net.minecraft.item.ItemGroups;

MOD.addTo(ItemGroups.INGREDIENTS, EXAMPLEST_ITEM_EVER);
MOD.addTo(ItemGroups.BUILDING_BLOCKS, EXAMPLEST_ITEM_EVER_BUT_BETTER);
```

## Client Registration

Use the client helpers from a `ClientModInitializer` only. Calling `MOD.client()` on a dedicated server will throw.

```java
import net.fabricmc.api.ClientModInitializer;

public final class ExampleClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MOD.cutout(EXAMPLEST_ITEM_EVER_BUT_BETTER);
        MOD.translucent(DISPLAY_CASE);
        MOD.entityRenderer(EXAMPLEST_ENTITY, ExampleEntityRenderer::new);
        MOD.blockEntityRenderer(DISPLAY_CASE_BLOCK_ENTITY, ExampleDisplayCaseRenderer::new);
        MOD.screen(EXAMPLEST_SCREEN_HANDLER, ExampleScreen::new);
        MOD.modelLayer(EXAMPLEST_MODEL_LAYER, ExampleModel::getTexturedModelData);
        MOD.particleFactory(EXAMPLEST_PARTICLE, ExampleParticle.Factory::new);
    }
}
```

`cutoutMipped(...)` currently maps to the same 1.21.11 block render layer as `cutout(...)`.

## Plush API

Butterfly plushes are block-model plush blocks with:

- a placed block
- a matching block item
- a shared plush block entity type
- a honk sound
- squish state when interacted with
- normal block-model lighting and cutout texture support

They do not use GeckoLib. Plush models should be normal vanilla block models.

Create a plush entrypoint:

```java
package com.example;

import moth.butterflyapi.ButterflyApi;
import moth.butterflyapi.mod.ModContext;
import moth.butterflyapi.plush.PlushEntrypoint;
import moth.butterflyapi.plush.PlushRegistrar;
import net.minecraft.block.AbstractBlock;
import net.minecraft.sound.BlockSoundGroup;

public final class ExamplePlushes implements PlushEntrypoint {
    private static final ModContext MOD = ButterflyApi.mod("example_mod", "Example Mod");

    @Override
    public void registerPlushes(PlushRegistrar registrar) {
        registrar.plush(MOD, "glow_moth_plush");

        registrar.plush(MOD, "heavy_plush", plush -> plush
                .sound("glow_moth_plush_honk")
                .blockSettings(AbstractBlock.Settings.create()
                        .strength(0.5F)
                        .sounds(BlockSoundGroup.WOOL)
                        .nonOpaque()
                        .dynamicBounds())
                .itemSettings(MOD.itemSettings("heavy_plush").maxCount(16))
                .onUse(blockEntity -> blockEntity.markDirty()));
    }
}
```

Register the entrypoint in your `fabric.mod.json`:

```json
{
  "entrypoints": {
    "butterfly_api:plush": [
      "com.example.ExamplePlushes"
    ]
  }
}
```

The default plush sound id is `<modid>:<plush_path>_honk`. For `example_mod:glow_moth_plush`, add this to `assets/example_mod/sounds.json`:

```json
{
  "glow_moth_plush_honk": {
    "sounds": [
      "example_mod:glow_moth_plush_honk"
    ]
  }
}
```

Then place the sound at:

```text
assets/example_mod/sounds/glow_moth_plush_honk.ogg
```

Recommended plush resource checklist:

```text
assets/<modid>/blockstates/<plush>.json
assets/<modid>/models/block/<plush>.json
assets/<modid>/models/item/<plush>.json
assets/<modid>/items/<plush>.json
assets/<modid>/textures/block/<plush>.png
assets/<modid>/sounds.json
assets/<modid>/sounds/<plush>_honk.ogg
data/<modid>/loot_tables/blocks/<plush>.json
data/<modid>/recipes/<plush>.json
```

Minimal blockstate:

```json
{
  "variants": {
    "facing=north": { "model": "example_mod:block/glow_moth_plush" },
    "facing=east": { "model": "example_mod:block/glow_moth_plush", "y": 90 },
    "facing=south": { "model": "example_mod:block/glow_moth_plush", "y": 180 },
    "facing=west": { "model": "example_mod:block/glow_moth_plush", "y": 270 }
  }
}
```

Minimal item model files for Minecraft 1.21.11:

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "example_mod:item/glow_moth_plush"
  }
}
```

```json
{
  "parent": "example_mod:block/glow_moth_plush"
}
```

Plush blocks and items are automatically added to:

```java
ButterflyApiTags.PLUSH_BLOCKS
ButterflyApiTags.PLUSH_ITEMS
```

Datapack paths:

```text
data/butterfly_api/tags/blocks/plushes.json
data/butterfly_api/tags/items/plushes.json
```

## Outlines

Butterfly API can outline entities and block entities from either the server or the local client.

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

Block entity outlines use the same methods:

```java
ServerOutlines.set(displayCaseBlockEntity, OutlineStyle.of("#FFFFFF"));
ServerOutlines.clear(displayCaseBlockEntity);
```

Local client-only outlines:

```java
import moth.butterflyapi.client.outline.ClientOutlines;

ClientOutlines.set(entity, OutlineStyle.builder("#FFAA33").thickness(2).build());
ClientOutlines.clear(entity);
ClientOutlines.clearAll();
```

Style notes:

- Colors are RGB only, such as `"#FFFFFF"` or `0xFFFFFF`.
- Thickness is measured in pixels and must be between `1` and `63`.
- `blackBorder(true)` supports thickness up to `22`.
- `visibleThroughWalls(true)` keeps outlines visible through depth.
- `matchModelLayer(true)` helps mask cutout or translucent render layers correctly.
- `consistentThickness(true)` keeps the requested width stable across distance.
- `placement(...)` can be `CENTERED`, `OUTSIDE`, or `INSIDE`.

For custom renderers that need to contribute their own outline mask, use `OutlineRenderers.capture(...)` on the client. Block entities whose renderer only exists for outline-mask contribution can implement `OutlineOnlyBlockEntity` so the normal block model is still rendered by the chunk renderer.

## Math Helpers

Angles:

```java
double yawDelta = Angles.deltaDegrees(entity.getYaw(), targetYaw);
Vec3d direction = Angles.direction(entity.getYaw(), entity.getPitch());
YawPitch look = Angles.lookAt(entity.getEyePos(), target.getEyePos());
```

Vectors:

```java
Vec3d towardTarget = Vecs.direction(entity.getPos(), target.getPos(), Vec3d.ZERO);
Vec3d capped = Vecs.limitLength(entity.getVelocity(), 0.5D);
Vec3d reflected = Vecs.reflect(incoming, surfaceNormal);
```

Boxes:

```java
Box searchArea = Boxes.around(entity.getPos(), 8.0D);
Box beamArea = Boxes.between(start, end, 0.25D);
Vec3d center = Boxes.center(searchArea);
```

Motion:

```java
Motion.pushFrom(target, source, 0.8D, 0.2D);
Motion.pullToward(projectile, target.getPos(), 0.4D, 0.0D);
```

Sampling:

```java
for (Vec3d point : Sampling.circle(center, new Vec3d(0.0D, 1.0D, 0.0D), 2.0D, 16)) {
    world.addParticle(particle, point.x, point.y, point.z, 0.0D, 0.0D, 0.0D);
}
```

Scalars:

```java
double progress = Scalars.clamp01(Scalars.inverseLerp(value, min, max));
double mapped = Scalars.mapClamped(distance, 0.0D, 32.0D, 1.0D, 0.0D);
double eased = Scalars.approach(current, target, step);
```

## Bootstrap Helpers

`Bootstrap` and `RunOnce` help keep initialization idempotent.

```java
import moth.butterflyapi.bootstrap.Bootstrap;

public final class ExampleBootstrap {
    private static final Bootstrap BOOTSTRAP = Bootstrap.create();

    public static void init() {
        BOOTSTRAP.run(
                ExampleItems::init,
                ExampleBlocks::init,
                ExampleSounds::init
        );
    }
}
```

## Bundled Content

Butterfly API includes:

- Plushes: Syntax Plush, Hex Plush, Hex Maid Plush, Moth Plush, Chicken Plush, Capozi Plush, and Debug Plush
- Big Red Button block with click advancements
- Matchstick item and matchstick fire blocks
- Connection enchantment
- Hi painting variant
- Butterfly API creative tab
- Plush recipes, loot tables, tags, and the collect-all-plushes advancement

## Resource And Data Conventions

Common translation keys:

```json
{
  "item.example_mod.silk_scrap": "Silk Scrap",
  "block.example_mod.polished_silk": "Polished Silk",
  "itemGroup.example_mod.main": "Example Mod"
}
```

For data-driven registry entries in 1.21.11, prefer data files:

```text
data/<modid>/enchantment/<id>.json
data/<modid>/painting_variant/<id>.json
data/minecraft/tags/painting_variant/placeable.json
```

For block items in 1.21.11, remember both item model layers:

```text
assets/<modid>/items/<id>.json
assets/<modid>/models/item/<id>.json
```

## Complete Mini Implementation

`fabric.mod.json`:

```json
{
  "entrypoints": {
    "main": [
      "com.example.ExampleMod"
    ],
    "client": [
      "com.example.ExampleClient"
    ],
    "butterfly_api:plush": [
      "com.example.ExamplePlushes"
    ]
  },
  "depends": {
    "butterfly_api": ">=1.2.0"
  }
}
```

Main initializer:

```java
package com.example;

import moth.butterflyapi.ButterflyApi;
import moth.butterflyapi.bootstrap.Bootstrap;
import moth.butterflyapi.mod.ModContext;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;

public final class ExampleMod implements ModInitializer {
    public static final ModContext MOD = ButterflyApi.mod("example_mod", "Example Mod");
    private static final Bootstrap BOOTSTRAP = Bootstrap.create();

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
            .translationKey("itemGroup.example_mod.main")
            .category("blocks", "Blocks", category -> category.add(POLISHED_SILK))
            .category("items", "Items", category -> category.add(SILK_SCRAP))
            .categoryOrder("blocks", "items")
            .build();

    @Override
    public void onInitialize() {
        BOOTSTRAP.run(() -> MOD.logger().info("Initialized {}", MOD.modName()));
    }
}
```

Client initializer:

```java
package com.example;

import net.fabricmc.api.ClientModInitializer;

public final class ExampleClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExampleMod.MOD.cutout(ExampleMod.POLISHED_SILK);
    }
}
```

Plush entrypoint:

```java
package com.example;

import moth.butterflyapi.plush.PlushEntrypoint;
import moth.butterflyapi.plush.PlushRegistrar;

public final class ExamplePlushes implements PlushEntrypoint {
    @Override
    public void registerPlushes(PlushRegistrar registrar) {
        registrar.plush(ExampleMod.MOD, "glow_moth_plush");
    }
}
```

## Troubleshooting

- A creative tab name shows as a raw key: add `itemGroup.<modid>.<path>` or your custom category translation key to `assets/<modid>/lang/en_us.json`.
- A block or item crashes during construction: make sure its settings came from `MOD.blockSettings(path)` or `MOD.itemSettings(path)`, or that you attached the correct registry key before construction.
- A plush has sound but no visible model: check `blockstates`, `models/block`, `models/item`, `items`, and texture paths for the same plush id.
- A plush has no sound: check `sounds.json`, the `.ogg` path, and the `PlushDefinition.sound(...)` id.
- A plush cannot register: plush entrypoints must run before Butterfly plush bootstrap, and every plush id must be unique.
- Outlines are not visible on a client-only target: use `ClientOutlines` from client code, or use `ServerOutlines` on the logical server for synced outlines.
- Cutout or translucent outline masks look wrong in a custom renderer: use `matchModelLayer(true)` on the style and make sure the renderer contributes an outline mask through the correct render layer.

## Project Layout

```text
src/main/java/moth/butterflyapi
  bootstrap/       Idempotent setup helpers
  client/          Client helpers, plush rendering, outline rendering
  content/         Bundled Butterfly API content
  itemgroup/       Creative-tab builders and styling
  math/            Angle, vector, box, scalar, sampling, and motion helpers
  mod/             ModContext
  outline/         Public outline style and server API
  plush/           Plush registration, blocks, items, and block entities
  registry/        Common registration helpers

src/main/resources
  assets/          Models, textures, sounds, shaders, and lang
  data/            Recipes, loot tables, advancements, tags, enchantments, paintings
```
