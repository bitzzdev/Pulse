package io.github.pulse.pressure;

import io.github.pulse.api.OptimizationStage;
import io.github.pulse.performance.PerformanceEngine;
import io.github.pulse.util.Constants;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PressureEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(PressureEngine.class);

    private static final String KEY_SPIKES = "frame_spikes";
    private static final String KEY_FPS_BELOW_TARGET = "fps_below_target";
    private static final String KEY_FRAME_VARIANCE = "frame_time_variance";
    private static final String KEY_CHUNK_REBUILDS = "chunk_rebuild_activity";
    private static final String KEY_GPU_UPLOAD = "gpu_upload_congestion";
    private static final String KEY_VELOCITY = "player_velocity";
    private static final String KEY_PARTICLES = "particle_system_load";
    private static final String KEY_TRANSPARENCY = "transparency_load";
    private static final String KEY_WEATHER = "weather";
    private static final String KEY_ENTITIES = "visible_entity_count";

    private static final double WEIGHT_SPIKES = 0.25;
    private static final double WEIGHT_FPS_BELOW = 0.20;
    private static final double WEIGHT_VARIANCE = 0.15;
    private static final double WEIGHT_CHUNK = 0.10;
    private static final double WEIGHT_GPU = 0.10;
    private static final double WEIGHT_VELOCITY = 0.05;
    private static final double WEIGHT_PARTICLES = 0.05;
    private static final double WEIGHT_TRANSPARENCY = 0.05;
    private static final double WEIGHT_WEATHER = 0.03;
    private static final double WEIGHT_ENTITIES = 0.02;

    private final PerformanceEngine performanceEngine;
    private double lastPressureScore;
    private double currentPressureScore;
    private double displayedPressureScore;
    private Map<String, Double> lastContributions;
    private boolean initialized;

    public PressureEngine() {
        this(PerformanceEngine.getInstance());
    }

    public PressureEngine(PerformanceEngine performanceEngine) {
        this.performanceEngine = performanceEngine;
        this.lastPressureScore = 0.0;
        this.currentPressureScore = 0.0;
        this.displayedPressureScore = 0.0;
        this.lastContributions = Collections.emptyMap();
        this.initialized = false;
    }

    public void initialize() {
        if (initialized) return;
        initialized = true;
        LOGGER.info("PressureEngine initialized");
    }

    public void shutdown() {
        if (!initialized) return;
        initialized = false;
        lastPressureScore = 0.0;
        currentPressureScore = 0.0;
        displayedPressureScore = 0.0;
        LOGGER.info("PressureEngine shut down");
    }

    public void update() {
        if (!initialized) return;

        double rawSpikes = normalizeSpikes();
        double rawFpsBelow = normalizeFpsBelowTarget();
        double rawVariance = normalizeFrameTimeVariance();
        double rawChunk = normalizeChunkRebuilds();
        double rawGpu = normalizeGpuUpload();
        double rawVelocity = normalizePlayerVelocity();
        double rawParticles = normalizeParticles();
        double rawTransparency = normalizeTransparency();
        double rawWeather = normalizeWeather();
        double rawEntities = normalizeEntities();

        Map<String, Double> contributions = new LinkedHashMap<>();
        contributions.put(KEY_SPIKES, rawSpikes * WEIGHT_SPIKES * 100.0);
        contributions.put(KEY_FPS_BELOW_TARGET, rawFpsBelow * WEIGHT_FPS_BELOW * 100.0);
        contributions.put(KEY_FRAME_VARIANCE, rawVariance * WEIGHT_VARIANCE * 100.0);
        contributions.put(KEY_CHUNK_REBUILDS, rawChunk * WEIGHT_CHUNK * 100.0);
        contributions.put(KEY_GPU_UPLOAD, rawGpu * WEIGHT_GPU * 100.0);
        contributions.put(KEY_VELOCITY, rawVelocity * WEIGHT_VELOCITY * 100.0);
        contributions.put(KEY_PARTICLES, rawParticles * WEIGHT_PARTICLES * 100.0);
        contributions.put(KEY_TRANSPARENCY, rawTransparency * WEIGHT_TRANSPARENCY * 100.0);
        contributions.put(KEY_WEATHER, rawWeather * WEIGHT_WEATHER * 100.0);
        contributions.put(KEY_ENTITIES, rawEntities * WEIGHT_ENTITIES * 100.0);

        double total = 0.0;
        for (double v : contributions.values()) {
            total += v;
        }
        currentPressureScore = Math.max(0.0, Math.min(100.0, total));

        applyHysteresis();
        applyRateLimit();

        lastPressureScore = displayedPressureScore;
        lastContributions = contributions;
    }

    private void applyHysteresis() {
        if (Math.abs(currentPressureScore - displayedPressureScore) > Constants.PRESSURE_HYSTERESIS) {
            displayedPressureScore = currentPressureScore;
        }
    }

    private void applyRateLimit() {
        double delta = displayedPressureScore - lastPressureScore;
        if (delta > Constants.PRESSURE_RATE_LIMIT) {
            displayedPressureScore = lastPressureScore + Constants.PRESSURE_RATE_LIMIT;
        } else if (delta < -Constants.PRESSURE_RATE_LIMIT) {
            displayedPressureScore = lastPressureScore - Constants.PRESSURE_RATE_LIMIT;
        }
        displayedPressureScore = Math.max(0.0, Math.min(100.0, displayedPressureScore));
    }

    private double normalizeSpikes() {
        int spikeCount = performanceEngine.getFrameSpikeCount();
        int windowSize = Constants.WINDOW_60S;
        double spikeRate = windowSize > 0 ? (double) spikeCount / windowSize : 0.0;
        return Math.min(1.0, spikeRate / Constants.MAX_SPIKE_RATE);
    }

    private double normalizeFpsBelowTarget() {
        float fps = performanceEngine.getCurrentFps();
        float target = Constants.TARGET_FPS;
        if (fps >= target) return 0.0;
        return Math.min(1.0, (target - fps) / target);
    }

    private double normalizeFrameTimeVariance() {
        double variance = performanceEngine.getFrameTimeVariance();
        double maxVariance = Constants.FRAME_SPIKE_THRESHOLD_MS * Constants.FRAME_SPIKE_THRESHOLD_MS;
        return Math.min(1.0, variance / maxVariance);
    }

    private double normalizeChunkRebuilds() {
        int activity = performanceEngine.getChunkRebuildActivity();
        return Math.min(1.0, activity / Constants.MAX_CHUNK_REBUILDS);
    }

    private double normalizeGpuUpload() {
        int activity = performanceEngine.getGpuUploadActivity();
        return Math.min(1.0, activity / Constants.MAX_GPU_UPLOADS);
    }

    private double normalizePlayerVelocity() {
        double speed = performanceEngine.getPlayerVelocity();
        return Math.min(1.0, speed / Constants.MAX_PLAYER_SPEED);
    }

    private double normalizeParticles() {
        int count = performanceEngine.getParticleCount();
        return Math.min(1.0, (double) count / Constants.MAX_PARTICLES);
    }

    private double normalizeTransparency() {
        int count = performanceEngine.getTransparentObjectCount();
        return Math.min(1.0, (double) count / Constants.MAX_TRANSPARENT);
    }

    private double normalizeWeather() {
        var world = net.minecraft.client.Minecraft.getInstance().level;
        if (world == null) return 0.0;
        if (world.isThundering()) return 1.0;
        if (world.isRaining()) return 0.6;
        return 0.0;
    }

    private double normalizeEntities() {
        int count = performanceEngine.getVisibleEntityCount();
        return Math.min(1.0, (double) count / Constants.MAX_ENTITIES);
    }

    public double getPressureScore() {
        return displayedPressureScore;
    }

    public PressureTrend getPressureTrend() {
        double delta = displayedPressureScore - lastPressureScore;
        if (delta > 1.0) return PressureTrend.RISING;
        if (delta < -1.0) return PressureTrend.FALLING;
        return PressureTrend.STABLE;
    }

    public Map<String, Double> getContributingFactors() {
        return Collections.unmodifiableMap(lastContributions);
    }

    public OptimizationStage getOptimizationStage() {
        double score = displayedPressureScore;
        if (score <= 15) return OptimizationStage.NORMAL;
        if (score <= 35) return OptimizationStage.MINOR;
        if (score <= 55) return OptimizationStage.MODERATE;
        if (score <= 75) return OptimizationStage.HEAVY;
        return OptimizationStage.EMERGENCY;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public enum PressureTrend {
        RISING,
        STABLE,
        FALLING
    }
}
