package moth.butterflyapi.bootstrap;

import java.util.Objects;

public final class RunOnce {
    private boolean hasRun;

    private RunOnce() {
    }

    public static RunOnce create() {
        return new RunOnce();
    }

    public synchronized boolean run(Runnable action) {
        Objects.requireNonNull(action, "action");
        if (hasRun) {
            return false;
        }

        action.run();
        hasRun = true;
        return true;
    }

    public synchronized boolean hasRun() {
        return hasRun;
    }
}
