package io.github.pulse.client;

import io.github.pulse.overlay.HudOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBind;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class PulseClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("pulse-client");

    private static HudOverlay hudOverlay;
    private static KeyBind toggleOverlayKey;
    private static KeyBind toggleDebugKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Pulse client");

        toggleOverlayKey = KeyBindingHelper.registerKeyBinding(new KeyBind(
            "key.pulse.toggle_overlay",
            GLFW.GLFW_KEY_F6,
            "category.pulse"
        ));

        toggleDebugKey = KeyBindingHelper.registerKeyBinding(new KeyBind(
            "key.pulse.toggle_debug",
            GLFW.GLFW_KEY_F7,
            "category.pulse"
        ));

        hudOverlay = new HudOverlay();
        hudOverlay.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleOverlayKey.wasPressed()) {
                hudOverlay.toggle();
            }
            while (toggleDebugKey.wasPressed()) {
                hudOverlay.toggleDebug();
            }
        });

        LOGGER.info("Pulse client initialized");
    }

    public static HudOverlay getHudOverlay() {
        return hudOverlay;
    }
}
