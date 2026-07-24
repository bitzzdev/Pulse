package io.github.pulse.modules;

import io.github.pulse.Pulse;
import io.github.pulse.api.AdaptiveModule;
import io.github.pulse.api.ModuleType;
import io.github.pulse.api.OptimizationStage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleManager {
    private static final ModuleManager INSTANCE = new ModuleManager();

    private final Map<ModuleType, AdaptiveModule> modules = new ConcurrentHashMap<>();
    private boolean initialized;

    private ModuleManager() {}

    public static ModuleManager getInstance() {
        return INSTANCE;
    }

    public void initialize() {
        if (initialized) return;
        register(new ParticleOptimizer());
        register(new EntityOptimizer());
        register(new ChunkUploadOptimizer());
        register(new CloudOptimizer());
        register(new ShadowOptimizer());
        register(new TransparencyOptimizer());
        register(new WeatherOptimizer());
        register(new AnimationOptimizer());
        register(new UIOptimizer());
        for (AdaptiveModule module : modules.values()) {
            module.initialize();
        }
        initialized = true;
        Pulse.LOGGER.info("Registered {} optimization modules", modules.size());
    }

    public void shutdown() {
        if (!initialized) return;
        for (AdaptiveModule module : modules.values()) {
            try { module.shutdown(); } catch (Exception e) {
                Pulse.LOGGER.error("Error shutting down module {}", module.getName(), e);
            }
        }
        modules.clear();
        initialized = false;
    }

    public void register(AdaptiveModule module) {
        modules.put(module.getType(), module);
        Pulse.LOGGER.debug("Registered module: {}", module.getName());
    }

    public void update(OptimizationStage stage) {
        for (AdaptiveModule module : modules.values()) {
            if (module.isEnabled()) {
                try {
                    module.update(stage);
                } catch (Exception e) {
                    Pulse.LOGGER.error("Error updating module {}: {}", module.getName(), e.getMessage());
                }
            }
        }
    }

    public AdaptiveModule getModule(ModuleType type) {
        return modules.get(type);
    }

    public AdaptiveModule getModuleByName(String name) {
        for (AdaptiveModule module : modules.values()) {
            if (module.getName().equalsIgnoreCase(name)) return module;
        }
        return null;
    }

    public Collection<AdaptiveModule> getAllModules() {
        return Collections.unmodifiableCollection(modules.values());
    }

    public List<AdaptiveModule> getActiveModules() {
        List<AdaptiveModule> active = new ArrayList<>();
        for (AdaptiveModule module : modules.values()) {
            if (module.isEnabled()) active.add(module);
        }
        return active;
    }

    public double getTotalEstimatedSavings() {
        double total = 0.0;
        for (AdaptiveModule module : modules.values()) {
            if (module.isEnabled()) total += module.getEstimatedSavings();
        }
        return total;
    }

    public void enableModule(ModuleType type) {
        AdaptiveModule module = modules.get(type);
        if (module != null) {
            module.enable();
            Pulse.LOGGER.info("Enabled module: {}", module.getName());
        }
    }

    public void disableModule(ModuleType type) {
        AdaptiveModule module = modules.get(type);
        if (module != null) {
            module.disable();
            Pulse.LOGGER.info("Disabled module: {}", module.getName());
        }
    }

    public boolean isInitialized() { return initialized; }
}