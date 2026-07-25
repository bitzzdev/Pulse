package io.github.pulse.overlay;

import io.github.pulse.Pulse;
import io.github.pulse.api.AdaptiveModule;
import io.github.pulse.api.OptimizationStage;
import io.github.pulse.api.PulseAPI;
import io.github.pulse.config.ConfigManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class HudOverlay {
    private static final Logger LOGGER = LoggerFactory.getLogger("pulse-overlay");
    private static final long AUTO_HIDE_DELAY_MS = 10000;
    private static final long FADE_DURATION_MS = 500;
    private static final int PADDING = 4;
    private static final int BG_COLOR = 0xCC0A0A0A;
    private static final int TEXT_COLOR = 0xFFE0E0E0;

    private boolean visible = true;
    private boolean debugMode = false;
    private float animationAlpha = 1.0f;
    private long lastActivityTime = 0;
    private long lastFrameTime = 0;
    private double currentFrameTimeMs = 0;

    private String cachedFpsText = "";
    private String cachedFrameTimeText = "";
    private String cachedTargetFpsText = "";
    private String cachedPressureText = "";
    private String cachedStageText = "";
    private String cachedBudgetText = "";
    private String cachedOptimizationsText = "";
    private String cachedSavingsText = "";

    private int lastFps = -1;
    private double lastFrameTimeMs = -1;
    private int lastTargetFps = -1;
    private int lastPressure = -1;
    private OptimizationStage lastStage = null;
    private double lastBudgetUsage = -1;
    private String lastOptimizations = "";
    private long lastSavings = -1;
    private boolean lastDebugMode = false;

    private boolean initialized = false;

    public void initialize() {
        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath("pulse", "overlay"),
            (GuiGraphics graphics, DeltaTracker tickCounter) -> render(graphics)
        );
        this.lastActivityTime = System.currentTimeMillis();
        this.animationAlpha = 1.0f;
        this.initialized = true;
        LOGGER.info("HudOverlay initialized");
    }

    public boolean isVisible() { return visible; }
    public boolean isDebugMode() { return debugMode; }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        if (debugMode) {
            this.lastActivityTime = System.currentTimeMillis();
            this.animationAlpha = 1.0f;
        }
    }

    private void render(GuiGraphics context) {
        if (!initialized || !visible || !debugMode) return;

        long now = System.currentTimeMillis();
        updateFrameTime(now);

        boolean dataChanged = updateData();
        if (dataChanged) lastActivityTime = now;

        long timeSinceActivity = now - lastActivityTime;
        if (timeSinceActivity > AUTO_HIDE_DELAY_MS) {
            long fadeElapsed = timeSinceActivity - AUTO_HIDE_DELAY_MS;
            animationAlpha = Math.max(0.0f, 1.0f - (float) fadeElapsed / (float) FADE_DURATION_MS);
            if (animationAlpha <= 0.0f) return;
        } else {
            animationAlpha = Math.min(1.0f, animationAlpha + 0.05f);
        }

        lastFrameTime = now;
    }

    private void updateFrameTime(long now) {
        if (lastFrameTime > 0) {
            long delta = now - lastFrameTime;
            if (delta > 0) {
                currentFrameTimeMs = Math.min(delta, 1000.0);
            }
        }
    }

    private boolean updateData() {
        boolean changed = false;

        int fps = PulseAPI.getCurrentFps();
        if (fps != lastFps) {
            lastFps = fps;
            cachedFpsText = String.format("FPS: %d", fps);
            changed = true;
        }

        if (Math.abs(currentFrameTimeMs - lastFrameTimeMs) > 0.1) {
            lastFrameTimeMs = currentFrameTimeMs;
            cachedFrameTimeText = String.format("Frame: %.1fms", currentFrameTimeMs);
            changed = true;
        }

        int targetFps = ConfigManager.getTargetFps();
        if (targetFps != lastTargetFps) {
            lastTargetFps = targetFps;
            cachedTargetFpsText = String.format("Target: %d FPS", targetFps);
            changed = true;
        }

        int pressure = PulseAPI.getPressureScore();
        if (pressure != lastPressure) {
            lastPressure = pressure;
            cachedPressureText = String.format("Pressure: %d/100", pressure);
            changed = true;
        }

        OptimizationStage stage = PulseAPI.getOptimizationStage();
        if (stage != lastStage) {
            lastStage = stage;
            cachedStageText = String.format("Stage: %s (%d/4)", stage.name(), stage.getLevel());
            changed = true;
        }

        double budgetUsage = 0;
        if (Pulse.getBudgetSystem() != null) {
            budgetUsage = Pulse.getBudgetSystem().getBudgetUsage() * 100.0;
        }
        if (Math.abs(budgetUsage - lastBudgetUsage) > 0.5) {
            lastBudgetUsage = budgetUsage;
            cachedBudgetText = String.format("Budget: %.0f%%", budgetUsage);
            changed = true;
        }

        StringBuilder optimizations = new StringBuilder();
        long totalSavings = 0;
        if (Pulse.getModuleManager() != null) {
            List<AdaptiveModule> modules = Pulse.getModuleManager().getActiveModules();
            for (int i = 0; i < modules.size(); i++) {
                if (i > 0) optimizations.append(", ");
                optimizations.append(modules.get(i).getName());
                totalSavings += modules.get(i).getEstimatedSavings();
            }
        }
        String optStr = optimizations.toString();
        if (!optStr.equals(lastOptimizations)) {
            lastOptimizations = optStr;
            cachedOptimizationsText = optStr.isEmpty() ? "None" : optStr;
            changed = true;
        }
        if (totalSavings != lastSavings) {
            lastSavings = totalSavings;
            cachedSavingsText = totalSavings > 0 ? String.format("Savings: ~%d%%", totalSavings) : "";
            changed = true;
        }

        if (debugMode != lastDebugMode) {
            lastDebugMode = debugMode;
            changed = true;
        }

        return changed;
    }

    private int getPressureColor(int pressure, float animAlpha) {
        int color;
        if (pressure < 30) color = 0x55FF55;
        else if (pressure < 80) color = 0xFF8800;
        else color = 0xFF5555;
        int a = Math.round(animAlpha * 255);
        return (color & 0x00FFFFFF) | (a << 24);
    }
}
