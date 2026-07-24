package io.github.pulse.adaptive;

import io.github.pulse.api.OptimizationStage;
import io.github.pulse.pressure.PressureEngine;
import io.github.pulse.util.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdaptiveManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdaptiveManager.class);

    private static final int THRESHOLD_NORMAL_MAX = 15;
    private static final int THRESHOLD_MINOR_MAX = 35;
    private static final int THRESHOLD_MODERATE_MAX = 55;
    private static final int THRESHOLD_HEAVY_MAX = 75;
    private static final int HYSTERESIS = 5;

    private final PressureEngine pressureEngine;
    private OptimizationStage currentStage;
    private OptimizationStage targetStage;
    private boolean recovering;
    private int recoveryTimerTicks;
    private int recoveryTicksRequired;
    private final List<StageTransitionListener> listeners;
    private boolean initialized;

    public AdaptiveManager() {
        this(PressureEngine.getInstance());
    }

    public AdaptiveManager(PressureEngine pressureEngine) {
        this.pressureEngine = pressureEngine;
        this.currentStage = OptimizationStage.NORMAL;
        this.targetStage = OptimizationStage.NORMAL;
        this.recovering = false;
        this.recoveryTimerTicks = 0;
        this.recoveryTicksRequired = 0;
        this.listeners = new CopyOnWriteArrayList<>();
        this.initialized = false;
    }

    public void initialize() {
        if (initialized) return;
        initialized = true;
        LOGGER.info("AdaptiveManager initialized, starting at {}", currentStage);
    }

    public void shutdown() {
        if (!initialized) return;
        initialized = false;
        listeners.clear();
        LOGGER.info("AdaptiveManager shut down");
    }

    public void update() {
        if (!initialized) return;

        pressureEngine.update();
        double score = pressureEngine.getPressureScore();
        OptimizationStage computed = stageFromScore(score);

        if (recovering) {
            recoveryTimerTicks++;
        }

        if (computed.ordinal() > currentStage.ordinal()) {
            handleIncrease(computed, score);
        } else if (computed.ordinal() < currentStage.ordinal()) {
            handleDecrease(computed, score);
        } else {
            if (recovering) {
                cancelRecovery();
            }
        }

        targetStage = computed;
    }

    private void handleIncrease(OptimizationStage computed, double score) {
        if (recovering) {
            cancelRecovery();
        }

        int nextOrdinal = currentStage.ordinal() + 1;
        OptimizationStage nextStage = OptimizationStage.values()[nextOrdinal];
        int threshold = stageUpperBound(currentStage);

        if (score > threshold + HYSTERESIS) {
            transitionTo(nextStage);
        }
    }

    private void handleDecrease(OptimizationStage computed, double score) {
        if (!recovering) {
            recovering = true;
            recoveryTimerTicks = 0;
            recoveryTicksRequired = getRecoveryTicks(currentStage);
            return;
        }

        if (recoveryTimerTicks >= recoveryTicksRequired) {
            int lowerBound = stageLowerBound(currentStage);
            if (score < lowerBound - HYSTERESIS) {
                int nextOrdinal = currentStage.ordinal() - 1;
                transitionTo(OptimizationStage.values()[nextOrdinal]);
                recovering = false;
                recoveryTimerTicks = 0;
                recoveryTicksRequired = 0;
            } else {
                recoveryTimerTicks = recoveryTicksRequired;
            }
        }
    }

    private void cancelRecovery() {
        recovering = false;
        recoveryTimerTicks = 0;
        recoveryTicksRequired = 0;
    }

    private void transitionTo(OptimizationStage newStage) {
        OptimizationStage oldStage = currentStage;
        currentStage = newStage;
        cancelRecovery();
        fireTransition(oldStage, newStage);
    }

    private void fireTransition(OptimizationStage from, OptimizationStage to) {
        LOGGER.info("Stage transition: {} -> {}", from, to);
        for (StageTransitionListener listener : listeners) {
            try {
                listener.onStageTransition(from, to);
            } catch (Exception e) {
                LOGGER.error("StageTransitionListener threw exception", e);
            }
        }
    }

    public void addListener(StageTransitionListener listener) {
        listeners.add(listener);
    }

    public void removeListener(StageTransitionListener listener) {
        listeners.remove(listener);
    }

    public OptimizationStage getCurrentStage() {
        return currentStage;
    }

    public OptimizationStage getTargetStage() {
        return targetStage;
    }

    public double getRecoveryProgress() {
        if (!recovering || recoveryTicksRequired <= 0) return 1.0;
        return Math.min(1.0, (double) recoveryTimerTicks / recoveryTicksRequired);
    }

    public boolean isRecovering() {
        return recovering;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public static OptimizationStage stageFromScore(double score) {
        if (score <= THRESHOLD_NORMAL_MAX) return OptimizationStage.NORMAL;
        if (score <= THRESHOLD_MINOR_MAX) return OptimizationStage.MINOR;
        if (score <= THRESHOLD_MODERATE_MAX) return OptimizationStage.MODERATE;
        if (score <= THRESHOLD_HEAVY_MAX) return OptimizationStage.HEAVY;
        return OptimizationStage.EMERGENCY;
    }

    private static int stageUpperBound(OptimizationStage stage) {
        return switch (stage) {
            case NORMAL -> THRESHOLD_NORMAL_MAX;
            case MINOR -> THRESHOLD_MINOR_MAX;
            case MODERATE -> THRESHOLD_MODERATE_MAX;
            case HEAVY -> THRESHOLD_HEAVY_MAX;
            case EMERGENCY -> 100;
        };
    }

    private static int stageLowerBound(OptimizationStage stage) {
        return switch (stage) {
            case NORMAL -> 0;
            case MINOR -> THRESHOLD_NORMAL_MAX + 1;
            case MODERATE -> THRESHOLD_MINOR_MAX + 1;
            case HEAVY -> THRESHOLD_MODERATE_MAX + 1;
            case EMERGENCY -> THRESHOLD_HEAVY_MAX + 1;
        };
    }

    private static int getRecoveryTicks(OptimizationStage stage) {
        long ms = switch (stage) {
            case NORMAL -> 0L;
            case MINOR -> Constants.RECOVERY_TIME_MINOR_MS;
            case MODERATE -> Constants.RECOVERY_TIME_MODERATE_MS;
            case HEAVY -> Constants.RECOVERY_TIME_HEAVY_MS;
            case EMERGENCY -> Constants.RECOVERY_TIME_EMERGENCY_MS;
        };
        return (int) (ms / 50L);
    }

    @FunctionalInterface
    public interface StageTransitionListener {
        void onStageTransition(OptimizationStage from, OptimizationStage to);
    }
}
