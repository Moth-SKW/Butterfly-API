package moth.butterflyapi.outline;

public enum OutlinePlacement {

    CENTERED(0),

    OUTSIDE(1),

    INSIDE(2);

    private final int id;

    OutlinePlacement(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public static OutlinePlacement fromId(int id) {
        return switch (id) {
            case 0 -> CENTERED;
            case 1 -> OUTSIDE;
            case 2 -> INSIDE;
            default -> throw new IllegalArgumentException("Unknown outline placement id: " + id);
        };
    }
}
