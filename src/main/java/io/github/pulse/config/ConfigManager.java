package io.github.pulse.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import io.github.pulse.api.ModuleType;
import io.github.pulse.api.OptimizationStage;
import io.github.pulse.config.PulseConfig.OverlayPosition;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategoryBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class ConfigManager implements ConfigScreenFactory<Screen> {
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
            .setTitle(Text.literal("Pulse Configuration"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategoryBuilder general = builder.getOrCreateCategory(Text.literal("General"));
        general.addEntry(entryBuilder
            .startIntSlider(Text.literal("Target FPS"), cfg.getTargetFps(), 30, 240)
            .setDefaultValue(60)
            .setTextGetter(value -> Text.literal(value + " FPS"))
            .setSaveConsumer(cfg::setTargetFps)
            .build());
        general.addEntry(entryBuilder
            .startDoubleSlider(Text.literal("Pressure Sensitivity"), cfg.getPressureSensitivity(), 0.5, 2.0)
            .setDefaultValue(1.0)
            .setTextGetter(value -> Text.literal(String.format("%.2f", value)))
            .setSaveConsumer(cfg::setPressureSensitivity)
            .build());
        general.addEntry(entryBuilder
            .startDoubleSlider(Text.literal("Recovery Speed"), cfg.getRecoverySpeed(), 0.1, 2.0)
            .setDefaultValue(1.0)
            .setTextGetter(value -> Text.literal(String.format("%.2f", value)))
            .setSaveConsumer(cfg::setRecoverySpeed)
            .build());
        general.addEntry(entryBuilder
            .startEnumSelector(Text.literal("Max Optimization Stage"), OptimizationStage.class, cfg.getMaxOptimizationStage())
            .setDefaultValue(OptimizationStage.EMERGENCY)
            .setSaveConsumer(cfg::setMaxOptimizationStage)
            .build());

        ConfigCategoryBuilder performance = builder.getOrCreateCategory(Text.literal("Performance"));
        performance.addEntry(entryBuilder
            .startIntSlider(Text.literal("Target FPS"), cfg.getTargetFps(), 30, 240)
            .setDefaultValue(60)
            .setTextGetter(value -> Text.literal(value + " FPS"))
            .setSaveConsumer(cfg::setTargetFps)
            .build());

        ConfigCategoryBuilder modules = builder.getOrCreateCategory(Text.literal("Modules"));
        for (ModuleType type : ModuleType.values()) {
            String displayName = type.name().charAt(0) + type.name().substring(1).toLowerCase();
            modules.addEntry(entryBuilder
                .startBooleanToggle(Text.literal(displayName), cfg.isModuleEnabled(type))
                .setDefaultValue(true)
                .setSaveConsumer(value -> cfg.setModuleEnabled(type, value))
                .build());
        }

        ConfigCategoryBuilder display = builder.getOrCreateCategory(Text.literal("Display"));
        display.addEntry(entryBuilder
            .startBooleanToggle(Text.literal("Show Overlay"), cfg.isShowOverlay())
            .setDefaultValue(true)
            .setSaveConsumer(cfg::setShowOverlay)
            .build());
        display.addEntry(entryBuilder
            .startEnumSelector(Text.literal("Overlay Position"), OverlayPosition.class, cfg.getOverlayPosition())
            .setDefaultValue(OverlayPosition.TOP_LEFT)
            .setSaveConsumer(cfg::setOverlayPosition)
            .build());
        display.addEntry(entryBuilder
            .startBooleanToggle(Text.literal("Show Frame Time Graph"), cfg.isShowFrameTimeGraph())
            .setDefaultValue(false)
            .setSaveConsumer(cfg::setShowFrameTimeGraph)
            .build());

        ConfigCategoryBuilder advanced = builder.getOrCreateCategory(Text.literal("Advanced"));
        advanced.addEntry(entryBuilder
            .startBooleanToggle(Text.literal("Debug Mode"), cfg.isDebugMode())
            .setDefaultValue(false)
            .setSaveConsumer(cfg::setDebugMode)
            .build());
        advanced.addEntry(entryBuilder
            .startBooleanToggle(Text.literal("Experimental Features"), cfg.isExperimentalFeatures())
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
