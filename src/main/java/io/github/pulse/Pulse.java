package io.github.pulse;

import io.github.pulse.adaptive.AdaptiveManager;
import io.github.pulse.api.PulseAPI;
import io.github.pulse.budget.BudgetSystem;
import io.github.pulse.compat.CompatibilityManager;
import io.github.pulse.config.ConfigManager;
import io.github.pulse.modules.ModuleManager;
import io.github.pulse.performance.PerformanceEngine;
import io.github.pulse.pressure.PressureEngine;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pulse implements ModInitializer {
    public static final String MOD_ID = "pulse";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static ConfigManager configManager;
    private static PerformanceEngine performanceEngine;
    private static PressureEngine pressureEngine;
    private static BudgetSystem budgetSystem;
    private static ModuleManager moduleManager;
    private static CompatibilityManager compatibilityManager;
    private static AdaptiveManager adaptiveManager;
    private static boolean initialized = false;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Pulse");

        configManager = ConfigManager.getInstance();
        configManager.initialize();

        performanceEngine = PerformanceEngine.getInstance();
        performanceEngine.initialize();

        pressureEngine = new PressureEngine(performanceEngine);
        pressureEngine.initialize();

        budgetSystem = new BudgetSystem(performanceEngine);
        budgetSystem.initialize();

        moduleManager = ModuleManager.getInstance();
        moduleManager.initialize();

        adaptiveManager = new AdaptiveManager(pressureEngine);
        adaptiveManager.initialize();

        compatibilityManager = new CompatibilityManager();
        compatibilityManager.initialize();

        PulseAPI.initialize();

        initialized = true;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (initialized) {
                shutdown();
            }
        }, "Pulse Shutdown"));
    }

    public static void shutdown() {
        if (adaptiveManager != null) adaptiveManager.shutdown();
        if (moduleManager != null) moduleManager.shutdown();
        if (budgetSystem != null) budgetSystem.shutdown();
        if (pressureEngine != null) pressureEngine.shutdown();
        if (performanceEngine != null) performanceEngine.shutdown();
        if (configManager != null) configManager.shutdown();
        if (compatibilityManager != null) compatibilityManager.shutdown();
        initialized = false;
        LOGGER.info("Pulse shut down");
    }

    public static ConfigManager getConfigManager() { return configManager; }
    public static PerformanceEngine getPerformanceEngine() { return performanceEngine; }
    public static PressureEngine getPressureEngine() { return pressureEngine; }
    public static BudgetSystem getBudgetSystem() { return budgetSystem; }
    public static ModuleManager getModuleManager() { return moduleManager; }
    public static CompatibilityManager getCompatibilityManager() { return compatibilityManager; }
    public static AdaptiveManager getAdaptiveManager() { return adaptiveManager; }
    public static boolean isInitialized() { return initialized; }
}