package io.github.pulse.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.pulse.api.OptimizationStage;
import io.github.pulse.api.PulseAPI;
import io.github.pulse.overlay.HudOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class PulseClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("pulse-client");

    private static HudOverlay hudOverlay;
    private static KeyMapping toggleOverlayKey;
    private static KeyMapping toggleDebugKey;
    private static OptimizationStage lastCaptionStage = null;
    private static int captionCooldown = 0;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Pulse client");

        toggleOverlayKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.pulse.toggle_overlay",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F6,
            new KeyMapping.Category(Identifier.fromNamespaceAndPath("pulse", "options"))
        ));

        toggleDebugKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.pulse.toggle_debug",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F7,
            new KeyMapping.Category(Identifier.fromNamespaceAndPath("pulse", "options"))
        ));

        hudOverlay = new HudOverlay();
        hudOverlay.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleOverlayKey.consumeClick()) {
                hudOverlay.toggle();
            }
            while (toggleDebugKey.consumeClick()) {
                hudOverlay.toggleDebug();
            }

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
                    showActionBar(caption);
                    captionCooldown = 100;
                }
            }
        });

        LOGGER.info("Pulse client initialized");
    }

    public static void showActionBar(String text) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.literal(text), true);
        }
    }

    public static void showActionBar(Component component) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(component, true);
        }
    }

    public static HudOverlay getHudOverlay() {
        return hudOverlay;
    }
}
