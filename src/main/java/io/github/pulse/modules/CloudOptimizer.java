package io.github.pulse.modules;

import io.github.pulse.Pulse;
import io.github.pulse.api.AdaptiveModule;
import io.github.pulse.api.ModuleType;
import io.github.pulse.api.OptimizationStage;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

public class CloudOptimizer implements AdaptiveModule {

    private static final float[] RENDER_DISTANCE_FACTORS = {1.0f, 0.8f, 0.6f, 0.4f, 0.0f};
    private static final float[] DENSITY_FACTORS = {1.0f, 0.85f, 0.65f, 0.4f, 0.0f};
    private static final int[] UPDATE_INTERVALS = {1, 2, 4, 8, Integer.MAX_VALUE};

    private boolean enabled = true;
    private OptimizationStage currentStage = OptimizationStage.NORMAL;
    private float renderDistanceFactor = 1.0f;
    private float densityFactor = 1.0f;
    private int updateInterval = 1;
    private boolean cloudsEnabled = true;
    private boolean forceDisabled;
    private int recoveryFrame;
    private static final int RECOVERY_FRAMES = 120;

    private boolean settingsSaved;
    private CloudStatus savedCloudStatus = null;

    @Override
    public void initialize() {
    }

    @Override
    public ModuleType getType() {
        return ModuleType.CLOUDS;
    }

    @Override
    public String getName() {
        return "Cloud Optimizer";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void enable() {
        this.enabled = true;
    }

    @Override
    public void disable() {
        this.enabled = false;
        restoreClouds();
    }

    @Override
    public long getEstimatedSavings() {
        if (!cloudsEnabled) {
            return 50L;
        }
        long saving = 0L;
        saving += (long) ((1.0f - renderDistanceFactor) * 40L);
        saving += (long) ((1.0f - densityFactor) * 40L);
        return Math.min(80L, saving);
    }

    @Override
    public void update(OptimizationStage stage) {
        OptimizationStage previous = currentStage;
        currentStage = stage;
        int ordinal = stage.ordinal();

        if (stage == OptimizationStage.NORMAL) {
            restoreClouds();
        } else {
            saveSettings();
        }

        if (stage == OptimizationStage.EMERGENCY) {
            forceDisabled = true;
            cloudsEnabled = false;
            applyCloudState(false);
            if (previous != stage) {
                onStageChange(previous, stage);
            }
            return;
        }
        if (forceDisabled) {
            recoveryFrame++;
            if (recoveryFrame < RECOVERY_FRAMES) {
                float t = (float) recoveryFrame / RECOVERY_FRAMES;
                renderDistanceFactor = t * RENDER_DISTANCE_FACTORS[Math.min(ordinal, RENDER_DISTANCE_FACTORS.length - 1)];
                densityFactor = t * DENSITY_FACTORS[Math.min(ordinal, DENSITY_FACTORS.length - 1)];
                updateInterval = Math.max(1, UPDATE_INTERVALS[Math.min(ordinal, UPDATE_INTERVALS.length - 1)] / 2);
                if (stage != previous) {
                    onStageChange(previous, stage);
                }
                return;
            }
            forceDisabled = false;
            recoveryFrame = 0;
            applyCloudState(true);
        }
        renderDistanceFactor = RENDER_DISTANCE_FACTORS[Math.min(ordinal, RENDER_DISTANCE_FACTORS.length - 1)];
        densityFactor = DENSITY_FACTORS[Math.min(ordinal, DENSITY_FACTORS.length - 1)];
        updateInterval = UPDATE_INTERVALS[Math.min(ordinal, UPDATE_INTERVALS.length - 1)];
        cloudsEnabled = renderDistanceFactor > 0.0f && densityFactor > 0.0f;
        if (stage != previous && !forceDisabled) {
            onStageChange(previous, stage);
        }
    }

    public void onStageChange(OptimizationStage oldStage, OptimizationStage newStage) {
        Pulse.LOGGER.debug("CloudOptimizer stage changed from {} to {}", oldStage, newStage);
        if (newStage == OptimizationStage.EMERGENCY && oldStage != OptimizationStage.EMERGENCY) {
            recoveryFrame = 0;
        }
    }

    public void reset() {
        currentStage = OptimizationStage.NORMAL;
        renderDistanceFactor = 1.0f;
        densityFactor = 1.0f;
        updateInterval = 1;
        cloudsEnabled = true;
        forceDisabled = false;
        recoveryFrame = 0;
        restoreClouds();
    }

    @Override
    public void recover() {
        reset();
    }

    @Override
    public double getCurrentImpact() {
        return 0.0;
    }

    @Override
    public ModulePriority getPriority() {
        return ModulePriority.LOW;
    }

    @Override
    public void shutdown() {
        reset();
    }

    public boolean shouldRenderClouds() {
        return cloudsEnabled;
    }

    public float getCloudRenderDistanceFactor() {
        return renderDistanceFactor;
    }

    public float getCloudDensityFactor() {
        return densityFactor;
    }

    public int getCloudUpdateInterval() {
        return updateInterval;
    }

    public boolean isRecovering() {
        return forceDisabled && recoveryFrame > 0;
    }

    public float getRecoveryProgress() {
        if (!forceDisabled) {
            return 1.0f;
        }
        return Math.min(1.0f, (float) recoveryFrame / RECOVERY_FRAMES);
    }

    private void saveSettings() {
        Options options = Minecraft.getInstance().options;
        CloudStatus current = options.cloudStatus().get();
        if (current == CloudStatus.OFF) {
            return;
        }
        if (!settingsSaved) {
            savedCloudStatus = current;
            settingsSaved = true;
        }
        if (savedCloudStatus != current) {
            savedCloudStatus = current;
        }
    }

    private void restoreSettings() {
        if (settingsSaved && savedCloudStatus != null) {
            applyCloudState(savedCloudStatus);
        }
        savedCloudStatus = null;
        settingsSaved = false;
        recoveryFrame = 0;
        forceDisabled = false;
    }

    private void applyCloudState(boolean enabled) {
        Options options = Minecraft.getInstance().options;
        if (enabled) {
            if (settingsSaved && savedCloudStatus != null) {
                options.cloudStatus().set(savedCloudStatus);
            } else {
                options.cloudStatus().set(CloudStatus.FANCY);
            }
        } else {
            options.cloudStatus().set(CloudStatus.OFF);
        }
    }

    private void applyCloudState(CloudStatus status) {
        Options options = Minecraft.getInstance().options;
        if (options != null) {
            options.cloudStatus().set(status);
        }
    }

    private void restoreClouds() {
        restoreSettings();
    }

    public boolean shouldSkipCloudUpdate(int frameCount) {
        if (currentStage == OptimizationStage.NORMAL) {
            return false;
        }
        if (updateInterval <= 1) {
            return false;
        }
        return frameCount % updateInterval != 0;
    }
}
