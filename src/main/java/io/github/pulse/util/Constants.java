package io.github.pulse.util;

public final class Constants {
    public static final float TARGET_FPS = 60.0f;
    public static final double FRAME_SPIKE_THRESHOLD_MS = 50.0;
    public static final int PRESSURE_HYSTERESIS = 5;
    public static final int PRESSURE_RATE_LIMIT = 3;
    public static final long RECOVERY_TIME_MINOR_MS = 5000L;
    public static final long RECOVERY_TIME_MODERATE_MS = 10000L;
    public static final long RECOVERY_TIME_HEAVY_MS = 20000L;
    public static final long RECOVERY_TIME_EMERGENCY_MS = 30000L;
    public static final int WINDOW_1S = 20;
    public static final int WINDOW_5S = 100;
    public static final int WINDOW_15S = 300;
    public static final int WINDOW_60S = 1200;
    public static final double MAX_SPIKE_RATE = 0.1;
    public static final double MAX_CHUNK_REBUILDS = 50.0;
    public static final double MAX_GPU_UPLOADS = 30.0;
    public static final double MAX_PLAYER_SPEED = 30.0;
    public static final int MAX_PARTICLES = 5000;
    public static final int MAX_TRANSPARENT = 2000;
    public static final int MAX_ENTITIES = 200;
    public static final double MEMORY_CRITICAL_THRESHOLD = 0.85;
    public static final double MEMORY_HIGH_THRESHOLD = 0.70;
    public static final long NANOS_PER_MILLI = 1_000_000L;
    public static final long NANOS_PER_SECOND = 1_000_000_000L;

    private Constants() {}
}
