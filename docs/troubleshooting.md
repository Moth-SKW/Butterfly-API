# Troubleshooting

## Raw Creative Tab Or Category Keys

Add the missing translation keys to `assets/<modid>/lang/en_us.json`.

```json
{
  "itemGroup.example_mod.main": "Example Mod",
  "itemGroup.example_mod.category.materials": "Materials"
}
```

## Block Or Item Crashes During Construction

Use keyed settings:

```java
MOD.blockSettings("my_block");
MOD.itemSettings("my_item");
```

Minecraft 1.21.11 expects registry keys to be attached before many block and item constructors run.

## Block Item Model Is Missing

Use both files:

```text
assets/<modid>/items/<id>.json
assets/<modid>/models/item/<id>.json
```

## Plush Sound Works But The Model Is Missing

Check that these paths all use the same plush id:

```text
assets/<modid>/blockstates/<plush>.json
assets/<modid>/models/block/<plush>.json
assets/<modid>/models/item/<plush>.json
assets/<modid>/items/<plush>.json
assets/<modid>/textures/block/<plush>.png
```

## Plush Has No Sound

Check:

- `assets/<modid>/sounds.json`
- `assets/<modid>/sounds/<plush>_honk.ogg`
- `PlushDefinition.sound(...)` if you override the default sound id

## Outlines Are Not Visible

Use `ClientOutlines` from client code for local-only targets, or `ServerOutlines` on the logical server for synced outlines.

For cutout or translucent custom renderers:

```java
OutlineStyle.builder("#FFFFFF")
        .matchModelLayer(true)
        .build();
```

Custom renderers that need their own mask should use `OutlineRenderers.capture(...)`.

## Multiblock Pieces Vanish Immediately

Make sure every part location can be replaced at placement time and that the blockstate includes the inherited `FACING`, `PART_X`, `PART_Y`, and `PART_Z` properties. `AbstractMultiblockBlock` automatically adds them unless a subclass overrides `appendProperties`.
