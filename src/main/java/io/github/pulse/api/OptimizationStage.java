package io.github.pulse.api;

public enum OptimizationStage {
    NORMAL(0, "Normal rendering"),
    MINOR(1, "Minor cosmetic optimizations"),
    MODERATE(2, "Moderate optimizations"),
    HEAVY(3, "Heavy cosmetic optimization"),
    EMERGENCY(4, "Emergency optimization");

    private final int level;
    private final String description;

    OptimizationStage(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() { return level; }
    public String getDescription() { return description; }
    public boolean isEmergency() { return this == EMERGENCY; }
}