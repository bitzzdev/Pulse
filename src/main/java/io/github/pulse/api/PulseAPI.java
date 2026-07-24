package io.github.pulse.api;

import io.github.pulse.Pulse;
import io.github.pulse.adaptive.AdaptiveManager;
import io.github.pulse.modules.ModuleManager;
import io.github.pulse.performance.PerformanceEngine;
import io.github.pulse.pressure.PressureEngine;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PulseAPI {
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    private PulseAPI() {}

    public static void initialize() {
        initialized.set(true);
    }

    public static int getPressureScore() {
        PressureEngine engine = Pulse.getPressureEngine();
        if (engine == null) return 0;
        return (int) Math.round(engine.getPressureScore());
    }

    public static OptimizationStage getOptimizationStage() {
        AdaptiveManager manager = Pulse.getAdaptiveManager();
        if (manager == null) return OptimizationStage.NORMAL;
        return manager.getCurrentStage();
    }

    public static int getCurrentFps() {
        PerformanceEngine engine = Pulse.getPerformanceEngine();
        if (engine == null) return 0;
        return Math.round(engine.getCurrentFps());
    }

    public static int getTargetFps() {
        if (Pulse.getBudgetSystem() == null) return 60;
        return Pulse.getBudgetSystem().getTargetFps();
    }

    public static boolean registerModule(AdaptiveModule module) {
        ModuleManager manager = Pulse.getModuleManager();
        if (manager == null) return false;
        manager.register(module);
        return true;
    }

    public static boolean isInitialized() {
        return initialized.get();
    }
}