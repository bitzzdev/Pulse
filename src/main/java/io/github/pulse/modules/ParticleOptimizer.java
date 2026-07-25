package io.github.pulse.modules;

import io.github.pulse.Pulse;
import io.github.pulse.api.AdaptiveModule;
import io.github.pulse.api.ModuleType;
import io.github.pulse.api.OptimizationStage;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParticleOptimizer implements AdaptiveModule {

    private static final float[] BUDGET_FACTORS = {1.0f, 0.8f, 0.6f, 0.4f, 0.2f};
    private static final float[] SPAWN_PROBABILITIES = {1.0f, 0.85f, 0.65f, 0.45f, 0.2f};
    private static final float[] LIFETIME_FACTORS = {1.0f, 0.9f, 0.75f, 0.55f, 0.3f};
    private static final int[] UPDATE_SKIP_INTERVALS = {1, 2, 3, 5, 10};
    private static final double DEFAULT_MERGE_RADIUS = 0.5;
    private static final double DEFAULT_DISTANCE_THRESHOLD = 32.0;

    private boolean enabled = true;
    private OptimizationStage currentStage = OptimizationStage.NORMAL;
    private float budgetFactor = 1.0f;
    private float spawnProbability = 1.0f;
    private float lifetimeFactor = 1.0f;
    private int updateSkipInterval = 1;
    private double mergeRadius = DEFAULT_MERGE_RADIUS;
    private double distanceThreshold = DEFAULT_DISTANCE_THRESHOLD;
    private int maxParticles = 0;

    private final Set<Particle> mergedParticles = new HashSet<>();
    private final Set<String> criticalParticleTypes = new HashSet<>();

    private boolean settingsSaved;

    public ParticleOptimizer() {
        criticalParticleTypes.add("effect");
        criticalParticleTypes.add("instant_effect");
        criticalParticleTypes.add("entity_effect");
        criticalParticleTypes.add("critical_hit");
        criticalParticleTypes.add("damage_indicator");
        criticalParticleTypes.add("hearts");
        criticalParticleTypes.add("enchant");
        criticalParticleTypes.add("totem_of_undying");
    }

    @Override
    public void initialize() {
    }

    @Override
    public ModuleType getType() {
        return ModuleType.PARTICLES;
    }

    @Override
    public String getName() {
        return "Particle Optimizer";
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
        restoreSettings();
    }

    @Override
    public long getEstimatedSavings() {
        return (long) ((1.0f - budgetFactor) * 80L + (1.0f - spawnProbability) * 20L);
    }

    @Override
    public void update(OptimizationStage stage) {
        OptimizationStage previous = currentStage;
        currentStage = stage;

        if (stage == OptimizationStage.NORMAL) {
            restoreSettings();
        } else {
            saveSettings();
        }

        int ordinal = stage.ordinal();
        budgetFactor = BUDGET_FACTORS[Math.min(ordinal, BUDGET_FACTORS.length - 1)];
        spawnProbability = SPAWN_PROBABILITIES[Math.min(ordinal, SPAWN_PROBABILITIES.length - 1)];
        lifetimeFactor = LIFETIME_FACTORS[Math.min(ordinal, LIFETIME_FACTORS.length - 1)];
        updateSkipInterval = UPDATE_SKIP_INTERVALS[Math.min(ordinal, UPDATE_SKIP_INTERVALS.length - 1)];
        if (stage == OptimizationStage.NORMAL) {
            mergeRadius = DEFAULT_MERGE_RADIUS;
            distanceThreshold = DEFAULT_DISTANCE_THRESHOLD;
        } else {
            mergeRadius = DEFAULT_MERGE_RADIUS * (1.0 + (1.0 - budgetFactor) * 2.0);
            distanceThreshold = DEFAULT_DISTANCE_THRESHOLD * budgetFactor;
        }
        maxParticles = (int) (getBaseMaxParticles() * budgetFactor);

        if (stage != OptimizationStage.NORMAL) {
            applyParticleSettings();
        }

        if (stage != previous) {
            onStageChange(previous, stage);
        }
    }

    public void onStageChange(OptimizationStage oldStage, OptimizationStage newStage) {
        Pulse.LOGGER.debug("ParticleOptimizer stage changed from {} to {}", oldStage, newStage);
        if (newStage == OptimizationStage.EMERGENCY) {
            clearExcessParticles();
        }
    }

    public void reset() {
        currentStage = OptimizationStage.NORMAL;
        budgetFactor = 1.0f;
        spawnProbability = 1.0f;
        lifetimeFactor = 1.0f;
        updateSkipInterval = 1;
        mergeRadius = DEFAULT_MERGE_RADIUS;
        distanceThreshold = DEFAULT_DISTANCE_THRESHOLD;
        mergedParticles.clear();
        restoreSettings();
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

    public boolean shouldRenderParticle(Particle particle) {
        if (currentStage == OptimizationStage.NORMAL) {
            return true;
        }
        if (isCriticalParticle(particle)) {
            return true;
        }
        if (isParticleHidden(particle)) {
            return false;
        }
        return true;
    }

    public boolean shouldSkipParticleUpdate(Particle particle, int distance) {
        if (currentStage == OptimizationStage.NORMAL) {
            return false;
        }
        if (isCriticalParticle(particle)) {
            return false;
        }
        if (distance > distanceThreshold && updateSkipInterval > 1) {
            return (particle.hashCode() % updateSkipInterval) != 0;
        }
        return false;
    }

    public boolean shouldSpawnParticle(String particleType) {
        if (currentStage == OptimizationStage.NORMAL) {
            return true;
        }
        if (criticalParticleTypes.contains(particleType)) {
            return true;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            int currentParticleCount = getParticleCount();
            if (currentParticleCount >= maxParticles) {
                return false;
            }
        }
        return minecraft.level.getRandom().nextFloat() < spawnProbability;
    }

    public boolean tryMergeParticles(Particle a, Particle b) {
        return false;
    }

    public float getParticleAlpha(Particle particle, double distance) {
        if (currentStage == OptimizationStage.NORMAL) {
            return 1.0f;
        }
        if (isCriticalParticle(particle)) {
            return 1.0f;
        }
        if (distance > distanceThreshold) {
            float fade = (float) (1.0 - (distance - distanceThreshold) / distanceThreshold);
            return Math.max(0.1f, Math.min(1.0f, fade * budgetFactor));
        }
        return 1.0f;
    }

    public float getLifetimeMultiplier() {
        return lifetimeFactor;
    }

    public int getMaxParticles() {
        return maxParticles;
    }

    public float getSpawnProbability() {
        return spawnProbability;
    }

    public int getUpdateSkipInterval() {
        return updateSkipInterval;
    }

    public double getDistanceThreshold() {
        return distanceThreshold;
    }

    public boolean isParticleHidden(Particle particle) {
        return false;
    }

    private boolean isCriticalParticle(Particle particle) {
        String name = particle.getClass().getSimpleName().toLowerCase();
        for (String type : criticalParticleTypes) {
            if (name.contains(type)) {
                return true;
            }
        }
        return false;
    }

    private int getParticleCount() {
        return 0;
    }

    private int getBaseMaxParticles() {
        return 4000;
    }

    private void clearExcessParticles() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        mergedParticles.clear();
    }

    private void saveSettings() {
        settingsSaved = true;
    }

    private void restoreSettings() {
        settingsSaved = false;
    }

    private void applyParticleSettings() {
    }
}
