package io.github.pulse.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.pulse.api.ModuleType;
import io.github.pulse.api.OptimizationStage;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PulseConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("pulse-config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("pulse.json");

    private int targetFps = 60;
    private double pressureSensitivity = 1.0;
    private double recoverySpeed = 1.0;
    private OptimizationStage maxOptimizationStage = OptimizationStage.EMERGENCY;
    private Map<String, Boolean> moduleEnabled = new HashMap<>();
    private boolean debugMode = false;

    public PulseConfig() {
        for (ModuleType type : ModuleType.values()) {
            moduleEnabled.put(type.name(), true);
        }
    }

    public int getTargetFps() {
        return targetFps;
    }

    public void setTargetFps(int targetFps) {
        this.targetFps = targetFps;
    }

    public double getPressureSensitivity() {
        return pressureSensitivity;
    }

    public void setPressureSensitivity(double pressureSensitivity) {
        this.pressureSensitivity = pressureSensitivity;
    }

    public double getRecoverySpeed() {
        return recoverySpeed;
    }

    public void setRecoverySpeed(double recoverySpeed) {
        this.recoverySpeed = recoverySpeed;
    }

    public OptimizationStage getMaxOptimizationStage() {
        return maxOptimizationStage;
    }

    public void setMaxOptimizationStage(OptimizationStage maxOptimizationStage) {
        this.maxOptimizationStage = maxOptimizationStage;
    }

    public boolean isModuleEnabled(ModuleType type) {
        return moduleEnabled.getOrDefault(type.name(), true);
    }

    public void setModuleEnabled(ModuleType type, boolean enabled) {
        moduleEnabled.put(type.name(), enabled);
    }

    public Map<String, Boolean> getModuleEnabledMap() {
        return moduleEnabled;
    }

    public void setModuleEnabledMap(Map<String, Boolean> moduleEnabled) {
        this.moduleEnabled = moduleEnabled;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                PulseConfig loaded = GSON.fromJson(reader, PulseConfig.class);
                if (loaded != null) {
                    this.targetFps = loaded.targetFps;
                    this.pressureSensitivity = loaded.pressureSensitivity;
                    this.recoverySpeed = loaded.recoverySpeed;
                    this.maxOptimizationStage = loaded.maxOptimizationStage;
                    if (loaded.moduleEnabled != null && !loaded.moduleEnabled.isEmpty()) {
                        for (ModuleType type : ModuleType.values()) {
                            if (loaded.moduleEnabled.containsKey(type.name())) {
                                this.moduleEnabled.put(type.name(), loaded.moduleEnabled.get(type.name()));
                            }
                        }
                    }
                    this.debugMode = loaded.debugMode;
                    LOGGER.info("Configuration loaded from {}", CONFIG_PATH);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load configuration", e);
            }
        } else {
            LOGGER.info("No configuration file found, using defaults");
            save();
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
                LOGGER.info("Configuration saved to {}", CONFIG_PATH);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save configuration", e);
        }
    }

    public void resetToDefaults() {
        this.targetFps = 60;
        this.pressureSensitivity = 1.0;
        this.recoverySpeed = 1.0;
        this.maxOptimizationStage = OptimizationStage.EMERGENCY;
        this.moduleEnabled.clear();
        for (ModuleType type : ModuleType.values()) {
            this.moduleEnabled.put(type.name(), true);
        }
        this.debugMode = false;
    }

    public void exportTo(Path path) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(this, writer);
                LOGGER.info("Configuration exported to {}", path);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to export configuration", e);
        }
    }

    public void importFrom(Path path) {
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                PulseConfig loaded = GSON.fromJson(reader, PulseConfig.class);
                if (loaded != null) {
                    this.targetFps = loaded.targetFps;
                    this.pressureSensitivity = loaded.pressureSensitivity;
                    this.recoverySpeed = loaded.recoverySpeed;
                    this.maxOptimizationStage = loaded.maxOptimizationStage;
                    if (loaded.moduleEnabled != null && !loaded.moduleEnabled.isEmpty()) {
                        this.moduleEnabled.clear();
                        this.moduleEnabled.putAll(loaded.moduleEnabled);
                    }
                    this.debugMode = loaded.debugMode;
                    LOGGER.info("Configuration imported from {}", path);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to import configuration", e);
            }
        } else {
            LOGGER.warn("Cannot import from non-existent file: {}", path);
        }
    }
}
