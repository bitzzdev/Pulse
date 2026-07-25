package io.github.pulse.client;

import io.github.pulse.Pulse;
import io.github.pulse.api.ModuleType;
import io.github.pulse.api.OptimizationStage;
import io.github.pulse.api.PulseAPI;
import io.github.pulse.config.ConfigManager;
import io.github.pulse.modules.EntityOptimizer;
import io.github.pulse.overlay.HudOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRenderEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class PulseClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("pulse-client");

    private static OptimizationStage lastCaptionStage = null;
    private static int captionCooldown = 0;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Pulse client");

        HudOverlay hudOverlay = new HudOverlay();
        hudOverlay.initialize();
        hudOverlay.setDebugMode(ConfigManager.isDebugMode());

        EntityRenderEvents.BEFORE_ENTITY_RENDER.register((entity, camera, position, tickDelta) -> {
            if (PulseAPI.getOptimizationStage() == OptimizationStage.NORMAL) return true;
            EntityOptimizer optimizer = (EntityOptimizer) Pulse.getModuleManager().getModule(ModuleType.ENTITIES);
            if (optimizer == null || !optimizer.isEnabled()) return true;
            return optimizer.shouldRenderEntity(entity);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Pulse.getPerformanceEngine().update();
            Pulse.getPressureEngine().update();
            Pulse.getAdaptiveManager().update();
            Pulse.getModuleManager().update(PulseAPI.getOptimizationStage());

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

    public static void showCaption(String text) {
        LOGGER.info(text);
    }
}
