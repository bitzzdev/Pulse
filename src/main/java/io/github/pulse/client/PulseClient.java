package io.github.pulse.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.pulse.overlay.HudOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
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

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Pulse client");

        toggleOverlayKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.pulse.toggle_overlay",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F6,
            new KeyMapping.Category(Identifier.fromNamespaceAndPath("pulse", "category"))
        ));

        toggleDebugKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.pulse.toggle_debug",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F7,
            new KeyMapping.Category(Identifier.fromNamespaceAndPath("pulse", "category"))
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
        });

        LOGGER.info("Pulse client initialized");
    }

    public static HudOverlay getHudOverlay() {
        return hudOverlay;
    }
}
