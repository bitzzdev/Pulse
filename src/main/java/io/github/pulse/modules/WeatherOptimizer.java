package io.github.pulse.modules;

import io.github.pulse.Pulse;
import io.github.pulse.api.AdaptiveModule;
import io.github.pulse.api.ModuleType;
import io.github.pulse.api.OptimizationStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

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
    private int previousRainParticles = 0;

    private boolean settingsSaved;
    private double savedRainLevel = -1;
    private double savedThunderLevel = -1;
    private float lastAppliedRain = -1;
    private float lastAppliedThunder = -1;

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
        restoreWeather();
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

        if (stage == OptimizationStage.NORMAL) {
            restoreWeather();
        } else {
            saveSettings();
        }

        int ordinal = stage.ordinal();
        particleCountFactor = PARTICLE_COUNT_FACTORS[Math.min(ordinal, PARTICLE_COUNT_FACTORS.length - 1)];
        renderDistanceFactor = RENDER_DISTANCE_FACTORS[Math.min(ordinal, RENDER_DISTANCE_FACTORS.length - 1)];
        thunderIntensityFactor = THUNDER_INTENSITY_FACTORS[Math.min(ordinal, THUNDER_INTENSITY_FACTORS.length - 1)];
        weatherEffectsEnabled = stage != OptimizationStage.EMERGENCY;
        applyWeatherSettings();
        if (stage != previous) {
            onStageChange(previous, stage);
        }
    }

    public void onStageChange(OptimizationStage oldStage, OptimizationStage newStage) {
        Pulse.LOGGER.debug("WeatherOptimizer stage changed from {} to {}", oldStage, newStage);
        if (newStage == OptimizationStage.EMERGENCY) {
            clearWeatherEffects();
        }
    }

    public void reset() {
        currentStage = OptimizationStage.NORMAL;
        particleCountFactor = 1.0f;
        renderDistanceFactor = 1.0f;
        thunderIntensityFactor = 1.0f;
        weatherEffectsEnabled = true;
        restoreWeather();
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

    private void saveSettings() {
        ClientLevel level = getClientLevel();
        if (level == null) return;

        float currentRain = level.getRainLevel(1.0f);
        float currentThunder = level.getThunderLevel(1.0f);

        boolean rainChanged = lastAppliedRain < 0
            || Math.abs(currentRain - lastAppliedRain) > 0.001f;
        boolean thunderChanged = lastAppliedThunder < 0
            || Math.abs(currentThunder - lastAppliedThunder) > 0.001f;

        if (currentRain > 0.05f && rainChanged) {
            savedRainLevel = currentRain;
        }
        if (currentThunder > 0.05f && thunderChanged) {
            savedThunderLevel = currentThunder;
        }
        if (currentRain > 0.05f || currentThunder > 0.05f) {
            settingsSaved = true;
        }
    }

    private void restoreSettings() {
        if (settingsSaved) {
            ClientLevel level = getClientLevel();
            if (level != null) {
                if (savedRainLevel >= 0) {
                    level.setRainLevel((float) savedRainLevel);
                }
                if (savedThunderLevel >= 0) {
                    level.setThunderLevel((float) savedThunderLevel);
                }
            }
        }
        savedRainLevel = -1;
        savedThunderLevel = -1;
        settingsSaved = false;
        lastAppliedRain = -1;
        lastAppliedThunder = -1;
    }

    private void applyWeatherSettings() {
        ClientLevel level = getClientLevel();
        if (level == null || savedRainLevel < 0) {
            lastAppliedRain = -1;
            lastAppliedThunder = -1;
            return;
        }
        float weatherFactor = switch (currentStage) {
            case MINOR -> 0.5f;
            case MODERATE -> 0.25f;
            case HEAVY -> 0.1f;
            case EMERGENCY -> 0.0f;
            default -> 1.0f;
        };
        float targetRain = (float) savedRainLevel * weatherFactor;
        float targetThunder = (float) savedThunderLevel * weatherFactor;
        if (savedRainLevel > 0.2f) {
            targetRain = Math.max(targetRain, 0.15f);
        }
        level.setRainLevel(targetRain);
        level.setThunderLevel(targetThunder);
        lastAppliedRain = targetRain;
        lastAppliedThunder = targetThunder;
    }

    private void clearWeatherEffects() {
        ClientLevel level = getClientLevel();
        if (level != null) {
            level.setRainLevel(0.0f);
            level.setThunderLevel(0.0f);
        }
    }

    private void restoreWeather() {
        restoreSettings();
    }

    public boolean shouldSkipWeatherParticle(double distance) {
        if (currentStage == OptimizationStage.NORMAL) {
            return false;
        }
        double maxDist = 64.0 * renderDistanceFactor;
        return distance > maxDist;
    }

    private ClientLevel getClientLevel() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level instanceof ClientLevel) {
            return (ClientLevel) minecraft.level;
        }
        return null;
    }
}
