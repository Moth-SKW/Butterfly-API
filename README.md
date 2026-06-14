# Butterfly API

Butterfly API is my Fabric 1.20.1 helper/API mod. It is mostly here so I do not have to keep rewriting the same registration helpers, item group setup, math utilities, and plush code every time I make another mod.

It also comes with a few actual things in-game, because I can and I did.

The short version: use this if you want some cleaner helper methods for Fabric modding, or if you want to add plushes that use Butterfly API's shared plush system.

## REQUIREMENTS

- Minecraft 1.20.1
- Java 17
- Fabric Loader 0.16.10 or newer
- Fabric API 0.92.6+1.20.1 or newer
- GeckoLib 4.4.9 or newer

## BUILD

Build the mod jar:

```powershell
.\gradlew.bat build
```

The finished jar ends up in `build/libs/`.

### Using the Modrinth version

Butterfly API is published on Modrinth under the slug `butterfly-api`. If you want to use it in another Fabric mod project, add Modrinth Maven and depend on version `1.0.0`.

```gradle
repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = "https://api.modrinth.com/maven"
            }
        }
        filter {
            includeGroup "maven.modrinth"
        }
    }
}

dependencies {
    modImplementation "maven.modrinth:butterfly-api:1.0.0"
}
```

If your mod needs Butterfly API to be installed alongside it, add it to your `fabric.mod.json` too:

```json
{
  "depends": {
    "butterfly_api": ">=1.0.0"
  }
}
```

Small warning because this one is easy to forget: Modrinth Maven does not bring in transitive dependencies. Keep Fabric API, GeckoLib, and anything else your dev environment needs declared separately :D

### Using a local build

If you are working from source, publish it to your local Maven cache:

```powershell
.\gradlew.bat publishToMavenLocal
```

Then another local Gradle project can use it like this:

```gradle
repositories {
    mavenLocal()
    maven { url = "https://maven.fabricmc.net/" }
    maven {
        name = "GeckoLib"
        url = "https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/"
    }
}

dependencies {
    modImplementation "moth.butterflyapi:Butterfly API:1.0.0"
}
```

## QUICK START

Most of the API starts with a `ModContext`. Make one for your mod and then use it for ids, logging, registration, tabs, client helpers, and plush builders.

```java
public final class ExampleMod implements ModInitializer {
    public static final ModContext MOD = ButterflyApi.mod("example_mod", "Example Mod");

    public static final Item EXAMPLE_ITEM = MOD.item("example_item", new Item(new Item.Settings()));
    public static final Block EXAMPLE_BLOCK = MOD.block(
            "example_block",
            new Block(AbstractBlock.Settings.create().strength(1.5F))
    );

    @Override
    public void onInitialize() {
        MOD.addTo(ItemGroups.INGREDIENTS, EXAMPLE_ITEM);
        MOD.logger().info("Loaded {}", MOD.modName());
    }
}
```

Useful context bits:

- `MOD.id("path")` makes `example_mod:path`.
- `MOD.modId()` gives the mod id.
- `MOD.modName()` gives the display name.
- `MOD.logger()` gives an SLF4J logger for the mod.
- `MOD.registrar()`, `MOD.tabs()`, and `MOD.client()` expose the helper classes directly.

## USE CASES

This is the "what am I actually here for?" map. The code has the tiny details, but this should help you find the right part without reading every file first.

