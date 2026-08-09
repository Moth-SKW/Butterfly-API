package moth.butterflyapi.outline;

import java.util.Locale;
import java.util.Objects;

public final class OutlineStyle {
    public static final int MIN_THICKNESS = 1;
    public static final int MAX_THICKNESS = 63;
    public static final int MAX_BORDERED_THICKNESS = 22;
    public static final int DEFAULT_THICKNESS = 2;
    public static final int DEFAULT_RGB = 0xFFFFFF;
    public static final boolean DEFAULT_BLACK_BORDER = false;
    public static final boolean DEFAULT_VISIBLE_THROUGH_WALLS = true;
    public static final boolean DEFAULT_MATCH_MODEL_LAYER = false;
    /** Keeps the requested outline width in framebuffer pixels at every distance. */
    public static final boolean DEFAULT_CONSISTENT_THICKNESS = true;
    /** Fraction of the distance-derived width approached per rendered frame. */
    public static final float DEFAULT_THICKNESS_ADAPTATION_RATE = 1.0F;
    public static final float MIN_THICKNESS_ADAPTATION_RATE = 0.0F;
    public static final float MAX_THICKNESS_ADAPTATION_RATE = 1.0F;
    /** Distance at which a non-consistent outline is at its requested width. */
    public static final float DEFAULT_THICKNESS_REFERENCE_DISTANCE = 16.0F;
    public static final OutlineStyle DEFAULT = new OutlineStyle(
            DEFAULT_RGB,
            DEFAULT_THICKNESS,
            OutlinePlacement.CENTERED,
            DEFAULT_BLACK_BORDER,
            DEFAULT_VISIBLE_THROUGH_WALLS,
            DEFAULT_MATCH_MODEL_LAYER,
            DEFAULT_CONSISTENT_THICKNESS,
            DEFAULT_THICKNESS_ADAPTATION_RATE
    );

    private final int rgb;
    private final int thickness;
    private final OutlinePlacement placement;
    private final boolean blackBorder;
    private final boolean visibleThroughWalls;
    private final boolean matchModelLayer;
    private final boolean consistentThickness;
    private final float thicknessAdaptationRate;

    public OutlineStyle(int rgb, int thickness, OutlinePlacement placement) {
        this(
                rgb,
                thickness,
                placement,
                DEFAULT_BLACK_BORDER,
                DEFAULT_VISIBLE_THROUGH_WALLS,
                DEFAULT_MATCH_MODEL_LAYER
        );
    }

    public OutlineStyle(
            int rgb,
            int thickness,
            OutlinePlacement placement,
            boolean blackBorder,
            boolean visibleThroughWalls,
            boolean matchModelLayer
    ) {
        this(
                rgb,
                thickness,
                placement,
                blackBorder,
                visibleThroughWalls,
                matchModelLayer,
                DEFAULT_CONSISTENT_THICKNESS,
                DEFAULT_THICKNESS_ADAPTATION_RATE
        );
    }

    public OutlineStyle(
            int rgb,
            int thickness,
            OutlinePlacement placement,
            boolean blackBorder,
            boolean visibleThroughWalls,
            boolean matchModelLayer,
            boolean consistentThickness,
            float thicknessAdaptationRate
    ) {
        this.rgb = validateRgb(rgb);
        this.thickness = validateThickness(thickness);
        this.placement = Objects.requireNonNull(placement, "placement");
        this.blackBorder = blackBorder;
        this.visibleThroughWalls = visibleThroughWalls;
        this.matchModelLayer = matchModelLayer;
        this.consistentThickness = consistentThickness;
        this.thicknessAdaptationRate = validateAdaptationRate(thicknessAdaptationRate);
        validateCombination(thickness, blackBorder);
    }

    public static OutlineStyle of(int rgb) {
        return new OutlineStyle(rgb, DEFAULT_THICKNESS, OutlinePlacement.CENTERED);
    }

