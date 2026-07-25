package io.github.pulse.modules;

import io.github.pulse.Pulse;
import io.github.pulse.api.AdaptiveModule;
import io.github.pulse.api.ModuleType;
import io.github.pulse.api.OptimizationStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChunkUploadOptimizer implements AdaptiveModule {

    private static final int[] MAX_UPLOADS_PER_FRAME = {Integer.MAX_VALUE, 8, 4, 2, 1};
    private static final double[] PRIORITY_DISTANCE_WEIGHTS = {1.0, 0.85, 0.7, 0.5, 0.3};

    private boolean enabled = true;
    private OptimizationStage currentStage = OptimizationStage.NORMAL;
    private int maxUploadsPerFrame = Integer.MAX_VALUE;
    private double priorityDistanceWeight = 1.0;

    private final PriorityQueue<ChunkUploadTask> uploadQueue = new PriorityQueue<>(
            (a, b) -> Double.compare(b.priority, a.priority)
    );
    private int queuedUploads;
    private int uploadsThisFrame;
    private long lastFrameTime;
    private long frameBudgetNs;

    public ChunkUploadOptimizer() {
        lastFrameTime = System.nanoTime();
        frameBudgetNs = 8_000_000L;
    }

    @Override
    public void initialize() {
    }

    @Override
    public ModuleType getType() {
        return ModuleType.CHUNKS;
    }

    @Override
    public String getName() {
        return "Chunk Upload Optimizer";
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
        if (maxUploadsPerFrame == Integer.MAX_VALUE) {
            return 0L;
        }
        int base = 64;
        float reduction = 1.0f - (float) maxUploadsPerFrame / base;
        return (long) (Math.min(0.7f, reduction * 0.9f) * 100);
    }

    @Override
    public void update(OptimizationStage stage) {
        OptimizationStage previous = currentStage;
        currentStage = stage;
        int ordinal = stage.ordinal();
        maxUploadsPerFrame = MAX_UPLOADS_PER_FRAME[Math.min(ordinal, MAX_UPLOADS_PER_FRAME.length - 1)];
        priorityDistanceWeight = PRIORITY_DISTANCE_WEIGHTS[Math.min(ordinal, PRIORITY_DISTANCE_WEIGHTS.length - 1)];
        if (stage == OptimizationStage.EMERGENCY) {
            frameBudgetNs = 2_000_000L;
        } else if (stage == OptimizationStage.HEAVY) {
            frameBudgetNs = 4_000_000L;
        } else if (stage == OptimizationStage.MODERATE) {
            frameBudgetNs = 6_000_000L;
        } else {
            frameBudgetNs = 8_000_000L;
        }
        if (stage != previous) {
            onStageChange(previous, stage);
        }
    }

    public void onStageChange(OptimizationStage oldStage, OptimizationStage newStage) {
        Pulse.LOGGER.debug("ChunkUploadOptimizer stage changed from {} to {}", oldStage, newStage);
        if (newStage.ordinal() > oldStage.ordinal()) {
            trimQueue();
        }
    }

    public void reset() {
        currentStage = OptimizationStage.NORMAL;
        maxUploadsPerFrame = Integer.MAX_VALUE;
        priorityDistanceWeight = 1.0;
        frameBudgetNs = 8_000_000L;
        uploadQueue.clear();
        queuedUploads = 0;
        uploadsThisFrame = 0;
    }

    @Override
    public void recover() {
        reset();
    }

    @Override
    public double getCurrentImpact() {
        return (double) queuedUploads / Math.max(1, maxUploadsPerFrame);
    }

    @Override
    public ModulePriority getPriority() {
        return ModulePriority.HIGH;
    }

    @Override
    public void shutdown() {
        reset();
    }

    public void enqueueUpload(SectionRenderDispatcher.RenderSection chunk) {
        if (chunk == null) {
            return;
        }
        double priority = calculatePriority(chunk);
        uploadQueue.offer(new ChunkUploadTask(chunk, priority));
        queuedUploads++;
    }

    public List<SectionRenderDispatcher.RenderSection> getUploadsForFrame() {
        List<SectionRenderDispatcher.RenderSection> uploads = new ArrayList<>();
        long frameStart = System.nanoTime();
        int count = 0;
        while (!uploadQueue.isEmpty() && count < maxUploadsPerFrame) {
            if (System.nanoTime() - frameStart > frameBudgetNs) {
                break;
            }
            ChunkUploadTask task = uploadQueue.poll();
            if (task != null) {
                uploads.add(task.chunk);
                count++;
                queuedUploads--;
            }
        }
        uploadsThisFrame = count;
        return uploads;
    }

    public void markUploadComplete(SectionRenderDispatcher.RenderSection chunk) {
        uploadsThisFrame = Math.max(0, uploadsThisFrame - 1);
    }

    public boolean hasPendingUploads() {
        return !uploadQueue.isEmpty();
    }

    public int getQueuedUploadCount() {
        return queuedUploads;
    }

    public int getMaxUploadsPerFrame() {
        return maxUploadsPerFrame;
    }

    public double getPriorityDistanceWeight() {
        return priorityDistanceWeight;
    }

    private double calculatePriority(SectionRenderDispatcher.RenderSection chunk) {
        Minecraft minecraft = Minecraft.getInstance();
        Vec3 cameraPos = minecraft.player != null ? minecraft.player.getEyePosition() : Vec3.ZERO;
        BlockPos chunkPos = chunk.getRenderOrigin();
        Vec3 chunkCenter = new Vec3(
                chunkPos.getX() + 8.0,
                chunkPos.getY() + 8.0,
                chunkPos.getZ() + 8.0
        );
        double distance = chunkCenter.distanceTo(cameraPos);
        double distanceScore = Math.max(0.0, 1.0 - distance / 256.0) * priorityDistanceWeight;
        Vec3 lookVec = minecraft.player != null ? minecraft.player.getLookAngle() : Vec3.ZERO;
        Vec3 toChunk = chunkCenter.subtract(cameraPos).normalize();
        double dotProduct = Math.max(0.0, lookVec.dot(toChunk));
        double directionScore = dotProduct * 0.4;
        double crosshairBonus = 0.0;
        if (minecraft.crosshairPickEntity != null) {
            Vec3 targetPos = minecraft.crosshairPickEntity.position();
            double targetDist = chunkCenter.distanceTo(targetPos);
            if (targetDist < 16.0) {
                crosshairBonus = 0.3;
            }
        }
        LocalPlayer player = minecraft.player;
        double velocityBonus = 0.0;
        if (player != null) {
            Vec3 velocity = player.getDeltaMovement();
            double speed = velocity.length();
            if (speed > 0.1) {
                Vec3 velDir = velocity.normalize();
                double velDot = Math.max(0.0, velDir.dot(toChunk));
                velocityBonus = velDot * Math.min(1.0, speed / 30.0) * 0.2;
            }
        }
        return distanceScore + directionScore + crosshairBonus + velocityBonus;
    }

    private void trimQueue() {
        int targetSize = Math.max(10, queuedUploads / 2);
        if (queuedUploads > targetSize) {
            PriorityQueue<ChunkUploadTask> trimmed = new PriorityQueue<>(
                    (a, b) -> Double.compare(b.priority, a.priority)
            );
            int keepCount = 0;
            while (!uploadQueue.isEmpty() && keepCount < targetSize) {
                ChunkUploadTask task = uploadQueue.poll();
                if (task != null) {
                    trimmed.offer(task);
                    keepCount++;
                }
            }
            uploadQueue.clear();
            uploadQueue.addAll(trimmed);
            queuedUploads = trimmed.size();
            Pulse.LOGGER.debug("Trimmed chunk upload queue to {} tasks", queuedUploads);
        }
    }

    private static class ChunkUploadTask {
        final SectionRenderDispatcher.RenderSection chunk;
        final double priority;

        ChunkUploadTask(SectionRenderDispatcher.RenderSection chunk, double priority) {
            this.chunk = chunk;
            this.priority = priority;
        }
    }
}
