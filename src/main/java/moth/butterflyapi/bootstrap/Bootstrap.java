package moth.butterflyapi.bootstrap;

import java.util.Objects;

public final class Bootstrap {
    private final RunOnce runOnce = RunOnce.create();

    private Bootstrap() {
    }

    public static Bootstrap create() {
        return new Bootstrap();
    }

    public boolean run(Runnable... steps) {
        Objects.requireNonNull(steps, "steps");
        return runOnce.run(() -> {
            for (Runnable step : steps) {
                Objects.requireNonNull(step, "step").run();
            }
        });
    }

    public boolean hasRun() {
        return runOnce.hasRun();
    }
}
