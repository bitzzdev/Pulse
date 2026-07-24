package io.github.pulse.api;

public interface AdaptiveModule {
    void initialize();
    void update(OptimizationStage stage);
    void enable();
    void disable();
    void recover();
    void shutdown();

    String getName();
    long getEstimatedSavings();
    double getCurrentImpact();

    ModulePriority getPriority();
    boolean isEnabled();
    ModuleType getType();

    enum ModulePriority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}