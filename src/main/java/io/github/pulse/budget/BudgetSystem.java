package io.github.pulse.budget;

import io.github.pulse.performance.PerformanceEngine;
import io.github.pulse.util.Constants;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BudgetSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger(BudgetSystem.class);

    private final PerformanceEngine performanceEngine;
    private int targetFps;
    private double frameBudgetMs;
    private double estimatedFrameCostMs;
    private double costTrend;
    private final ConcurrentMap<String, Double> moduleSavings;
    private double historyCost;
    private int historyCount;
    private boolean initialized;

    public BudgetSystem() {
        this(PerformanceEngine.getInstance());
    }

    public BudgetSystem(PerformanceEngine performanceEngine) {
        this.performanceEngine = performanceEngine;
        this.targetFps = (int) Constants.TARGET_FPS;
        this.frameBudgetMs = 1000.0 / targetFps;
        this.estimatedFrameCostMs = frameBudgetMs;
        this.costTrend = 0.0;
        this.moduleSavings = new ConcurrentHashMap<>();
        this.historyCost = 0.0;
        this.historyCount = 0;
        this.initialized = false;
    }

    public void initialize() {
        if (initialized) return;
        initialized = true;
        LOGGER.info("BudgetSystem initialized with target {} FPS ({}ms budget)", targetFps, String.format("%.2f", frameBudgetMs));
    }

    public void shutdown() {
        if (!initialized) return;
        initialized = false;
        moduleSavings.clear();
        LOGGER.info("BudgetSystem shut down");
    }

    public void update() {
        if (!initialized) return;

        double currentFrameTime = performanceEngine.getFrameTime();
        estimatedFrameCostMs = currentFrameTime;

        historyCost += currentFrameTime;
        historyCount++;
        if (historyCount > Constants.WINDOW_60S) {
            int windowSize = Constants.WINDOW_60S;
            int halfWindow = windowSize / 2;
            if (historyCount > windowSize) {
                historyCost = 0.0;
                historyCount = 0;
            }
        }

        double avgCost = historyCount > 0 ? historyCost / historyCount : estimatedFrameCostMs;
        costTrend = (estimatedFrameCostMs - avgCost) / Math.max(avgCost, 1.0);
    }

    public void registerModuleSavings(String moduleId, double savingsMs) {
        moduleSavings.put(moduleId, Math.max(0.0, savingsMs));
    }

    public void unregisterModuleSavings(String moduleId) {
        moduleSavings.remove(moduleId);
    }

    public double getFrameBudgetMs() {
        return frameBudgetMs;
    }

    public int getTargetFps() {
        return targetFps;
    }

    public void setTargetFps(int targetFps) {
        if (targetFps < 1) return;
        this.targetFps = targetFps;
        this.frameBudgetMs = 1000.0 / targetFps;
    }

    public double getBudgetUsage() {
        double effectiveCost = Math.max(0.0, estimatedFrameCostMs - getEstimatedSavings());
        if (frameBudgetMs <= 0) return 1.0;
        return Math.min(1.0, effectiveCost / frameBudgetMs);
    }

    public double getEstimatedSavings() {
        double total = 0.0;
        for (double v : moduleSavings.values()) {
            total += v;
        }
        return total;
    }

    public double getRemainingBudget() {
        double effectiveCost = Math.max(0.0, estimatedFrameCostMs - getEstimatedSavings());
        return Math.max(0.0, frameBudgetMs - effectiveCost);
    }

    public boolean isOverBudget() {
        return getBudgetUsage() > 1.0;
    }

    public double getEstimatedFrameCostMs() {
        return estimatedFrameCostMs;
    }

    public double getCostTrend() {
        return costTrend;
    }

    public double predictFutureFrameCostMs(int ticksAhead) {
        return estimatedFrameCostMs * (1.0 + costTrend * ticksAhead);
    }

    public double predictBudgetUsage(int ticksAhead) {
        double predictedCost = predictFutureFrameCostMs(ticksAhead);
        double effectiveCost = Math.max(0.0, predictedCost - getEstimatedSavings());
        if (frameBudgetMs <= 0) return 1.0;
        return Math.min(1.0, effectiveCost / frameBudgetMs);
    }

    public boolean isInitialized() {
        return initialized;
    }
}
