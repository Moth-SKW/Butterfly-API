package moth.butterflyapi.outline;

import java.util.Objects;

public final class BlockOutlineSettings {
    public static final int DEFAULT_MAX_CONNECTED_BLOCKS = 2048;
    public static final float DEFAULT_DEPTH_BIAS = 0.002F;
    public static final float DEFAULT_FACE_PADDING = 0.003F;
    public static final float DEFAULT_POLYGON_OFFSET_FACTOR = 0.0F;
    public static final float DEFAULT_POLYGON_OFFSET_UNITS = 0.0F;
    public static final BlockOutlineSettings DEFAULT = builder().build();

    private final boolean mergeConnectedBlocks;
    private final int maxConnectedBlocks;
    private final float depthBias;
    private final float facePadding;
    private final boolean matchModelLayer;
    private final boolean preserveInsideInterior;
    private final float polygonOffsetFactor;
    private final float polygonOffsetUnits;

    public BlockOutlineSettings(
            boolean mergeConnectedBlocks,
            int maxConnectedBlocks,
            float depthBias,
            float facePadding,
            boolean matchModelLayer,
            boolean preserveInsideInterior,
            float polygonOffsetFactor,
            float polygonOffsetUnits
    ) {
        this.mergeConnectedBlocks = mergeConnectedBlocks;
        this.maxConnectedBlocks = validateMaxConnectedBlocks(maxConnectedBlocks);
        this.depthBias = validateDepthBias(depthBias);
        this.facePadding = validateFacePadding(facePadding);
        this.matchModelLayer = matchModelLayer;
        this.preserveInsideInterior = preserveInsideInterior;
        this.polygonOffsetFactor = validatePolygonOffset(polygonOffsetFactor, "polygonOffsetFactor");
        this.polygonOffsetUnits = validatePolygonOffset(polygonOffsetUnits, "polygonOffsetUnits");
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return builder()
                .mergeConnectedBlocks(mergeConnectedBlocks)
                .maxConnectedBlocks(maxConnectedBlocks)
                .depthBias(depthBias)
                .facePadding(facePadding)
                .matchModelLayer(matchModelLayer)
                .preserveInsideInterior(preserveInsideInterior)
                .polygonOffset(polygonOffsetFactor, polygonOffsetUnits);
    }

    public boolean mergeConnectedBlocks() {
        return mergeConnectedBlocks;
    }

    public int maxConnectedBlocks() {
        return maxConnectedBlocks;
    }

    public float depthBias() {
        return depthBias;
    }

    public float facePadding() {
        return facePadding;
    }

    public boolean matchModelLayer() {
        return matchModelLayer;
    }

    public boolean preserveInsideInterior() {
        return preserveInsideInterior;
    }

    public float polygonOffsetFactor() {
        return polygonOffsetFactor;
    }

    public float polygonOffsetUnits() {
        return polygonOffsetUnits;
    }

    public boolean hasPolygonOffset() {
        return polygonOffsetFactor != 0.0F || polygonOffsetUnits != 0.0F;
    }

    private static int validateMaxConnectedBlocks(int maxConnectedBlocks) {
        if (maxConnectedBlocks < 1) {
            throw new IllegalArgumentException("maxConnectedBlocks must be at least 1");
        }
        return maxConnectedBlocks;
    }

    private static float validateDepthBias(float depthBias) {
        if (!Float.isFinite(depthBias) || depthBias < 0.0F || depthBias > 0.1F) {
            throw new IllegalArgumentException("depthBias must be between 0.0 and 0.1");
        }
        return depthBias;
    }

    private static float validateFacePadding(float facePadding) {
        if (!Float.isFinite(facePadding) || facePadding < 0.0F || facePadding > 0.1F) {
            throw new IllegalArgumentException("facePadding must be between 0.0 and 0.1");
        }
        return facePadding;
    }

    private static float validatePolygonOffset(float value, String name) {
        if (!Float.isFinite(value) || value < -64.0F || value > 64.0F) {
            throw new IllegalArgumentException(name + " must be between -64.0 and 64.0");
        }
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof BlockOutlineSettings other)) {
            return false;
        }
        return mergeConnectedBlocks == other.mergeConnectedBlocks
                && maxConnectedBlocks == other.maxConnectedBlocks
                && Float.compare(depthBias, other.depthBias) == 0
                && Float.compare(facePadding, other.facePadding) == 0
                && matchModelLayer == other.matchModelLayer
                && preserveInsideInterior == other.preserveInsideInterior
                && Float.compare(polygonOffsetFactor, other.polygonOffsetFactor) == 0
                && Float.compare(polygonOffsetUnits, other.polygonOffsetUnits) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                mergeConnectedBlocks,
                maxConnectedBlocks,
                depthBias,
                facePadding,
                matchModelLayer,
                preserveInsideInterior,
                polygonOffsetFactor,
                polygonOffsetUnits
        );
    }

    @Override
    public String toString() {
        return "BlockOutlineSettings{mergeConnectedBlocks=%s, maxConnectedBlocks=%d, depthBias=%s, facePadding=%s, matchModelLayer=%s, preserveInsideInterior=%s, polygonOffsetFactor=%s, polygonOffsetUnits=%s}"
                .formatted(
                        mergeConnectedBlocks,
                        maxConnectedBlocks,
                        depthBias,
                        facePadding,
                        matchModelLayer,
                        preserveInsideInterior,
                        polygonOffsetFactor,
                        polygonOffsetUnits
                );
    }

    public static final class Builder {
        private boolean mergeConnectedBlocks;
        private int maxConnectedBlocks = DEFAULT_MAX_CONNECTED_BLOCKS;
        private float depthBias = DEFAULT_DEPTH_BIAS;
        private float facePadding = DEFAULT_FACE_PADDING;
        private boolean matchModelLayer;
        private boolean preserveInsideInterior;
        private float polygonOffsetFactor = DEFAULT_POLYGON_OFFSET_FACTOR;
        private float polygonOffsetUnits = DEFAULT_POLYGON_OFFSET_UNITS;

        private Builder() {
        }

        public Builder mergeConnectedBlocks(boolean mergeConnectedBlocks) {
            this.mergeConnectedBlocks = mergeConnectedBlocks;
            return this;
        }

        public Builder maxConnectedBlocks(int maxConnectedBlocks) {
            this.maxConnectedBlocks = validateMaxConnectedBlocks(maxConnectedBlocks);
            return this;
        }

        public Builder depthBias(float depthBias) {
            this.depthBias = validateDepthBias(depthBias);
            return this;
        }

        public Builder facePadding(float facePadding) {
            this.facePadding = validateFacePadding(facePadding);
            return this;
        }

        public Builder matchModelLayer(boolean matchModelLayer) {
            this.matchModelLayer = matchModelLayer;
            return this;
        }

        public Builder preserveInsideInterior(boolean preserveInsideInterior) {
            this.preserveInsideInterior = preserveInsideInterior;
            return this;
        }

        public Builder polygonOffset(float factor, float units) {
            this.polygonOffsetFactor = validatePolygonOffset(factor, "polygonOffsetFactor");
            this.polygonOffsetUnits = validatePolygonOffset(units, "polygonOffsetUnits");
            return this;
        }

        public BlockOutlineSettings build() {
            return new BlockOutlineSettings(
                    mergeConnectedBlocks,
                    maxConnectedBlocks,
                    depthBias,
                    facePadding,
                    matchModelLayer,
                    preserveInsideInterior,
                    polygonOffsetFactor,
                    polygonOffsetUnits
            );
        }
    }
}
