# Blocks And Multiblocks

Butterfly provides direct block registration helpers for simple content and a builder for blocks with custom settings, items, or item factories.

## Block Builder

```java
import moth.butterflyapi.block.BlockRegistration;
import net.minecraft.block.Block;

public static final BlockRegistration<Block> DISPLAY_CASE = MOD.blockBuilder(
        "display_case",
        settings -> new Block(settings.strength(1.0F).nonOpaque()))
        .itemSettings(settings -> settings.maxCount(16))
        .register();
```

No block item:

```java
public static final BlockRegistration<Block> TECHNICAL_MARKER = MOD.blockBuilder(
        "technical_marker",
        settings -> new Block(settings.noCollision().nonOpaque()))
        .noItem()
        .register();
```

Custom block item:

```java
import net.minecraft.item.BlockItem;

public static final BlockRegistration<Block> DISPLAY_CASE = MOD.blockBuilder(
        "display_case",
        DisplayCaseBlock::new)
        .itemFactory(block -> new BlockItem(block, MOD.itemSettings("display_case").maxCount(1)))
        .register();
```

Tall block item:

```java
public static final BlockRegistration<TallCrystalBlock> TALL_CRYSTAL = MOD.blockBuilder(
        "tall_crystal",
        TallCrystalBlock::new)
        .tallBlockItem()
        .register();
```

## Multiblock Shapes

Built-in shapes:

```java
MultiblockShape.ONE_BY_THREE_BY_ONE
MultiblockShape.ONE_BY_FOUR_BY_ONE
MultiblockShape.TWO_BY_TWO_BY_TWO
MultiblockShape.TWO_BY_ONE_BY_TWO
```

Extend `AbstractMultiblockBlock` for common placement/removal behavior:

```java
import moth.butterflyapi.block.multiblock.AbstractMultiblockBlock;
import moth.butterflyapi.block.multiblock.MultiblockShape;
import net.minecraft.block.AbstractBlock;

public final class TallCrystalBlock extends AbstractMultiblockBlock {
    public TallCrystalBlock(AbstractBlock.Settings settings) {
        super(MultiblockShape.ONE_BY_THREE_BY_ONE, settings);
    }
}

public static final TallCrystalBlock TALL_CRYSTAL = MOD.block(
        "tall_crystal",
        TallCrystalBlock::new
);
```

`AbstractMultiblockBlock` stores:

- horizontal facing
- part X
- part Y
- part Z

When one part is removed, the remaining parts are removed without duplicate drops. If neighbor updates leave an incomplete structure behind, orphaned pieces clean themselves up.

Use `MultiblockPlacement` directly when your block needs custom behavior:

```java
boolean canPlace = MultiblockPlacement.canPlace(world, origin, facing, shape, context);
BlockPos partPos = MultiblockPlacement.partPos(origin, facing, part);
BlockPos origin = MultiblockPlacement.originOf(partPos, facing, part);
```

## Resource Checklist

For block items in Minecraft 1.21.11, remember both item model layers:

```text
assets/<modid>/items/<id>.json
assets/<modid>/models/item/<id>.json
```

Common block resources:

```text
assets/<modid>/blockstates/<id>.json
assets/<modid>/models/block/<id>.json
assets/<modid>/textures/block/<id>.png
data/<modid>/loot_table/blocks/<id>.json
data/<modid>/recipe/<id>.json
```
