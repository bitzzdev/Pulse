package io.github.pulse.modules;

import io.github.pulse.Pulse;
import io.github.pulse.api.AdaptiveModule;
import io.github.pulse.api.ModuleType;
import io.github.pulse.api.OptimizationStage;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AnimationOptimizer implements AdaptiveModule {

    private static final double[] ANIMATION_DISTANCE_THRESHOLDS = {Double.MAX_VALUE, 48.0, 32.0, 20.0, 12.0};
    private static final int[] INTERPOLATION_INTERVALS = {1, 2, 3, 5, 10};
    private static final float[] NON_CRITICAL_FACTORS = {1.0f, 0.9f, 0.7f, 0.4f, 0.15f};

    private boolean enabled = true;
    private OptimizationStage currentStage = OptimizationStage.NORMAL;
    private double animationDistanceThreshold = Double.MAX_VALUE;
    private int interpolationInterval = 1;
    private float nonCriticalFactor = 1.0f;
    private boolean skipOffScreenAnimations;
    private int frameCounter;

    @Override
    public void initialize() {
    }

    @Override
    public ModuleType getType() {
        return ModuleType.ANIMATIONS;
    }

    @Override
    public String getName() {
        return "Animation Optimizer";
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
        if (animationDistanceThreshold < Double.MAX_VALUE) {
            saving += 30L;
        }
        if (interpolationInterval > 1) {
            saving += (long) ((1.0f - 1.0f / interpolationInterval) * 30L);
        }
        if (skipOffScreenAnimations) {
            saving += 15L;
        }
        saving += (long) ((1.0f - nonCriticalFactor) * 20L);
        return Math.min(80L, saving);
    }

    @Override
    public void update(OptimizationStage stage) {
        OptimizationStage previous = currentStage;
        currentStage = stage;
        int ordinal = stage.ordinal();
        animationDistanceThreshold = ANIMATION_DISTANCE_THRESHOLDS[Math.min(ordinal, ANIMATION_DISTANCE_THRESHOLDS.length - 1)];
        interpolationInterval = INTERPOLATION_INTERVALS[Math.min(ordinal, INTERPOLATION_INTERVALS.length - 1)];
        nonCriticalFactor = NON_CRITICAL_FACTORS[Math.min(ordinal, NON_CRITICAL_FACTORS.length - 1)];
        skipOffScreenAnimations = ordinal >= OptimizationStage.MODERATE.ordinal();
        frameCounter++;
        if (stage != previous) {
            onStageChange(previous, stage);
        }
    }

    @Override
    public void onStageChange(OptimizationStage oldStage, OptimizationStage newStage) {
        Pulse.LOGGER.debug("AnimationOptimizer stage changed from {} to {}", oldStage, newStage);
        if (newStage == OptimizationStage.EMERGENCY) {
            frameCounter = 0;
        }
    }

    @Override
    public void reset() {
        currentStage = OptimizationStage.NORMAL;
        animationDistanceThreshold = Double.MAX_VALUE;
        interpolationInterval = 1;
        nonCriticalFactor = 1.0f;
        skipOffScreenAnimations = false;
        frameCounter = 0;
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

    public boolean shouldAnimateEntity(Entity entity) {
        if (currentStage == OptimizationStage.NORMAL) {
            return true;
        }
        if (entity == null) {
            return false;
        }
        double distance = getEntityDistance(entity);
        if (distance > animationDistanceThreshold) {
            return false;
        }
        if (skipOffScreenAnimations && !isEntityOnScreen(entity)) {
            return false;
        }
        return true;
    }

    public boolean shouldSkipAnimationFrame(Entity entity) {
        if (currentStage == OptimizationStage.NORMAL) {
            return false;
        }
        if (interpolationInterval <= 1) {
            return false;
        }
        if (entity == null) {
            return true;
        }
        double distance = getEntityDistance(entity);
        if (distance <= animationDistanceThreshold) {
            return false;
        }
        return (entity.getId() + frameCounter) % interpolationInterval != 0;
    }

    public int getInterpolationInterval(Entity entity) {
        if (currentStage == OptimizationStage.NORMAL) {
            return 1;
        }
        if (entity == null) {
            return interpolationInterval;
        }
        double distance = getEntityDistance(entity);
        if (distance > animationDistanceThreshold) {
            return interpolationInterval;
        }
        return 1;
    }

    public float getAnimationQualityFactor(Entity entity) {
        if (currentStage == OptimizationStage.NORMAL) {
            return 1.0f;
        }
        if (entity == null) {
            return nonCriticalFactor;
        }
        double distance = getEntityDistance(entity);
        if (distance > animationDistanceThreshold) {
            return nonCriticalFactor;
        }
        return 1.0f;
    }

    public boolean shouldSkipNonCriticalAnimations() {
        if (currentStage == OptimizationStage.NORMAL) {
            return false;
        }
        return currentStage == OptimizationStage.EMERGENCY;
    }

    public boolean isEntityOnScreen(Entity entity) {
        if (entity == null) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.levelRenderer == null) {
            return true;
        }
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Frustum frustum = minecraft.levelRenderer.getFrustum();
        if (frustum == null) {
            return true;
        }
        AABB bounds = entity.getBoundingBox();
        return frustum.isVisible(bounds);
    }

    public boolean shouldUpdateCuller(int frameCount) {
        if (currentStage == OptimizationStage.NORMAL) {
            return true;
        }
        int interval = Math.max(1, interpolationInterval);
        return frameCount % interval == 0;
    }

    public double getAnimationDistanceThreshold() {
        return animationDistanceThreshold;
    }

    public int getInterpolationInterval() {
        return interpolationInterval;
    }

    public float getNonCriticalFactor() {
        return nonCriticalFactor;
    }

    public boolean shouldSkipOffScreen() {
        return skipOffScreenAnimations;
    }

    private double getEntityDistance(Entity entity) {
        if (entity == null) {
            return Double.MAX_VALUE;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.cameraEntity != null) {
            return minecraft.cameraEntity.distanceTo(entity);
        }
        if (minecraft.player != null) {
            return minecraft.player.distanceTo(entity);
        }
        return 0.0;
    }
}
