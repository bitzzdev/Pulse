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
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class PulseClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("pulse-client");

    private static OptimizationStage lastCaptionStage = null;
    private static int captionCooldown = 0;
    private static int savedRenderDistance = -1;

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
        var options = client.options;

        if (stage == OptimizationStage.NORMAL) {
            if (savedRenderDistance >= 0) {
                options.renderDistance().set(savedRenderDistance);
                savedRenderDistance = -1;
            }
            return;
        }

        if (savedRenderDistance < 0) {
            savedRenderDistance = options.renderDistance().get();
        }

        int renderDist = switch (stage) {
            case MINOR -> Math.max(2, savedRenderDistance - 2);
            case MODERATE -> Math.max(2, savedRenderDistance - 4);
            case HEAVY -> Math.max(2, savedRenderDistance - 6);
            case EMERGENCY -> Math.max(2, savedRenderDistance - 8);
            default -> savedRenderDistance;
        };

        if (options.renderDistance().get() != renderDist) {
            options.renderDistance().set(renderDist);
        }
    }

    public static void showCaption(String text) {
        LOGGER.info(text);
    }
}
