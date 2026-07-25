package io.github.pulse.overlay;

import io.github.pulse.Pulse;
import io.github.pulse.api.AdaptiveModule;
import io.github.pulse.api.OptimizationStage;
import io.github.pulse.api.PulseAPI;
import io.github.pulse.config.ConfigManager;
import io.github.pulse.config.PulseConfig.OverlayPosition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
    private static final int SPARKLINE_WIDTH = 80;
    private static final int SPARKLINE_HEIGHT = 20;
    private static final int SPARKLINE_SAMPLES = 60;
    private static final int PADDING = 4;
    private static final int BG_COLOR = 0xCC0A0A0A;
    private static final int TEXT_COLOR = 0xFFE0E0E0;

    private boolean visible = true;
    private boolean debugMode = false;
    private float animationAlpha = 1.0f;
    private long lastActivityTime = 0;
    private long lastFrameTime = 0;
    private double currentFrameTimeMs = 0;
    private double[] frameTimeHistory = new double[SPARKLINE_SAMPLES];
    private int historyIndex = 0;
    private int historyCount = 0;

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
    private boolean lastShowGraph = false;
    private boolean lastDebugMode = false;

    private boolean initialized = false;

    public void initialize() {
        HudElementRegistry.addLast(
            Identifier.of("pulse", "overlay"),
            (graphics, deltaTracker) -> render(graphics)
        );
        this.lastActivityTime = System.currentTimeMillis();
        this.animationAlpha = 1.0f;
        this.initialized = true;
        LOGGER.info("HudOverlay initialized");
    }

    public void toggle() {
        this.visible = !this.visible;
        if (this.visible) {
            this.lastActivityTime = System.currentTimeMillis();
            this.animationAlpha = 1.0f;
        }
    }

    public void toggleDebug() {
        this.debugMode = !this.debugMode;
        ConfigManager.getInstance().getConfig().setDebugMode(this.debugMode);
        this.lastActivityTime = System.currentTimeMillis();
        this.animationAlpha = 1.0f;
    }

    private void render(GuiGraphicsExtractor context) {
        if (!initialized || !visible) return;

        long now = System.currentTimeMillis();
        updateFrameTime(now);

        boolean dataChanged = updateData();
        if (dataChanged) lastActivityTime = now;

        if (!debugMode) {
            long timeSinceActivity = now - lastActivityTime;
            if (timeSinceActivity > AUTO_HIDE_DELAY_MS) {
                long fadeElapsed = timeSinceActivity - AUTO_HIDE_DELAY_MS;
                animationAlpha = Math.max(0.0f, 1.0f - (float) fadeElapsed / (float) FADE_DURATION_MS);
                if (animationAlpha <= 0.0f) return;
            } else {
                animationAlpha = Math.min(1.0f, animationAlpha + 0.05f);
            }
        } else {
            animationAlpha = 1.0f;
        }

        renderOverlay(context);
        lastFrameTime = now;
    }

    private void updateFrameTime(long now) {
        if (lastFrameTime > 0) {
            long delta = now - lastFrameTime;
            if (delta > 0) {
                currentFrameTimeMs = Math.min(delta, 1000.0);
                frameTimeHistory[historyIndex] = currentFrameTimeMs;
                historyIndex = (historyIndex + 1) % SPARKLINE_SAMPLES;
                if (historyCount < SPARKLINE_SAMPLES) historyCount++;
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

        boolean showGraph = ConfigManager.isShowFrameTimeGraph();
        if (showGraph != lastShowGraph) {
            lastShowGraph = showGraph;
            changed = true;
        }

        if (debugMode != lastDebugMode) {
            lastDebugMode = debugMode;
            changed = true;
        }

        return changed;
    }

    private void renderOverlay(GuiGraphicsExtractor context) {
        Font font = Minecraft.getInstance().font;
        if (font == null) return;

        List<String> lines = new ArrayList<>();
        lines.add(cachedFpsText + " | " + cachedFrameTimeText);
        lines.add(cachedTargetFpsText + " | " + cachedPressureText);
        lines.add(cachedStageText + " | " + cachedBudgetText);
        lines.add("Active: " + cachedOptimizationsText);
        if (!cachedSavingsText.isEmpty()) lines.add(cachedSavingsText);

        if (!debugMode && lastPressure >= 0) {
            lines.set(1, cachedTargetFpsText + " | P: " + lastPressure);
        }

        int maxLineWidth = 0;
        for (String line : lines) {
            int w = font.width(line);
            if (w > maxLineWidth) maxLineWidth = w;
        }

        int graphHeight = lastShowGraph && historyCount > 1 ? SPARKLINE_HEIGHT + PADDING : 0;
        int overlayWidth = Math.max(maxLineWidth + PADDING * 2, SPARKLINE_WIDTH + PADDING * 2);
        int overlayHeight = lines.size() * (font.lineHeight + 1) + PADDING * 2 + graphHeight;

        int[] pos = getOverlayPosition(overlayWidth, overlayHeight);
        int x = pos[0];
        int y = pos[1];

        int alphaByte = Math.round(animationAlpha * 255);
        int bgWithAlpha = (BG_COLOR & 0x00FFFFFF) | (alphaByte << 24);
        int textWithAlpha = (TEXT_COLOR & 0x00FFFFFF) | (alphaByte << 24);

        context.fill(x, y, x + overlayWidth, y + overlayHeight, bgWithAlpha);

        int textY = y + PADDING;
        for (String line : lines) {
            int color = textWithAlpha;
            if (line.startsWith("Pressure:") || line.contains("P: ")) {
                color = getPressureColor(lastPressure, animationAlpha);
            }
            context.drawText(font, line, x + PADDING, textY, color, false);
            textY += font.lineHeight + 1;
        }

        if (lastShowGraph && historyCount > 1) {
            renderSparkline(context, x + PADDING, textY + PADDING, overlayWidth - PADDING * 2);
        }
    }

    private void renderSparkline(GuiGraphicsExtractor context, int x, int y, int width) {
        int samples = Math.min(historyCount, SPARKLINE_SAMPLES);
        if (samples < 2) return;

        double maxFrameTime = 0;
        for (int i = 0; i < samples; i++) {
            int idx = (historyIndex - samples + i + SPARKLINE_SAMPLES) % SPARKLINE_SAMPLES;
            if (frameTimeHistory[idx] > maxFrameTime) maxFrameTime = frameTimeHistory[idx];
        }
        if (maxFrameTime <= 0) maxFrameTime = 16.67;

        int barWidth = Math.max(2, width / samples);
        int sparkAlpha = Math.round(animationAlpha * 180);
        int sparkColor = (0x88FF88 & 0x00FFFFFF) | (sparkAlpha << 24);

        for (int i = 0; i < samples; i++) {
            int idx = (historyIndex - samples + i + SPARKLINE_SAMPLES) % SPARKLINE_SAMPLES;
            double value = frameTimeHistory[idx];
            int barHeight = (int) ((value / maxFrameTime) * SPARKLINE_HEIGHT);
            if (barHeight < 1) barHeight = 1;
            int bx = x + i * (barWidth + 1);
            int by = y + SPARKLINE_HEIGHT - barHeight;
            context.fill(bx, by, bx + barWidth, y + SPARKLINE_HEIGHT, sparkColor);
        }

        int labelAlpha = Math.round(animationAlpha * 255);
        int labelColor = (0xAAAAAA & 0x00FFFFFF) | (labelAlpha << 24);
        Font sparklineFont = Minecraft.getInstance().font;
        String maxLabel = String.format("%.0fms", maxFrameTime);
        context.drawText(sparklineFont, maxLabel, x + width - sparklineFont.width(maxLabel), y - sparklineFont.lineHeight - 1, labelColor, false);
    }

    private int[] getOverlayPosition(int overlayWidth, int overlayHeight) {
        Minecraft client = Minecraft.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int margin = 4;

        OverlayPosition pos = ConfigManager.getOverlayPosition();
        return switch (pos) {
            case TOP_LEFT -> new int[]{margin, margin};
            case TOP_RIGHT -> new int[]{screenWidth - overlayWidth - margin, margin};
            case BOTTOM_LEFT -> new int[]{margin, screenHeight - overlayHeight - margin};
            case BOTTOM_RIGHT -> new int[]{screenWidth - overlayWidth - margin, screenHeight - overlayHeight - margin};
        };
    }

    private int getPressureColor(int pressure, float animAlpha) {
        int color;
        if (pressure < 30) color = 0x55FF55;
        else if (pressure < 60) color = 0xFFFF55;
        else if (pressure < 80) color = 0xFF8800;
        else color = 0xFF5555;
        int a = Math.round(animAlpha * 255);
        return (color & 0x00FFFFFF) | (a << 24);
    }
}
