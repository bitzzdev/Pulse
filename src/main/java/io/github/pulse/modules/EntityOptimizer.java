package io.github.pulse.modules;

import io.github.pulse.Pulse;
import io.github.pulse.api.AdaptiveModule;
import io.github.pulse.api.ModuleType;
import io.github.pulse.api.OptimizationStage;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

public class EntityOptimizer implements AdaptiveModule {

    private static final double[] ANIMATION_LOD_DISTANCES = {Double.MAX_VALUE, 48.0, 32.0, 24.0, 16.0};
    private static final float[] ANIMATION_FREQUENCY_FACTORS = {1.0f, 0.9f, 0.7f, 0.5f, 0.25f};
    private static final float[] INTERPOLATION_RATES = {1.0f, 0.9f, 0.75f, 0.5f, 0.3f};
    private static final double PRIORITY_DISTANCE = 16.0;

    private boolean enabled = true;
    private OptimizationStage currentStage = OptimizationStage.NORMAL;
    private double animationLODDistance = Double.MAX_VALUE;
    private float animationFrequencyFactor = 1.0f;
    private boolean skipCosmetics;
    private float interpolationRate = 1.0f;
    private int animationTickSkip;

    private final Set<Integer> highPriorityEntities = new HashSet<>();

    @Override
    public void initialize() {
    }

    @Override
    public ModuleType getType() {
        return ModuleType.ENTITIES;
    }

    @Override
    public String getName() {
        return "Entity Optimizer";
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
        if (animationFrequencyFactor < 1.0f) {
            saving += (long) ((1.0f - animationFrequencyFactor) * 50L);
        }
        if (skipCosmetics) {
            saving += 15L;
        }
        if (interpolationRate < 1.0f) {
            saving += (long) ((1.0f - interpolationRate) * 20L);
        }
        return Math.min(85L, saving);
    }

    @Override
    public void update(OptimizationStage stage) {
        OptimizationStage previous = currentStage;
        currentStage = stage;
        int ordinal = stage.ordinal();
        animationLODDistance = ANIMATION_LOD_DISTANCES[Math.min(ordinal, ANIMATION_LOD_DISTANCES.length - 1)];
        animationFrequencyFactor = ANIMATION_FREQUENCY_FACTORS[Math.min(ordinal, ANIMATION_FREQUENCY_FACTORS.length - 1)];
        interpolationRate = INTERPOLATION_RATES[Math.min(ordinal, INTERPOLATION_RATES.length - 1)];
        skipCosmetics = ordinal >= OptimizationStage.MODERATE.ordinal();
        animationTickSkip = Math.max(1, (int) ((1.0f - animationFrequencyFactor) * 10) + 1);
        updateEntityRenderDistance();
        if (stage != previous) {
            onStageChange(previous, stage);
        }
    }

    public void onStageChange(OptimizationStage oldStage, OptimizationStage newStage) {
        Pulse.LOGGER.debug("EntityOptimizer stage changed from {} to {}", oldStage, newStage);
        if (newStage == OptimizationStage.EMERGENCY) {
            skipCosmetics = true;
        }
    }

    public void reset() {
        currentStage = OptimizationStage.NORMAL;
        animationLODDistance = Double.MAX_VALUE;
        animationFrequencyFactor = 1.0f;
        skipCosmetics = false;
        interpolationRate = 1.0f;
        animationTickSkip = 1;
        highPriorityEntities.clear();
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

    public boolean shouldRenderEntity(Entity entity) {
        if (currentStage == OptimizationStage.NORMAL) {
            return true;
        }
        return true;
    }

    public boolean shouldRenderCosmetics(Entity entity) {
        if (currentStage == OptimizationStage.NORMAL) {
            return true;
        }
        if (!skipCosmetics) {
            return true;
        }
        double distance = getEntityDistance(entity);
        if (distance < PRIORITY_DISTANCE) {
            return true;
        }
        return highPriorityEntities.contains(entity.getId());
    }

    public boolean shouldReduceAnimation(Entity entity) {
        if (currentStage == OptimizationStage.NORMAL) {
            return false;
        }
        if (highPriorityEntities.contains(entity.getId())) {
            return false;
        }
        double distance = getEntityDistance(entity);
        return distance > animationLODDistance;
    }

    public int getAnimationInterval(Entity entity) {
        if (currentStage == OptimizationStage.NORMAL) {
            return 1;
        }
        if (highPriorityEntities.contains(entity.getId())) {
            return 1;
        }
        double distance = getEntityDistance(entity);
        if (distance > animationLODDistance) {
            int interval = animationTickSkip;
            if (distance > animationLODDistance * 2.0) {
                interval = Math.max(2, interval * 2);
            }
            return interval;
        }
        return 1;
    }

    public float getInterpolationRate(Entity entity) {
        if (currentStage == OptimizationStage.NORMAL) {
            return 1.0f;
        }
        if (highPriorityEntities.contains(entity.getId())) {
            return 1.0f;
        }
        double distance = getEntityDistance(entity);
        if (distance > animationLODDistance) {
            return interpolationRate;
        }
        return 1.0f;
    }

    public void markHighPriority(Entity entity) {
        highPriorityEntities.add(entity.getId());
    }

    public void clearHighPriority() {
        highPriorityEntities.clear();
    }

    public boolean isHighPriority(Entity entity) {
        return highPriorityEntities.contains(entity.getId());
    }

    public void updatePriorities(Vec3 cameraPos, Entity crosshairTarget) {
        highPriorityEntities.clear();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity == null) continue;
            Vec3 pos = entity.position();
            double dist = pos.distanceTo(cameraPos);
            if (dist < PRIORITY_DISTANCE) {
                highPriorityEntities.add(entity.getId());
            }
        }
        if (crosshairTarget != null) {
            highPriorityEntities.add(crosshairTarget.getId());
        }
    }

    public double getAnimationLODDistance() {
        return animationLODDistance;
    }

    public float getAnimationFrequencyFactor() {
        return animationFrequencyFactor;
    }

    public boolean shouldSkipCosmetics() {
        return skipCosmetics;
    }

    private double getEntityDistance(Entity entity) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.cameraEntity != null) {
            return minecraft.cameraEntity.distanceTo(entity);
        }
        if (minecraft.player != null) {
            return minecraft.player.distanceTo(entity);
        }
        return Double.MAX_VALUE;
    }

    private void updateEntityRenderDistance() {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (dispatcher != null && animationLODDistance < Double.MAX_VALUE) {
            float renderDistance = (float) Math.max(16.0, animationLODDistance * 1.5);
        }
    }
}
