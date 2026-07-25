package io.github.pulse.modules;

import io.github.pulse.Pulse;
import io.github.pulse.api.AdaptiveModule;
import io.github.pulse.api.ModuleType;
import io.github.pulse.api.OptimizationStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;

public class UIOptimizer implements AdaptiveModule {

    private static final int[] CHAT_UPDATE_INTERVALS = {1, 2, 3, 5, 10};
    private static final float[] TOOLTIP_DETAIL_FACTORS = {1.0f, 0.9f, 0.75f, 0.5f, 0.25f};
    private static final float[] OVERLAY_OPACITY_FACTORS = {1.0f, 0.95f, 0.85f, 0.7f, 0.5f};

    private boolean enabled = true;
    private OptimizationStage currentStage = OptimizationStage.NORMAL;
    private int chatUpdateInterval = 1;
    private float tooltipDetailFactor = 1.0f;
    private float overlayOpacityFactor = 1.0f;
    private boolean reduceBackgroundRendering;
    private boolean skipParticleEffects;
    private int chatTickCounter;

    @Override
    public void initialize() {
    }

    @Override
    public ModuleType getType() {
        return ModuleType.UI;
    }

    @Override
    public String getName() {
        return "UI Optimizer";
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
        long saving = 0L;
        if (chatUpdateInterval > 1) {
            saving += (long) ((1.0f - 1.0f / chatUpdateInterval) * 15L);
        }
        saving += (long) ((1.0f - tooltipDetailFactor) * 20L);
        saving += (long) ((1.0f - overlayOpacityFactor) * 10L);
        if (reduceBackgroundRendering) {
            saving += 10L;
        }
        if (skipParticleEffects) {
            saving += 10L;
        }
        return Math.min(50L, saving);
    }

    @Override
    public void update(OptimizationStage stage) {
        OptimizationStage previous = currentStage;
        currentStage = stage;
        int ordinal = stage.ordinal();
        chatUpdateInterval = CHAT_UPDATE_INTERVALS[Math.min(ordinal, CHAT_UPDATE_INTERVALS.length - 1)];
        tooltipDetailFactor = TOOLTIP_DETAIL_FACTORS[Math.min(ordinal, TOOLTIP_DETAIL_FACTORS.length - 1)];
        overlayOpacityFactor = OVERLAY_OPACITY_FACTORS[Math.min(ordinal, OVERLAY_OPACITY_FACTORS.length - 1)];
        reduceBackgroundRendering = ordinal >= OptimizationStage.MODERATE.ordinal();
        skipParticleEffects = ordinal >= OptimizationStage.HEAVY.ordinal();
        chatTickCounter++;
        if (stage != previous) {
            onStageChange(previous, stage);
        }
    }

    public void onStageChange(OptimizationStage oldStage, OptimizationStage newStage) {
        Pulse.LOGGER.debug("UIOptimizer stage changed from {} to {}", oldStage, newStage);
        if (newStage == OptimizationStage.EMERGENCY) {
            chatTickCounter = 0;
        }
    }

    public void reset() {
        currentStage = OptimizationStage.NORMAL;
        chatUpdateInterval = 1;
        tooltipDetailFactor = 1.0f;
        overlayOpacityFactor = 1.0f;
        reduceBackgroundRendering = false;
        skipParticleEffects = false;
        chatTickCounter = 0;
    }

    @Override
    public void recover() {
        reset();
    }

    @Override
    public double getCurrentImpact() {
        return 0.0;
    }

    @Override
    public ModulePriority getPriority() {
        return ModulePriority.LOW;
    }

    @Override
    public void shutdown() {
        reset();
    }

    public boolean shouldUpdateChat() {
        if (currentStage == OptimizationStage.NORMAL) {
            return true;
        }
        return chatTickCounter % chatUpdateInterval == 0;
    }

    public int getChatUpdateInterval() {
        return chatUpdateInterval;
    }

    public float getTooltipDetailFactor() {
        return tooltipDetailFactor;
    }

    public boolean shouldSimplifyTooltip() {
        if (currentStage == OptimizationStage.NORMAL) {
            return false;
        }
        return tooltipDetailFactor < 0.8f;
    }

    public int getTooltipBackgroundAlpha() {
        if (currentStage == OptimizationStage.NORMAL) {
            return 255;
        }
        return (int) (255 * overlayOpacityFactor);
    }

    public float getOverlayOpacityFactor() {
        return overlayOpacityFactor;
    }

    public boolean shouldReduceBackgroundRendering() {
        return reduceBackgroundRendering;
    }

    public boolean shouldSkipUiParticleEffects() {
        return skipParticleEffects;
    }

    public boolean shouldReduceBossBarEffects() {
        if (currentStage == OptimizationStage.NORMAL) {
            return false;
        }
        return currentStage.ordinal() >= OptimizationStage.MODERATE.ordinal();
    }

    public boolean shouldSkipItemTooltipDecorations() {
        if (currentStage == OptimizationStage.NORMAL) {
            return false;
        }
        return tooltipDetailFactor < 0.6f;
    }

    public boolean shouldReduceScoreboardDetail() {
        if (currentStage == OptimizationStage.NORMAL) {
            return false;
        }
        return currentStage.ordinal() >= OptimizationStage.MODERATE.ordinal();
    }

    public int getChatFadeTime(int baseFadeTime) {
        if (currentStage == OptimizationStage.NORMAL) {
            return baseFadeTime;
        }
        return Math.max(40, baseFadeTime / chatUpdateInterval);
    }

    public boolean shouldRenderBackgrounds() {
        if (!reduceBackgroundRendering) {
            return true;
        }
        return currentStage.ordinal() < OptimizationStage.HEAVY.ordinal();
    }

    public void onChatTick() {
        chatTickCounter++;
    }
}
