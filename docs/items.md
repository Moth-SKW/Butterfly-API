# Items

Butterfly item helpers cover three common 1.21.11 pain points:

- displayed attack speed modifiers
- separate GUI and held models/textures
- custom progress bars beneath item stacks

## Displayed Attack Speed

Vanilla attribute modifiers use `displayed speed - 4.0`. `ItemUtils` lets you pass the value you want players to see.

```java
import moth.butterflyapi.item.ItemUtils;
import net.minecraft.item.Item;

public static final Item QUICK_BLADE = MOD.item(
        "quick_blade",
        settings -> new Item(ItemUtils.combatAttributes(
                settings,
                MOD.id("quick_blade"),
                5.0D,
                1.8D))
);
```

Use only attack speed:

```java
ItemUtils.displayedAttackSpeed(settings, MOD.id("fast_tool"), 2.4D);
```

Convert manually:

```java
double vanillaModifier = ItemUtils.attackSpeedModifier(1.6D);
```

## Separate GUI And Held Models

Minecraft 1.21.11 uses item model definitions under `assets/<modid>/items/<id>.json`. Butterfly can generate the JSON text for a split GUI/held item.

```java
import moth.butterflyapi.item.ItemModelDefinitions;

var models = ItemModelDefinitions.separateGuiAndHeld(MOD.id("twisted_spade"));

String itemDefinitionPath = models.itemDefinitionPath();
String itemDefinitionJson = models.itemDefinitionJson();
String guiModelJson = models.guiModelJson(MOD.id("item/twisted_spade_gui"));
String heldModelJson = models.heldModelJson(MOD.id("item/twisted_spade"));
```

Default output paths:

```text
assets/<modid>/items/<id>.json
assets/<modid>/models/item/<id>.json
assets/<modid>/models/item/<id>_held.json
```

The item definition uses the held model in:

- `firstperson_lefthand`
- `firstperson_righthand`
- `thirdperson_lefthand`
- `thirdperson_righthand`

The GUI model is the fallback for inventory, ground, fixed, and other contexts.

## Custom Item Progress Bars

Register a single Twisted Spade-style bar:

```java
import moth.butterflyapi.item.bar.ItemProgressBars;

ItemProgressBars.register(TWISTED_SPADE, 0xE63F51, stack -> charge(stack));
```

`charge(stack)` should return normalized progress where `0.0` is empty and `1.0` is full.

Register multiple rows:

```java
import moth.butterflyapi.item.bar.ItemProgressBar;

ItemProgressBars.register(TWISTED_CORE, stack -> List.of(
        ItemProgressBar.of(primaryProgress(stack), 0x66CCFF),
        ItemProgressBar.of(secondaryProgress(stack), 0xFFD166, false)
));
```

Register layered progress:

```java
ItemProgressBars.registerLayered(
        TWISTED_SPADE,
        true,
        stack -> storedCycles(stack),
        0xE63F51,
        0x8A4DFF,
        0xFFD166
);
```

Layered progress accepts values above `1.0`. A value of `2.35` means two completed layers plus a 35% overlay of the next color. Butterfly renders layered bars in constant time, so high values do not draw every completed layer.

`pixelSnap` controls width rounding:

- `true`: rounds to pixel steps for a crisp vanilla-style bar
- `false`: uses ceiling behavior so tiny non-zero progress appears sooner
