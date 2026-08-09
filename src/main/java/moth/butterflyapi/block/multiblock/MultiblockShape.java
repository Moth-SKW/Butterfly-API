package moth.butterflyapi.block.multiblock;

import net.minecraft.util.math.Direction;

import java.util.Objects;
import java.util.stream.Stream;

public record MultiblockShape(int width, int height, int depth) {
    public static final MultiblockShape ONE_BY_THREE_BY_ONE = new MultiblockShape(1, 3, 1);
    public static final MultiblockShape ONE_BY_FOUR_BY_ONE = new MultiblockShape(1, 4, 1);
    public static final MultiblockShape TWO_BY_TWO_BY_TWO = new MultiblockShape(2, 2, 2);
    public static final MultiblockShape TWO_BY_ONE_BY_TWO = new MultiblockShape(2, 1, 2);

    public MultiblockShape {
        if (width <= 0 || height <= 0 || depth <= 0) {
            throw new IllegalArgumentException("Multiblock dimensions must be positive");
        }
        if (width > 2 || height > 4 || depth > 2) {
            throw new IllegalArgumentException("Butterfly multiblock properties support up to 2x4x2");
        }
    }

    public Stream<MultiblockPart> parts() {
        Stream.Builder<MultiblockPart> builder = Stream.builder();
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
                for (int x = 0; x < width; x++) {
                    builder.add(new MultiblockPart(x, y, z));
                }
            }
        }
        return builder.build();
    }

    public boolean contains(MultiblockPart part) {
        Objects.requireNonNull(part, "part");
        return part.x() >= 0 && part.x() < width
                && part.y() >= 0 && part.y() < height
                && part.z() >= 0 && part.z() < depth;
    }

    public static Direction horizontal(Direction direction) {
        Direction checkedDirection = Objects.requireNonNull(direction, "direction");
        return checkedDirection.getAxis().isHorizontal() ? checkedDirection : Direction.NORTH;
    }
}