| If you want to... | Start with... | Good for... |
| --- | --- | --- |
| Register normal mod stuff | `ModContext` / `Registrar` | Items, blocks, sounds, enchantments, entities, particles, paintings, recipes, and similar |
| Make or fill creative tabs | `TabBuilder` / `MOD.tabBuilder(...)` | Searchable tabs, categories, custom slot surfaces, per-entry styling, and adding entries to vanilla tabs |
| Do client-only setup | `ClientRegistrar` / `MOD.client()` | Render layers, entity renderers, block entity renderers, screens, model predicates, particles |
| Do gameplay math | `moth.butterflyapi.math` | Look vectors, particle circles, spread patterns, hitboxes, knockback, pushes, pulls, and value mapping |
| Add placeable plushes | `PlushEntrypoint` / `PlushRegistrar` | Simple plushes, GeckoLib plushes, honk sounds, squish behavior, shared plush block entity setup |
| Target all plushes at once | `ButterflyApiTags` / `#butterfly_api:plushes` | Code checks, datapack rules, loot logic, recipes, or anything that should care about every plush |
| Stop init code from running twice | `Bootstrap` / `RunOnce` | Content bootstraps, lazy setup, and "please only do this once" code |
| Use the built-in content | `butterfly_api` ids | Plushes, Matchstick, Big Red Button, Connection enchantment, creative tab content |

## REGISTRY

This part saves from typing `Registry.register(...)` over and over. Everything registers under your `ModContext` mod id.

```java
public static final Item GEM = MOD.item("gem", new Item(new Item.Settings()));

public static final Block STONE_TILE = MOD.block(
        "stone_tile",
        new Block(AbstractBlock.Settings.create().strength(2.0F))
);

public static final Block LOGIC_BLOCK = MOD.block(
        "logic_block",
        new Block(AbstractBlock.Settings.create()),
        registeredBlock -> new BlockItem(registeredBlock, new Item.Settings().maxCount(16))
);

public static final Block INVISIBLE_TECH_BLOCK = MOD.blockOnly(
        "invisible_tech_block",
        new Block(AbstractBlock.Settings.create().noCollision())
);

public static final SoundEvent CHIME = MOD.sound("chime");
```

The registry helpers cover:

- `register(registry, path, value)` for any registry.
- `item(path, item)`.
- `block(path, block)` with a normal `BlockItem`.
- `block(path, block, itemSettings)` for a block with a custom item.
- `block(path, block, BlockItemFactory)` when you want full control.
- `blockOnly(path, block)` for blocks without items.
- `door(path, block)` for `TallBlockItem`.
- `sound(path)` and `sound(path, soundEvent)`.
- `enchantment`, `entity`, `blockEntityType`, `screenHandler`, `statusEffect`, `potion`, `recipeSerializer`, `recipeType`, `particle`, and `painting`.

## ITEM TABS

The `Tabs` helper still supports the original `MOD.tab(...)` overloads, but the builder is where the more advanced creative-tab setup lives.

Start a builder through either `MOD.tabBuilder("path")` or `MOD.tabs().builder("path")`:

```java
public static final ItemGroup EXAMPLE_TAB = MOD.tabBuilder("example")
        .icon(EXAMPLE_ITEM)
        .translationKey("itemGroup.example_mod.example")
        .add(EXAMPLE_ITEM, EXAMPLE_BLOCK)
        .build();
```

Tabs created through the builder have a search bar by default. Search results stay scoped to the selected tab instead of turning into the global vanilla search tab.

The original helpers are still available:

```java
public static final ItemGroup SIMPLE_TAB = MOD.tab(
        "simple",
        EXAMPLE_ITEM,
        EXAMPLE_ITEM,
        EXAMPLE_BLOCK
);

@Override
public void onInitialize() {
    MOD.addTo(ItemGroups.BUILDING_BLOCKS, EXAMPLE_BLOCK);
}
```

Useful tab entry points:

- `MOD.tabBuilder("example")` starts a `TabBuilder`.
- `MOD.tabs().builder("example")` does the same thing through the `Tabs` helper.
- `MOD.tabKey("example")` makes the item-group registry key.
- `MOD.tabTranslationKey("example")` returns `itemGroup.<modid>.example`.
- `MOD.tab(...)` keeps the original simple tab overloads.
- `MOD.addTo(groupKey, entries...)` adds items or blocks to an existing tab.

### Basic builder options

```java
public static final ItemGroup EXAMPLE_TAB = MOD.tabBuilder("example")
        .icon(EXAMPLE_ITEM)
        .displayName(Text.literal("Example"))
        .entries(entries -> {
            entries.add(EXAMPLE_ITEM);
            entries.add(EXAMPLE_BLOCK);
        })
        .noScrollbar()
        .noRenderedName()
        .build();
```

