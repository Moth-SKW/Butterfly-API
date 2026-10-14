package moth.butterflyapi.testing;

import moth.butterflyapi.block.multiblock.AbstractMultiblockBlock;
import moth.butterflyapi.block.multiblock.MultiblockShape;
import net.minecraft.world.level.block.state.BlockBehaviour;

final class IntegrationTestMultiblockBlock extends AbstractMultiblockBlock {
    IntegrationTestMultiblockBlock(BlockBehaviour.Properties settings) {
        super(MultiblockShape.TWO_BY_ONE_BY_TWO, settings);
    }
}
