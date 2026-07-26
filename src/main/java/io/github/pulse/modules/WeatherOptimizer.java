package io.github.pulse.modules;

import io.github.pulse.Pulse;
import io.github.pulse.api.AdaptiveModule;
import io.github.pulse.api.ModuleType;
import io.github.pulse.api.OptimizationStage;

public class WeatherOptimizer implements AdaptiveModule {

    private static final float[] PARTICLE_COUNT_FACTORS = {1.0f, 0.75f, 0.55f, 0.35f, 0.1f};
    private static final float[] RENDER_DISTANCE_FACTORS = {1.0f, 0.8f, 0.6f, 0.4f, 0.15f};
    private static final float[] THUNDER_INTENSITY_FACTORS = {1.0f, 0.9f, 0.7f, 0.4f, 0.0f};

    private boolean enabled = true;
    private OptimizationStage currentStage = OptimizationStage.NORMAL;
    private float particleCountFactor = 1.0f;
    private float renderDistanceFactor = 1.0f;
    private float thunderIntensityFactor = 1.0f;
    private boolean weatherEffectsEnabled = true;

    @Override
    public void initialize() {
    }

    @Override
    public ModuleType getType() {
        return ModuleType.WEATHER;
    }

    @Override
    public String getName() {
        return "Weather Optimizer";
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
    }

    @Override
    public long getEstimatedSavings() {
        long saving = 0L;
        saving += (long) ((1.0f - particleCountFactor) * 40L);
        saving += (long) ((1.0f - renderDistanceFactor) * 20L);
        saving += (long) ((1.0f - thunderIntensityFactor) * 10L);
        return Math.min(70L, saving);
    }

    @Override
    public void update(OptimizationStage stage) {
        OptimizationStage previous = currentStage;
        currentStage = stage;

        int ordinal = stage.ordinal();
        particleCountFactor = PARTICLE_COUNT_FACTORS[Math.min(ordinal, PARTICLE_COUNT_FACTORS.length - 1)];
        renderDistanceFactor = RENDER_DISTANCE_FACTORS[Math.min(ordinal, RENDER_DISTANCE_FACTORS.length - 1)];
        thunderIntensityFactor = THUNDER_INTENSITY_FACTORS[Math.min(ordinal, THUNDER_INTENSITY_FACTORS.length - 1)];
        weatherEffectsEnabled = stage != OptimizationStage.EMERGENCY;
        if (stage != previous) {
            onStageChange(previous, stage);
        }
    }

    public void onStageChange(OptimizationStage oldStage, OptimizationStage newStage) {
        Pulse.LOGGER.debug("WeatherOptimizer stage changed from {} to {}", oldStage, newStage);
    }

    public void reset() {
        currentStage = OptimizationStage.NORMAL;
        particleCountFactor = 1.0f;
        renderDistanceFactor = 1.0f;
        thunderIntensityFactor = 1.0f;
        weatherEffectsEnabled = true;
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
        return ModulePriority.MEDIUM;
    }

    @Override
    public void shutdown() {
        reset();
    }

    public boolean shouldRenderWeather() {
        return weatherEffectsEnabled && enabled;
    }

    public int getAdjustedParticleCount(int baseCount) {
        if (currentStage == OptimizationStage.NORMAL) {
            return baseCount;
        }
        return Math.max(1, (int) (baseCount * particleCountFactor));
    }

    public float getSpawnChance() {
        if (currentStage == OptimizationStage.NORMAL) {
            return 1.0f;
        }
        return particleCountFactor;
    }

    public float getRenderDistanceFactor() {
        return renderDistanceFactor;
    }

    public float getThunderIntensityFactor() {
        return thunderIntensityFactor;
    }

    public boolean shouldRenderThunder() {
        return thunderIntensityFactor > 0.0f;
    }

    public boolean shouldRenderLightningFlash() {
        if (currentStage == OptimizationStage.NORMAL) {
            return true;
        }
        if (currentStage == OptimizationStage.EMERGENCY) {
            return false;
        }
        return thunderIntensityFactor > 0.3f;
    }

    public float getLightningFlashIntensity() {
        return thunderIntensityFactor;
    }

    public boolean shouldSkipWeatherParticle(double distance) {
        if (currentStage == OptimizationStage.NORMAL) {
            return false;
        }
        double maxDist = 64.0 * renderDistanceFactor;
        return distance > maxDist;
    }

}