    public static OutlineStyle of(String color) {
        return of(parseRgb(color));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(int rgb) {
        return builder().rgb(rgb);
    }

    public static Builder builder(String color) {
        return builder().color(color);
    }

    public Builder toBuilder() {
        return builder()
                .rgb(rgb)
                .thickness(thickness)
                .placement(placement)
                .blackBorder(blackBorder)
                .visibleThroughWalls(visibleThroughWalls)
                .matchModelLayer(matchModelLayer)
                .consistentThickness(consistentThickness)
                .thicknessAdaptationRate(thicknessAdaptationRate);
    }

    public int rgb() {
        return rgb;
    }

    public int thickness() {
        return thickness;
    }

    public OutlinePlacement placement() {
        return placement;
    }

    public boolean blackBorder() {
        return blackBorder;
    }

    public boolean visibleThroughWalls() {
        return visibleThroughWalls;
    }

    public boolean matchModelLayer() {
        return matchModelLayer;
    }

    public boolean consistentThickness() {
        return consistentThickness;
    }

    public float thicknessAdaptationRate() {
        return thicknessAdaptationRate;
    }

    public OutlineStyle withRgb(int rgb) {
        return new OutlineStyle(
                rgb,
                thickness,
                placement,
                blackBorder,
                visibleThroughWalls,
                matchModelLayer,
                consistentThickness,
                thicknessAdaptationRate
        );
    }

    public OutlineStyle withColor(String color) {
        return withRgb(parseRgb(color));
    }

    public OutlineStyle withThickness(int thickness) {
        return new OutlineStyle(
                rgb,
                thickness,
                placement,
                blackBorder,
                visibleThroughWalls,
                matchModelLayer,
                consistentThickness,
                thicknessAdaptationRate
        );
    }

    public OutlineStyle withPlacement(OutlinePlacement placement) {
        return new OutlineStyle(
                rgb,
                thickness,
                placement,
                blackBorder,
                visibleThroughWalls,
                matchModelLayer,
                consistentThickness,
                thicknessAdaptationRate
        );
    }

    public OutlineStyle withBlackBorder(boolean blackBorder) {
        return new OutlineStyle(
                rgb,
                thickness,
                placement,
                blackBorder,
                visibleThroughWalls,
                matchModelLayer,
                consistentThickness,
                thicknessAdaptationRate
        );
    }

    public OutlineStyle withVisibleThroughWalls(boolean visibleThroughWalls) {
        return new OutlineStyle(
                rgb,
                thickness,
                placement,
                blackBorder,
                visibleThroughWalls,
                matchModelLayer,
                consistentThickness,
                thicknessAdaptationRate
        );
    }

    public OutlineStyle withMatchModelLayer(boolean matchModelLayer) {
        return new OutlineStyle(
                rgb,
                thickness,
                placement,
                blackBorder,
                visibleThroughWalls,
                matchModelLayer,
                consistentThickness,
                thicknessAdaptationRate
        );
    }

    public OutlineStyle withConsistentThickness(boolean consistentThickness) {
        return new OutlineStyle(
                rgb,
                thickness,
                placement,
                blackBorder,
                visibleThroughWalls,
                matchModelLayer,
                consistentThickness,
                thicknessAdaptationRate
        );
    }

    public OutlineStyle withThicknessAdaptationRate(float thicknessAdaptationRate) {
        return new OutlineStyle(
                rgb,
                thickness,
                placement,
                blackBorder,
                visibleThroughWalls,
                matchModelLayer,
                consistentThickness,
                thicknessAdaptationRate
        );
    }

    public static int parseRgb(String color) {
        Objects.requireNonNull(color, "color");
        String value = color.trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        }
        if (value.length() != 6) {
            throw new IllegalArgumentException("Outline colors must use exactly six RGB digits: #RRGGBB");
        }
        for (int index = 0; index < value.length(); index++) {
            if (Character.digit(value.charAt(index), 16) < 0) {
                throw new IllegalArgumentException("Invalid outline color: " + color);
            }
        }
        return Integer.parseInt(value.toUpperCase(Locale.ROOT), 16);
    }

    private static int validateRgb(int rgb) {
        if ((rgb & 0xFF000000) != 0) {
            throw new IllegalArgumentException("Outline RGB must be between 0x000000 and 0xFFFFFF");
        }
        return rgb;
    }

    private static int validateThickness(int thickness) {
        if (thickness < MIN_THICKNESS || thickness > MAX_THICKNESS) {
            throw new IllegalArgumentException(
                    "Outline thickness must be between " + MIN_THICKNESS + " and " + MAX_THICKNESS + " pixels"
            );
        }
        return thickness;
    }

