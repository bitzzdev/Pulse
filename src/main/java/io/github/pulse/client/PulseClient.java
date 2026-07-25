package io.github.pulse.client;

import io.github.pulse.Pulse;
import io.github.pulse.api.OptimizationStage;
import io.github.pulse.api.PulseAPI;
import io.github.pulse.config.ConfigManager;
import io.github.pulse.overlay.HudOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class PulseClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("pulse-client");

    private static OptimizationStage lastCaptionStage = null;
    private static int captionCooldown = 0;

    private static int savedRenderDistance = -1;
    private static double savedEntityDistance = -1;
    private static boolean savedGraphicsFast = false;
    private static double savedRainLevel = -1;
    private static double savedThunderLevel = -1;
    private static boolean settingsSaved = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Pulse client");

        HudOverlay hudOverlay = new HudOverlay();
        hudOverlay.initialize();
        hudOverlay.setDebugMode(ConfigManager.isDebugMode());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Pulse.getPerformanceEngine().update();
            Pulse.getPressureEngine().update();
            Pulse.getAdaptiveManager().update();
            Pulse.getModuleManager().update(PulseAPI.getOptimizationStage());

            applyOptimizations(client);

            if (captionCooldown > 0) captionCooldown--;
            if (captionCooldown <= 0) {
                OptimizationStage stage = PulseAPI.getOptimizationStage();
                if (stage != lastCaptionStage) {
                    lastCaptionStage = stage;
                    String caption = switch (stage) {
                        case NORMAL -> "Pulse: Normal";
                        case MINOR -> "Pulse: Minor optimization";
                        case MODERATE -> "Pulse: Moderate optimization";
                        case HEAVY -> "Pulse: Heavy optimization";
                        case EMERGENCY -> "Pulse: Emergency optimization";
                    };
                    showCaption(caption);
                    captionCooldown = 100;
                }
            }
        });

        LOGGER.info("Pulse client initialized");
    }

    private static void applyOptimizations(Minecraft client) {
        OptimizationStage stage = PulseAPI.getOptimizationStage();
        Options options = client.options;
        ClientLevel level = client.level;

        if (stage == OptimizationStage.NORMAL) {
            restoreSettings(options, level);
            return;
        }

        if (!settingsSaved) {
            savedRenderDistance = options.renderDistance().get();
            savedGraphicsFast = options.graphicsMode().get() == GraphicsStatus.FAST;
            var entDist = options.entityDistanceScaling().get();
            savedEntityDistance = entDist;
            if (level != null) {
                savedRainLevel = level.getRainLevel(1.0f);
                savedThunderLevel = level.getThunderLevel(1.0f);
            }
            settingsSaved = true;
        }

        int ordinal = stage.ordinal();

        int renderDist = Math.max(2, savedRenderDistance - ordinal * 2);
        if (options.renderDistance().get() != renderDist) {
            options.renderDistance().set(renderDist);
        }

        if (ordinal >= OptimizationStage.HEAVY.ordinal()) {
            options.graphicsMode().set(GraphicsStatus.FAST);
        }

        double entDistScale = Math.max(0.25, 1.0 - ordinal * 0.15);
        options.entityDistanceScaling().set(entDistScale);

        if (level != null) {
            float weatherFactor = switch (stage) {
                case MINOR -> 0.5f;
                case MODERATE -> 0.25f;
                case HEAVY -> 0.1f;
                case EMERGENCY -> 0.0f;
                default -> 1.0f;
            };
            level.setRainLevel((float) savedRainLevel * weatherFactor);
            level.setThunderLevel((float) savedThunderLevel * weatherFactor);
        }
    }

    private static void restoreSettings(Options options, ClientLevel level) {
        if (settingsSaved) {
            if (savedRenderDistance >= 0) {
                options.renderDistance().set(savedRenderDistance);
            }
            if (savedGraphicsFast) {
                options.graphicsMode().set(GraphicsStatus.FAST);
            } else {
                options.graphicsMode().set(GraphicsStatus.FANCY);
            }
            if (savedEntityDistance > 0) {
                options.entityDistanceScaling().set(savedEntityDistance);
            }
            if (level != null && savedRainLevel >= 0) {
                level.setRainLevel((float) savedRainLevel);
                level.setThunderLevel((float) savedThunderLevel);
            }
            savedRenderDistance = -1;
            savedEntityDistance = -1;
            savedGraphicsFast = false;
            savedRainLevel = -1;
            savedThunderLevel = -1;
            settingsSaved = false;
        }
    }

    public static void showCaption(String text) {
        LOGGER.info(text);
    }
}
