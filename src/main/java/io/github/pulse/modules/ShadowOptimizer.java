package io.github.pulse.modules;

import io.github.pulse.Pulse;
import io.github.pulse.api.AdaptiveModule;
import io.github.pulse.api.ModuleType;
import io.github.pulse.api.OptimizationStage;
import net.minecraft.client.Minecraft;

public class ShadowOptimizer implements AdaptiveModule {

    private static final float[] SHADOW_DISTANCE_FACTORS = {1.0f, 0.85f, 0.65f, 0.4f, 0.15f};
    private static final float[] SHADOW_RESOLUTION_FACTORS = {1.0f, 0.9f, 0.7f, 0.5f, 0.25f};

    private boolean enabled = true;
    private OptimizationStage currentStage = OptimizationStage.NORMAL;
    private float shadowDistanceFactor = 1.0f;
    private float shadowResolutionFactor = 1.0f;
    private boolean shaderModsDetected;
    private boolean shaderPackActive;
    private boolean shadowBatching;

    @Override
    public ModuleType getType() {
        return ModuleType.SHADOWS;
    }

    @Override
    public String getName() {
        return "Shadow Optimizer";
    }

    @Override
    public boolean isEnabled() {
        return enabled && shaderModsDetected;
    }

    @Override
    public void enable() {
        this.enabled = true;
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    @Override
    public float getEstimatedSavings() {
        if (!shaderModsDetected || !shaderPackActive) {
            return 0.0f;
        }
        float saving = 0.0f;
        saving += (1.0f - shadowDistanceFactor) * 0.5f;
        saving += (1.0f - shadowResolutionFactor) * 0.3f;
        if (shadowBatching) {
            saving += 0.1f;
        }
        return Math.min(0.8f, saving);
    }

    @Override
    public void update(OptimizationStage stage) {
        OptimizationStage previous = currentStage;
        currentStage = stage;
        detectShaderMods();
        shaderPackActive = isShaderPackEnabled();
        if (!shaderModsDetected || !shaderPackActive) {
            if (stage != previous) {
                onStageChange(previous, stage);
            }
            return;
        }
        int ordinal = stage.ordinal();
        shadowDistanceFactor = SHADOW_DISTANCE_FACTORS[Math.min(ordinal, SHADOW_DISTANCE_FACTORS.length - 1)];
        shadowResolutionFactor = SHADOW_RESOLUTION_FACTORS[Math.min(ordinal, SHADOW_RESOLUTION_FACTORS.length - 1)];
        shadowBatching = ordinal >= OptimizationStage.MODERATE.ordinal();
        applyShadowSettings();
        if (stage != previous) {
            onStageChange(previous, stage);
        }
    }

    @Override
    public void onStageChange(OptimizationStage oldStage, OptimizationStage newStage) {
        Pulse.LOGGER.debug("ShadowOptimizer stage changed from {} to {}", oldStage, newStage);
        if (shaderModsDetected) {
            Pulse.LOGGER.debug("Shadow settings adjusted: distance={}, resolution={}",
                    shadowDistanceFactor, shadowResolutionFactor);
        }
    }

    @Override
    public void reset() {
        currentStage = OptimizationStage.NORMAL;
        shadowDistanceFactor = 1.0f;
        shadowResolutionFactor = 1.0f;
        shadowBatching = false;
        restoreDefaultShadows();
    }

    public float getShadowDistanceFactor() {
        return shadowDistanceFactor;
    }

    public float getShadowResolutionFactor() {
        return shadowResolutionFactor;
    }

    public boolean shouldBatchShadows() {
        return shadowBatching;
    }

    public boolean areShadersActive() {
        return shaderModsDetected && shaderPackActive;
    }

    public boolean isShaderModPresent() {
        return shaderModsDetected;
    }

    private void detectShaderMods() {
        shaderModsDetected = false;
        try {
            Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            shaderModsDetected = true;
            return;
        } catch (ClassNotFoundException e) {
        }
        try {
            Class.forName("optifine.Installer");
            shaderModsDetected = true;
            return;
        } catch (ClassNotFoundException e) {
        }
        try {
            Class.forName("net.optifine.shaders.Shaders");
            shaderModsDetected = true;
        } catch (ClassNotFoundException e) {
        }
    }

    private boolean isShaderPackEnabled() {
        if (!shaderModsDetected) {
            return false;
        }
        try {
            Class<?> irisApi = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object api = irisApi.getMethod("getInstance").invoke(null);
            boolean enabled = (boolean) irisApi.getMethod("isShaderPackActive").invoke(api);
            return enabled;
        } catch (Exception e) {
        }
        try {
            Class<?> shadersClass = Class.forName("net.optifine.shaders.Shaders");
            boolean enabled = (boolean) shadersClass.getField("shaderPackLoaded").get(null);
            return enabled;
        } catch (Exception e) {
        }
        return false;
    }

    private void applyShadowSettings() {
        if (!shaderModsDetected || !shaderPackActive) {
            return;
        }
        try {
            Class<?> irisApi = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object api = irisApi.getMethod("getInstance").invoke(null);
            Object shaderPack = irisApi.getMethod("getShaderPack").invoke(api);
            if (shaderPack != null) {
                Class<?> shaderPackClass = shaderPack.getClass();
                if (shaderPackClass.getMethod("setShadowDistance", float.class) != null) {
                    float baseDistance = 96.0f;
                    float newDistance = baseDistance * shadowDistanceFactor;
                    shaderPackClass.getMethod("setShadowDistance", float.class).invoke(shaderPack, newDistance);
                }
                if (shaderPackClass.getMethod("setShadowResolution", float.class) != null) {
                    float baseResolution = 1.0f;
                    float newResolution = baseResolution * shadowResolutionFactor;
                    shaderPackClass.getMethod("setShadowResolution", float.class).invoke(shaderPack, newResolution);
                }
            }
        } catch (Exception e) {
            Pulse.LOGGER.warn("Failed to apply shadow settings via Iris API: {}", e.getMessage());
        }
    }

    private void restoreDefaultShadows() {
        shadowDistanceFactor = 1.0f;
        shadowResolutionFactor = 1.0f;
        shadowBatching = false;
    }
}