    private static void validateCombination(int thickness, boolean blackBorder) {
        if (blackBorder && thickness > MAX_BORDERED_THICKNESS) {
            throw new IllegalArgumentException(
                    "Outlined black borders support a maximum thickness of " + MAX_BORDERED_THICKNESS + " pixels"
            );
        }
    }

    private static float validateAdaptationRate(float rate) {
        if (!Float.isFinite(rate)
                || rate < MIN_THICKNESS_ADAPTATION_RATE
                || rate > MAX_THICKNESS_ADAPTATION_RATE) {
            throw new IllegalArgumentException(
                    "Outline thickness adaptation rate must be between "
                            + MIN_THICKNESS_ADAPTATION_RATE + " and " + MAX_THICKNESS_ADAPTATION_RATE
            );
        }
        return rate;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof OutlineStyle other)) {
            return false;
        }
        return rgb == other.rgb
                && thickness == other.thickness
                && placement == other.placement
                && blackBorder == other.blackBorder
                && visibleThroughWalls == other.visibleThroughWalls
                && matchModelLayer == other.matchModelLayer
                && consistentThickness == other.consistentThickness
                && Float.compare(thicknessAdaptationRate, other.thicknessAdaptationRate) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                rgb,
                thickness,
                placement,
                blackBorder,
                visibleThroughWalls,
                matchModelLayer,
                consistentThickness,
                thicknessAdaptationRate
        );
    }

    @Override
    public String toString() {
        return "OutlineStyle{rgb=#%06X, thickness=%d, placement=%s, blackBorder=%s, visibleThroughWalls=%s, matchModelLayer=%s, consistentThickness=%s, thicknessAdaptationRate=%s}"
                .formatted(
                        rgb,
                        thickness,
                        placement,
                        blackBorder,
                        visibleThroughWalls,
                        matchModelLayer,
                        consistentThickness,
                        thicknessAdaptationRate
                );
    }

    public static final class Builder {
        private int rgb = DEFAULT_RGB;
        private int thickness = DEFAULT_THICKNESS;
        private OutlinePlacement placement = OutlinePlacement.CENTERED;
        private boolean blackBorder = DEFAULT_BLACK_BORDER;
        private boolean visibleThroughWalls = DEFAULT_VISIBLE_THROUGH_WALLS;
        private boolean matchModelLayer = DEFAULT_MATCH_MODEL_LAYER;
        private boolean consistentThickness = DEFAULT_CONSISTENT_THICKNESS;
        private float thicknessAdaptationRate = DEFAULT_THICKNESS_ADAPTATION_RATE;

        private Builder() {
        }

        public Builder rgb(int rgb) {
            this.rgb = validateRgb(rgb);
            return this;
        }

        public Builder color(int rgb) {
            return rgb(rgb);
        }

        public Builder color(String color) {
            return rgb(parseRgb(color));
        }

        public Builder thickness(int thickness) {
            this.thickness = validateThickness(thickness);
            return this;
        }

        public Builder placement(OutlinePlacement placement) {
            this.placement = Objects.requireNonNull(placement, "placement");
            return this;
        }

        public Builder blackBorder(boolean blackBorder) {
            this.blackBorder = blackBorder;
            return this;
        }

        public Builder visibleThroughWalls(boolean visibleThroughWalls) {
            this.visibleThroughWalls = visibleThroughWalls;
            return this;
        }

        public Builder matchModelLayer(boolean matchModelLayer) {
            this.matchModelLayer = matchModelLayer;
            return this;
        }

        public Builder consistentThickness(boolean consistentThickness) {
            this.consistentThickness = consistentThickness;
            return this;
        }

        public Builder thicknessAdaptationRate(float thicknessAdaptationRate) {
            this.thicknessAdaptationRate = validateAdaptationRate(thicknessAdaptationRate);
            return this;
        }

        public OutlineStyle build() {
            return new OutlineStyle(
                    rgb,
                    thickness,
                    placement,
                    blackBorder,
                    visibleThroughWalls,
                    matchModelLayer,
                    consistentThickness,
                    thicknessAdaptationRate
            );
        }
    }
}