The builder supports:

- `icon(ItemConvertible)` or `icon(Supplier<ItemStack>)`.
- `displayName(Text)` or `translationKey(String)`.
- `entries(Consumer<ItemGroup.Entries>)`.
- `add(ItemConvertible...)`.
- `searchBar()` when you want to state the searchable behavior explicitly.
- `special()`.
- `noScrollbar()`.
- `noRenderedName()`.
- `backgroundSuffix(String)` for a vanilla creative background suffix.
- `backgroundTexture(Identifier)` for a full namespaced background texture.
- `build()` to register and return the finished `ItemGroup`.

### Custom creative-tab appearance

A tab can replace the creative background and customize the slot grid:

```java
public static final ItemGroup EXAMPLE_TAB = MOD.tabBuilder("example")
        .icon(EXAMPLE_ITEM)
        .backgroundTexture(MOD.id(
                "textures/gui/container/creative_inventory/tab_example.png"
        ))
        .slotBackgroundTexture(MOD.id("textures/gui/example_slot.png"))
        .betweenSlotsColor("#30283D")
        .tabNameColor("#F4D7FF")
        .add(EXAMPLE_ITEM, EXAMPLE_BLOCK)
        .build();
```

Custom appearance helpers include:

- `customOverlay(true)` or `customAppearance()` to use Butterfly API's custom grid rendering.
- `customOverlay(false)` or `vanillaAppearance()` to keep the vanilla tab appearance.
- `slotBackgroundTexture(Identifier)` or `slotBackgroundColor("#RRGGBB")`.
- `betweenSlotsTexture(Identifier)` or `betweenSlotsColor("#RRGGBB")`.
- `tabNameColor("#RRGGBB")` and `defaultTabNameColor()`.

A vanilla-looking tab can still receive a tint:

```java
public static final ItemGroup TINTED_TAB = MOD.tabBuilder("tinted")
        .icon(EXAMPLE_ITEM)
        .vanillaTint("#D77FA6")
        .add(EXAMPLE_ITEM)
        .build();
```

Tinting is intentionally limited to vanilla appearance. This keeps it as the lightweight option for a tab that should still look vanilla instead of mixing it with the full custom overlay.

Supported tint forms:

```java
.vanillaTint("#D77FA6")
.vanillaTint("#D77FA6", 0.25F)
.vanillaAppearance().tint("#40D77FA6")
```

Six-digit tint colors use Butterfly API's default transparency. Eight-digit tint colors use `#AARRGGBB`.

### Categories

Categories reserve a full horizontal row, display a heading, and place their entries beneath it. Entries added directly to the main builder stay uncategorized and appear before the categories.

```java
public static final ItemGroup EXAMPLE_TAB = MOD.tabBuilder("example")
        .icon(EXAMPLE_ITEM)
        .add(UNCATEGORIZED_ITEM)
        .category("building", Text.literal("Building"), category -> category
                .alignLeft()
                .backgroundTexture(MOD.id("textures/gui/building_category.png"))
                .borderColor("#5B435F")
                .textColor("#FFFFFF")
                .add(EXAMPLE_BLOCK))
        .category("items", Text.literal("Items"), category -> category
                .alignCenter()
                .backgroundColor("#93678F")
                .add(EXAMPLE_ITEM))
        .build();
```

Category helpers include:

- `alignLeft()`, `alignCenter()`, and `alignRight()`.
- `textAlignment(TabCategory.TextAlignment)`.
- `backgroundTexture(Identifier)` or `backgroundColor("#RRGGBB")`.
- `borderColor("#RRGGBB")`.
- `textColor("#RRGGBB")`.
- `entries(...)`, `add(...)`, and the same styled-entry helpers as the main builder.
- `emptySlot()` for one deliberately empty position.
- `emptyRow()` for a full cushion row.

