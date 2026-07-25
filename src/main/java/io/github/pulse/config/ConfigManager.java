package io.github.pulse.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.pulse.api.ModuleType;
import io.github.pulse.api.OptimizationStage;
import io.github.pulse.config.PulseConfig.OverlayPosition;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class ConfigManager implements ModMenuApi {
    private static final Logger LOGGER = LoggerFactory.getLogger("pulse-config");
    private static ConfigManager instance;

    private PulseConfig config;

    public ConfigManager() {
        config = new PulseConfig();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public void initialize() {
        instance = this;
        config.load();
        LOGGER.info("ConfigManager initialized");
    }

    public void shutdown() {
        config.save();
        LOGGER.info("ConfigManager shut down");
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::buildScreen;
    }

    @Override
    public Screen create(Screen parent) {
        return buildScreen(parent);
    }

    public Screen getScreen() {
        return buildScreen(null);
    }

    public Screen getScreen(Screen parent) {
        return buildScreen(parent);
    }

    private Screen buildScreen(Screen parent) {
        PulseConfig cfg = getInstance().config;

        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.literal("Pulse Configuration"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
        general.addEntry(entryBuilder
            .startIntSlider(Component.literal("Target FPS"), cfg.getTargetFps(), 30, 240)
            .setDefaultValue(60)
            .setTextGetter(value -> Component.literal(value + " FPS"))
            .setSaveConsumer(cfg::setTargetFps)
            .build());
        general.addEntry(entryBuilder
            .startLongSlider(Component.literal("Pressure Sensitivity"), (long)(cfg.getPressureSensitivity() * 100), 50, 200)
            .setDefaultValue(100)
            .setTextGetter(value -> Component.literal(String.format("%.2f", value / 100.0)))
            .setSaveConsumer(value -> cfg.setPressureSensitivity(value / 100.0))
            .build());
        general.addEntry(entryBuilder
            .startLongSlider(Component.literal("Recovery Speed"), (long)(cfg.getRecoverySpeed() * 100), 10, 200)
            .setDefaultValue(100)
            .setTextGetter(value -> Component.literal(String.format("%.2f", value / 100.0)))
            .setSaveConsumer(value -> cfg.setRecoverySpeed(value / 100.0))
            .build());
        general.addEntry(entryBuilder
            .startEnumSelector(Component.literal("Max Optimization Stage"), OptimizationStage.class, cfg.getMaxOptimizationStage())
            .setDefaultValue(OptimizationStage.EMERGENCY)
            .setSaveConsumer(cfg::setMaxOptimizationStage)
            .build());

        ConfigCategory performance = builder.getOrCreateCategory(Component.literal("Performance"));
        performance.addEntry(entryBuilder
            .startIntSlider(Component.literal("Target FPS"), cfg.getTargetFps(), 30, 240)
            .setDefaultValue(60)
            .setTextGetter(value -> Component.literal(value + " FPS"))
            .setSaveConsumer(cfg::setTargetFps)
            .build());

        ConfigCategory modules = builder.getOrCreateCategory(Component.literal("Modules"));
        for (ModuleType type : ModuleType.values()) {
            String displayName = type.name().charAt(0) + type.name().substring(1).toLowerCase();
            modules.addEntry(entryBuilder
                .startBooleanToggle(Component.literal(displayName), cfg.isModuleEnabled(type))
                .setDefaultValue(true)
                .setSaveConsumer(value -> cfg.setModuleEnabled(type, value))
                .build());
        }

        ConfigCategory display = builder.getOrCreateCategory(Component.literal("Display"));
        display.addEntry(entryBuilder
            .startBooleanToggle(Component.literal("Show Overlay"), cfg.isShowOverlay())
            .setDefaultValue(true)
            .setSaveConsumer(cfg::setShowOverlay)
            .build());
        display.addEntry(entryBuilder
            .startEnumSelector(Component.literal("Overlay Position"), OverlayPosition.class, cfg.getOverlayPosition())
            .setDefaultValue(OverlayPosition.TOP_LEFT)
            .setSaveConsumer(cfg::setOverlayPosition)
            .build());
        display.addEntry(entryBuilder
            .startBooleanToggle(Component.literal("Show Frame Time Graph"), cfg.isShowFrameTimeGraph())
            .setDefaultValue(false)
            .setSaveConsumer(cfg::setShowFrameTimeGraph)
            .build());

        ConfigCategory advanced = builder.getOrCreateCategory(Component.literal("Advanced"));
        advanced.addEntry(entryBuilder
            .startBooleanToggle(Component.literal("Debug Mode"), cfg.isDebugMode())
            .setDefaultValue(false)
            .setSaveConsumer(cfg::setDebugMode)
            .build());
        advanced.addEntry(entryBuilder
            .startBooleanToggle(Component.literal("Experimental Features"), cfg.isExperimentalFeatures())
            .setDefaultValue(false)
            .setSaveConsumer(cfg::setExperimentalFeatures)
            .build());

        builder.setSavingRunnable(() -> {
            cfg.save();
            LOGGER.info("Configuration saved from GUI");
        });

        return builder.build();
    }

    public PulseConfig getConfig() {
        return config;
    }

    public void resetToDefaults() {
        config.resetToDefaults();
        config.save();
        LOGGER.info("Configuration reset to defaults");
    }

    public void exportConfig(Path path) {
        config.exportTo(path);
    }

    public void importConfig(Path path) {
        config.importFrom(path);
        config.save();
    }

    public static int getTargetFps() {
        return getInstance().config.getTargetFps();
    }

    public static double getPressureSensitivity() {
        return getInstance().config.getPressureSensitivity();
    }

    public static double getRecoverySpeed() {
        return getInstance().config.getRecoverySpeed();
    }

    public static OptimizationStage getMaxOptimizationStage() {
        return getInstance().config.getMaxOptimizationStage();
    }

    public static boolean isModuleEnabled(ModuleType type) {
        return getInstance().config.isModuleEnabled(type);
    }

    public static boolean isShowOverlay() {
        return getInstance().config.isShowOverlay();
    }

    public static OverlayPosition getOverlayPosition() {
        return getInstance().config.getOverlayPosition();
    }

    public static boolean isShowFrameTimeGraph() {
        return getInstance().config.isShowFrameTimeGraph();
    }

    public static boolean isDebugMode() {
        return getInstance().config.isDebugMode();
    }

    public static boolean isExperimentalFeatures() {
        return getInstance().config.isExperimentalFeatures();
    }
}
