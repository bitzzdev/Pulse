package io.github.pulse.modules;

import io.github.pulse.Pulse;
import io.github.pulse.api.AdaptiveModule;
import io.github.pulse.api.ModuleType;
import io.github.pulse.api.OptimizationStage;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class TransparencyOptimizer implements AdaptiveModule {

    private static final int[] SORT_INTERVALS = {1, 2, 4, 8, 16};
    private static final float[] TRANSPARENCY_DISTANCE_FACTORS = {1.0f, 0.9f, 0.75f, 0.5f, 0.3f};
    private static final double LEAF_DISTANCE_THRESHOLD = 24.0;

    private boolean enabled = true;
    private OptimizationStage currentStage = OptimizationStage.NORMAL;
    private int sortInterval = 1;
    private float transparencyDistanceFactor = 1.0f;
    private boolean resortTranslucency = true;
    private boolean optimizeLeafBlocks;
    private double leafOptimizationDistance = Double.MAX_VALUE;

    private int sortCounter;

    @Override
    public void initialize() {
    }

    @Override
    public ModuleType getType() {
        return ModuleType.TRANSPARENCY;
    }

    @Override
    public String getName() {
        return "Transparency Optimizer";
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
        if (sortInterval > 1) {
            saving += (long) ((1.0f - 1.0f / sortInterval) * 35L);
        }
        saving += (long) ((1.0f - transparencyDistanceFactor) * 30L);
        if (optimizeLeafBlocks) {
            saving += 15L;
        }
        return Math.min(70L, saving);
    }

    @Override
    public void update(OptimizationStage stage) {
        OptimizationStage previous = currentStage;
        currentStage = stage;
        int ordinal = stage.ordinal();
        sortInterval = SORT_INTERVALS[Math.min(ordinal, SORT_INTERVALS.length - 1)];
        transparencyDistanceFactor = TRANSPARENCY_DISTANCE_FACTORS[Math.min(ordinal, TRANSPARENCY_DISTANCE_FACTORS.length - 1)];
        resortTranslucency = sortInterval <= 1 || (sortCounter % sortInterval == 0);
        optimizeLeafBlocks = ordinal >= OptimizationStage.MODERATE.ordinal();
        if (optimizeLeafBlocks) {
            leafOptimizationDistance = LEAF_DISTANCE_THRESHOLD;
        } else {
            leafOptimizationDistance = Double.MAX_VALUE;
        }
        sortCounter++;
        if (stage != previous) {
            onStageChange(previous, stage);
        }
    }

    public void onStageChange(OptimizationStage oldStage, OptimizationStage newStage) {
        Pulse.LOGGER.debug("TransparencyOptimizer stage changed from {} to {}", oldStage, newStage);
        if (newStage.ordinal() > OptimizationStage.MODERATE.ordinal()) {
            sortCounter = 0;
        }
    }

    public void reset() {
        currentStage = OptimizationStage.NORMAL;
        sortInterval = 1;
        transparencyDistanceFactor = 1.0f;
        resortTranslucency = true;
        optimizeLeafBlocks = false;
        leafOptimizationDistance = Double.MAX_VALUE;
        sortCounter = 0;
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

    public boolean shouldResortTranslucency() {
        if (currentStage == OptimizationStage.NORMAL) {
            return true;
        }
        return resortTranslucency;
    }

    public int getSortInterval() {
        return sortInterval;
    }

    public boolean shouldRenderTransparentBlock(BlockPos pos, double distance) {
        if (currentStage == OptimizationStage.NORMAL) {
            return true;
        }
        double maxDistance = 256.0 * transparencyDistanceFactor;
        return distance <= maxDistance;
    }

    public boolean shouldSimplifyLeafBlock(BlockPos pos, double distance) {
        if (!optimizeLeafBlocks) {
            return false;
        }
        return distance > leafOptimizationDistance;
    }

    public boolean isLeafBlock(BlockState state) {
        return state.is(Blocks.OAK_LEAVES) || state.is(Blocks.SPRUCE_LEAVES)
                || state.is(Blocks.BIRCH_LEAVES) || state.is(Blocks.JUNGLE_LEAVES)
                || state.is(Blocks.ACACIA_LEAVES) || state.is(Blocks.DARK_OAK_LEAVES)
                || state.is(Blocks.MANGROVE_LEAVES) || state.is(Blocks.CHERRY_LEAVES)
                || state.is(Blocks.AZALEA_LEAVES) || state.is(Blocks.FLOWERING_AZALEA_LEAVES);
    }

    public float getTransparencyCutoff() {
        if (currentStage == OptimizationStage.NORMAL) {
            return 0.0f;
        }
        int ordinal = currentStage.ordinal();
        return ordinal * 0.025f;
    }

    public boolean shouldSkipTranslucentLayer(double distance) {
        if (currentStage == OptimizationStage.NORMAL) {
            return false;
        }
        double cutoff = 128.0 * transparencyDistanceFactor;
        return distance > cutoff;
    }

    public float getTransparencyDistanceFactor() {
        return transparencyDistanceFactor;
    }

    public double getLeafOptimizationDistance() {
        return leafOptimizationDistance;
    }

    public boolean shouldOptimizeLeaves() {
        return optimizeLeafBlocks;
    }
}