Categories appear in registration order unless `categoryOrder(...)` is used to provide an explicit order. A category with no matching entries is hidden while searching.

### Per-entry slot appearance

Normal entries automatically use the tab-wide slot texture or color:

```java
.add(EXAMPLE_ITEM)
```

A specific entry can override that surface:

```java
.entry(EXAMPLE_ITEM, entry -> entry
        .slotColor("#E68FB3"))
```

Texture overrides work the same way:

```java
.entry(EXAMPLE_BLOCK, entry -> entry
        .slotTexture(MOD.id("textures/gui/example_block_slot.png")))
```

The longer method names are also available:

```java
.entry(EXAMPLE_ITEM, entry -> entry
        .slotBackgroundColor("#E68FB3"))

.entry(EXAMPLE_BLOCK, entry -> entry
        .slotBackgroundTexture(MOD.id("textures/gui/example_block_slot.png")))
```

The same styling API is available inside categories and for search-only entries.

### Search-only entries

Search-only entries do not appear while the search field is empty. They become visible only when the current search matches them.

```java
public static final ItemGroup EXAMPLE_TAB = MOD.tabBuilder("example")
        .icon(EXAMPLE_ITEM)
        .add(EXAMPLE_ITEM)
        .searchOnly(SECRET_ITEM)
        .searchOnlyEntry(SECRET_BLOCK, entry -> entry
                .slotColor("#6B4C8C"))
        .build();
```

Normal entries do not require any extra code. Keep using `add(...)`, `entries(...)`, or `entry(...)` unless something should specifically be hidden until searched.

### Empty slots and rows

Empty positions still render the configured slot texture or color:

```java
public static final ItemGroup EXAMPLE_TAB = MOD.tabBuilder("example")
        .icon(EXAMPLE_ITEM)
        .add(EXAMPLE_ITEM)
        .emptySlot()
        .add(EXAMPLE_BLOCK)
        .emptyRow()
        .add(ANOTHER_ITEM)
        .build();
```

- `emptySlot()` reserves one slot.
- `emptyRow()` finishes the current row and adds a full empty cushion row.
- Empty positions keep the normal slot hover overlay.
- Category heading rows do not show individual slot hover overlays.

### Texture dimensions

Butterfly API stretches supplied textures to the expected UI area, so larger textures with the same ratio also work.

Recommended native sizes:

- Full creative background: `195x136`.
- Slot texture: `16x16`.
- Category texture: `160x16`.
- Vanilla search field width: `80`.

A category texture uses a `10:1` ratio. `320x32` or `640x64` will scale correctly, while a different ratio will be stretched to fit.

Example asset paths:

```text
assets/example_mod/textures/gui/container/creative_inventory/tab_example.png
assets/example_mod/textures/gui/example_slot.png
assets/example_mod/textures/gui/example_category.png
```

## CLIENT

Client setup gets its own little wrapper too. Use these from your client entrypoint.

```java
public final class ExampleClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExampleMod.MOD.cutout(ExampleMod.EXAMPLE_BLOCK);
        ExampleMod.MOD.entityRenderer(ExampleMod.EXAMPLE_ENTITY, ExampleEntityRenderer::new);
        ExampleMod.MOD.blockEntityRenderer(ExampleMod.EXAMPLE_BLOCK_ENTITY, ExampleBlockEntityRenderer::new);
    }
}
```

Client helpers include:

- `cutout`, `cutoutMipped`, and `translucent` render layers.
- `entityRenderer` for entity renderers.
- `blockEntityRenderer` for block entity renderers.
- `screen` for handled screens.
- `predicate` for item model predicates.
- `modelLayer` for entity model layers.
- `particleFactory` for particle factories.

## MATH

The math package is a pile of small helpers that are useful when working with particles, movement, targeting, hitboxes, and general "where is this thing in 3D space" code.

### Scalars

`Scalars` is for tiny number helpers:

- `clamp01`
- `square` and `cube`
- `isZero` and `nearlyEqual`
- `lerp` and `inverseLerp`
- `map` and `mapClamped`
- `approach`
- `roundTo`
- `EPSILON`

