package io.github.pulse.performance;

import io.github.pulse.util.Constants;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceEngine.class);
    private static PerformanceEngine instance;

    private final RollingStatistics frameTimeStats1s;
    private final RollingStatistics frameTimeStats5s;
    private final RollingStatistics frameTimeStats15s;
    private final RollingStatistics frameTimeStats60s;

    private final RollingStatistics spikeStats1s;
    private final RollingStatistics spikeStats5s;
    private final RollingStatistics spikeStats15s;
    private final RollingStatistics spikeStats60s;

    private float currentFps;
    private double currentFrameTimeMs;
    private double playerSpeed;
    private double cameraMovement;
    private int chunkRebuildActivity;
    private int gpuUploadActivity;
    private int particleCount;
    private int visibleEntityCount;
    private int transparentObjectCount;
    private double memoryPressure;
    private int gcEvents;

    private float prevYaw;
    private float prevPitch;
    private boolean hasPrevRotation;
    private long lastGcCount;
    private long lastUpdateTime;
    private boolean initialized;

    private PerformanceEngine() {
        this.frameTimeStats1s = new RollingStatistics(Constants.WINDOW_1S);
        this.frameTimeStats5s = new RollingStatistics(Constants.WINDOW_5S);
        this.frameTimeStats15s = new RollingStatistics(Constants.WINDOW_15S);
        this.frameTimeStats60s = new RollingStatistics(Constants.WINDOW_60S);
        this.spikeStats1s = new RollingStatistics(Constants.WINDOW_1S);
        this.spikeStats5s = new RollingStatistics(Constants.WINDOW_5S);
        this.spikeStats15s = new RollingStatistics(Constants.WINDOW_15S);
        this.spikeStats60s = new RollingStatistics(Constants.WINDOW_60S);
        this.lastUpdateTime = System.nanoTime();
        this.hasPrevRotation = false;
        this.initialized = false;

        long totalGc = 0;
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            totalGc += gc.getCollectionCount();
        }
        this.lastGcCount = totalGc;
    }

    public static PerformanceEngine getInstance() {
        if (instance == null) {
            instance = new PerformanceEngine();
        }
        return instance;
    }

    public void initialize() {
        if (initialized) return;
        initialized = true;
        LOGGER.info("PerformanceEngine initialized");
    }

    public void shutdown() {
        if (!initialized) return;
        initialized = false;
        reset();
        LOGGER.info("PerformanceEngine shut down");
    }

    public void update() {
        if (!initialized) return;

        Minecraft client = Minecraft.getInstance();

        long now = System.nanoTime();
        double deltaMs = (now - lastUpdateTime) / Constants.NANOS_PER_MILLI;
        lastUpdateTime = now;

        currentFps = client.fps;
        currentFrameTimeMs = deltaMs;

        frameTimeStats1s.addSample(currentFrameTimeMs);
        frameTimeStats5s.addSample(currentFrameTimeMs);
        frameTimeStats15s.addSample(currentFrameTimeMs);
        frameTimeStats60s.addSample(currentFrameTimeMs);

        boolean isSpike = currentFrameTimeMs > Constants.FRAME_SPIKE_THRESHOLD_MS;
        double spikeValue = isSpike ? 1.0 : 0.0;
        spikeStats1s.addSample(spikeValue);
        spikeStats5s.addSample(spikeValue);
        spikeStats15s.addSample(spikeValue);
        spikeStats60s.addSample(spikeValue);

        LocalPlayer player = client.player;
        if (player != null) {
            Vec3 velocity = player.getVelocity();
            playerSpeed = (float) velocity.length();

            float yaw = player.getYaw();
            float pitch = player.getPitch();
            if (hasPrevRotation) {
                float dyaw = Math.abs(yaw - prevYaw);
                float dpitch = Math.abs(pitch - prevPitch);
                if (dyaw > 180.0f) dyaw = 360.0f - dyaw;
                cameraMovement = Math.sqrt(dyaw * dyaw + dpitch * dpitch);
            } else {
                cameraMovement = 0.0;
                hasPrevRotation = true;
            }
            prevYaw = yaw;
            prevPitch = pitch;
        } else {
            playerSpeed = 0.0;
            cameraMovement = 0.0;
            hasPrevRotation = false;
        }

        if (client.levelRenderer != null) {
            chunkRebuildActivity = client.levelRenderer.getChunkBuilder().getQueuedBuildCount();
            gpuUploadActivity = client.levelRenderer.getChunkBuilder().getTotalUploadedBytes() > 0 ? 1 : 0;
        } else {
            chunkRebuildActivity = 0;
            gpuUploadActivity = 0;
        }

        if (client.particleEngine != null) {
            particleCount = client.particleEngine.getParticleCount();
        } else {
            particleCount = 0;
        }

        ClientLevel world = client.level;
        if (world != null) {
            visibleEntityCount = world.getRegularEntities().size();
            transparentObjectCount = world.getBlockEntityTickList().size();
        } else {
            visibleEntityCount = 0;
            transparentObjectCount = 0;
        }

        Runtime runtime = Runtime.getRuntime();
        long totalMem = runtime.totalMemory();
        long freeMem = runtime.freeMemory();
        memoryPressure = totalMem > 0 ? (double) (totalMem - freeMem) / totalMem : 0.0;

        long currentGc = 0;
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            currentGc += gc.getCollectionCount();
        }
        gcEvents = (int) (currentGc - lastGcCount);
        lastGcCount = currentGc;
    }

    public void reset() {
        frameTimeStats1s.clear();
        frameTimeStats5s.clear();
        frameTimeStats15s.clear();
        frameTimeStats60s.clear();
        spikeStats1s.clear();
        spikeStats5s.clear();
        spikeStats15s.clear();
        spikeStats60s.clear();
        currentFps = 0.0f;
        currentFrameTimeMs = 0.0;
        playerSpeed = 0.0;
        cameraMovement = 0.0;
        chunkRebuildActivity = 0;
        gpuUploadActivity = 0;
        particleCount = 0;
        visibleEntityCount = 0;
        transparentObjectCount = 0;
        memoryPressure = 0.0;
        gcEvents = 0;
        hasPrevRotation = false;
        lastUpdateTime = System.nanoTime();
    }

    public float getCurrentFps() {
        return currentFps;
    }

    public double getAverageFps() {
        if (frameTimeStats60s.getCount() == 0) return currentFps;
        double avgFrameTimeMs = frameTimeStats60s.getAverage();
        return avgFrameTimeMs > 0 ? (float) (1000.0 / avgFrameTimeMs) : 0.0f;
    }

    public double getPercentileLow1() {
        double p1 = frameTimeStats60s.getPercentile(1.0);
        return p1 > 0 ? 1000.0 / p1 : 0.0;
    }

    public double getPercentileLow01() {
        double p01 = frameTimeStats60s.getPercentile(0.1);
        return p01 > 0 ? 1000.0 / p01 : 0.0;
    }

    public double getFrameTime() {
        return currentFrameTimeMs;
    }

    public double getFrameTimeVariance() {
        return frameTimeStats60s.getVariance();
    }

    public int getFrameSpikeCount() {
        return (int) Math.round(spikeStats60s.getSum());
    }

    public double getPlayerVelocity() {
        return playerSpeed;
    }

    public double getCameraMovement() {
        return cameraMovement;
    }

    public int getChunkRebuildActivity() {
        return chunkRebuildActivity;
    }

    public int getGpuUploadActivity() {
        return gpuUploadActivity;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public int getVisibleEntityCount() {
        return visibleEntityCount;
    }

    public int getTransparentObjectCount() {
        return transparentObjectCount;
    }

    public double getMemoryPressure() {
        return memoryPressure;
    }

    public int getGcEvents() {
        return gcEvents;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public RollingStatistics getFrameTimeStats1s() {
        return frameTimeStats1s;
    }

    public RollingStatistics getFrameTimeStats5s() {
        return frameTimeStats5s;
    }

    public RollingStatistics getFrameTimeStats15s() {
        return frameTimeStats15s;
    }

    public RollingStatistics getFrameTimeStats60s() {
        return frameTimeStats60s;
    }

    public RollingStatistics getSpikeStats60s() {
        return spikeStats60s;
    }
}
