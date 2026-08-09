# Creative Tabs

Butterfly can create simple item groups or styled categorized tabs.

## Simple Tab

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

`assets/example_mod/lang/en_us.json`:

```json
{
  "itemGroup.example_mod.main": "Example Mod"
}
```

## Categorized Tab

```java
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;

public static final ItemGroup EXAMPLE_TAB = MOD.tabBuilder("main")
        .icon(SILK_SCRAP)
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
                .add(SILK_SCRAP)
                .entry(POLISHED_SILK, entry -> entry.slotColor("#463B51")))
        .category("tools", Text.translatable("itemGroup.example_mod.category.tools"), category -> category
                .backgroundColor("#2F3740")
                .add(EXAMPLE_TOOL))
        .categoryOrder("materials", "tools")
        .searchOnly(HIDDEN_DEBUG_ITEM)
        .build();
```

Category names can be literal text or translatable text.

```json
{
  "itemGroup.example_mod.category.materials": "Materials",
  "itemGroup.example_mod.category.tools": "Tools"
}
```

## Add To Existing Tabs

```java
import net.minecraft.item.ItemGroups;

MOD.addTo(ItemGroups.INGREDIENTS, SILK_SCRAP);
MOD.addTo(ItemGroups.BUILDING_BLOCKS, POLISHED_SILK);
```