### Vecs

`Vecs` is for `Vec3d` work:

- `safeNormalize(vector)` and `safeNormalize(vector, fallback)`.
- `horizontal`, `withX`, `withY`, `withZ`.
- `addX`, `addY`, `addZ`.
- `scaleX`, `scaleY`, `scaleZ`.
- `midpoint`, `lerp`, `toward`, `direction`.
- `setLength`, `limitLength`, `clampLength`.
- `projectOnto`, `rejectFrom`, `reflect`, `perpendicular`.
- `closestPointOnSegment`, `distanceToSegment`, `distanceSqToSegment`.
- `lengthSquared`.

### Angles and YawPitch

`Angles` and `YawPitch` are for turning rotations into vectors and vectors back into rotations.

- degrees/radians conversion.
- degree and radian wrapping.
- shortest-angle deltas.
- degree lerp that handles wraparound properly.
- `direction(yaw, pitch)`.
- `yaw`, `pitch`, `yawPitch`, and `lookAt`.

```java
Vec3d direction = Angles.direction(player.getYaw(), player.getPitch());
YawPitch look = Angles.lookAt(player.getEyePos(), target.getEyePos());
```

### Basis, Sampling, Boxes, and Motion

`Basis3` makes a local forward/right/up basis from a direction. `Sampling` makes ring, circle, arc, and spread points. `Boxes` makes common `Box` shapes. `Motion` handles velocity, impulses, knockback, pushes, and pulls.

```java
Vec3d forward = Vecs.direction(player.getEyePos(), target.getEyePos());
List<Vec3d> ring = Sampling.circle(target.getPos(), forward, 2.0D, 16);
Box traceBox = Boxes.between(player.getEyePos(), target.getEyePos(), 0.25D);
Motion.pushFrom(target, player, 0.8D, 0.25D);
```

## PLUSHES

This is the fun part, but it is still an actual API. Butterfly API has a shared plush system for small placeable plush blocks, so other mods can add plushes without each one needing its own block entity setup.

Plushes are:

- Registered through the `butterfly_api:plush` entrypoint before plush bootstrap finishes.
- Bound into one shared `butterfly_api:plush` block entity type.
- Waterloggable and horizontally facing.
- Clickable, with a mode toggle, honk sound, and squish effect.
- Rendered with either a normal block model or GeckoLib.
- Customizable with block settings, item settings, sounds, animation controllers, render scale/offset, and use behavior.

Add a plush entrypoint in `fabric.mod.json`:

```json
{
  "entrypoints": {
    "butterfly_api:plush": [
      "example.ExamplePlushes"
    ]
  }
}
```

Register plushes from that entrypoint:

```java
public final class ExamplePlushes implements PlushEntrypoint {
    private static final ModContext MOD = ButterflyApi.mod("example_mod", "Example Mod");

    @Override
    public void registerPlushes(PlushRegistrar registrar) {
        registrar.plush(MOD, "plain_plush");

        registrar.plush(MOD, "custom_plush", builder -> builder
                .sound("custom_plush_honk")
                .itemSettings(new Item.Settings().maxCount(16))
                .onUse(plush -> plush.squish(2)));

        registrar.geckoPlush(MOD, "animated_plush", builder -> builder
                .loopingAnimation("idle")
                .geckolib(PlushDefinition.GeckoRenderData
                        .block(MOD, "animated_plush")
                        .withScale(0.9F)
                        .withOffset(0.0F, 0.0F, 0.0F)));
    }
}
```

Default plush conventions:

- Default sound id is `<namespace>:<path>_honk`.
- Normal plushes use normal blockstate, block model, item model, and texture assets.
- Gecko plushes default to:
  - `assets/<modid>/geo/block/<path>.geo.json`
  - `assets/<modid>/textures/block/<path>.png`
  - `assets/<modid>/animations/block/<path>.animation.json`

After bootstrap, a registered plush gives you its block, item, and sound:

```java
RegisteredPlush plush = ButterflyPlushes.get(MOD.id("plain_plush"));
Block block = plush.block();
Item item = plush.item();
SoundEvent sound = plush.sound();
```

Register plushes through the entrypoint path. Once `ButterflyPlushes.bootstrap()` has run, the shared block entity type is already built and new plushes cannot be added.

## TAGS AND DATAPACKS

Butterfly API exposes plush tags for code and datapacks.

```java
if (stack.isIn(ButterflyApiTags.PLUSH_ITEMS)) {
    // This item is a Butterfly API plush item.
}
```

Code helpers:

- `ButterflyApiTags.PLUSH_BLOCKS`
- `ButterflyApiTags.PLUSH_ITEMS`
- `ButterflyApiTags.block("path")`
- `ButterflyApiTags.item("path")`

Datapack ids:

- `#butterfly_api:plushes` for item tags.
- `#butterfly_api:plushes` for block tags.

The bundled plush tag currently contains:

- `butterfly_api:syntax_plush`
- `butterfly_api:hex_plush`
- `butterfly_api:hex_maid_plush`
- `butterfly_api:moth_plush`
- `butterfly_api:chicken_plush`
- `butterfly_api:capozi_plush`
- `butterfly_api:debug_plush`

## BOOTSTRAP HELPERS

`RunOnce` and `Bootstrap` are tiny helpers for init code that should only run once.

```java
private static final Bootstrap BOOTSTRAP = Bootstrap.create();

public static void init() {
    BOOTSTRAP.run(
            ModItems::init,
            ModBlocks::init,
            ModSounds::init
    );
}
```

`Bootstrap.run(...)` returns `true` the first time it runs and `false` after that. `hasRun()` tells you whether it already fired.

## BUNDLED CONTENT

Butterfly API is not only helper classes. It also ships some content under the `butterfly_api` namespace.

### Plushes

Bundled plushes:

- Syntax Plush
- Hex Plush
- Hex Maid Plush
- Moth Plush, using GeckoLib animation
- Chicken Plush
- Capozi Plush
- Debug Plush

Each plush is placeable, interactable, tagged as a plush, has a honk sound, and appears in the Butterfly API creative tab. There is also a `collect_all_plushes` advancement.

### Big Red Button

`butterfly_api:big_red_button` is exactly what it sounds like. It can be placed on floors, walls, or ceilings, and it tracks each player's clicks.

It grants advancements at:

- 1 click
- 100 clicks
- 10,000 clicks
- 1,000,000 clicks

### Matchstick

`butterfly_api:matchstick` is a small fire-starting item. It can:

- Light campfires, candles, and candle cakes.
- Place `matchstick_fire` or `matchstick_soul_fire`.
- Light Nether portals when used on a valid frame.
- Set living entities on fire.

### Connection Enchantment

`butterfly_api:connection` is a treasure enchantment for enchantable items. It binds to an owner when enchanted or taken from an anvil.

Connected items:

- Can only be picked up or taken by their owner after binding.
- Are preserved through death up to `ConnectionEnchantmentUtil.MAX_CONNECTION_DEATHS`.
- Can appear on books from trail ruins archaeology loot.

### Other Content

- `butterfly_api:hi` painting variant.
- Butterfly API creative tab.
- Crafting recipes for bundled plushes and the Big Red Button.
- Extra datapack recipes, including smithing template duplication recipes.

## PROJECT LAYOUT

If you want to dig through the source, the packages are split up like this:

- API entrypoints live in `moth.butterflyapi`.
- Registry helpers live in `moth.butterflyapi.registry`.
- Mod context helpers live in `moth.butterflyapi.mod`.
- Client helpers live in `moth.butterflyapi.client`.
- Item group helpers live in `moth.butterflyapi.itemgroup`.
- Math helpers live in `moth.butterflyapi.math`.
- Plush helpers live in `moth.butterflyapi.plush`.
- Bundled content lives in `moth.butterflyapi.content`.
